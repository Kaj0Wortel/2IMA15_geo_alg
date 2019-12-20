package convex_layers.visual;

import convex_layers.BaseInputVertex;
import convex_layers.OutputEdge;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import tools.MultiTool;
import tools.Var;
import tools.data.array.ArrayTools;
import tools.event.Key;
import tools.font.FontLoader;
import tools.log.Logger;
import tools.log.StreamLogger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class can visualize different sets of points and edges with different colors, auto-scaling and
 * auto adjusted grid. <br>
 * Function keys:
 * <table>
 *   <tr><th>Key</th><th>Function</th></tr>
 *   <tr><td>RIGHT</td><td>Next image</td></tr>
 *   <tr><td>CTRL+RIGHT</td><td>10 next images</td></tr>
 *   <tr><td>LEFT</td><td>Previous image</td></tr>
 *   <tr><td>CTRL+LEFT</td><td>10 previous images</td></tr>
 *   <tr><td>HOME</td><td>First image</td></tr>
 *   <tr><td>END</td><td>Last image</td></tr>
 *   <tr><td>CTRL+S</td><td>Save images</td></tr>
 * </table>
 */
public class Visualizer {
    /* ----------------------------------------------------------------------
     * Constants.
     * ----------------------------------------------------------------------
     */
    // Constants used to tweak the visuals.
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final Paint DEFAULT_POINT_COLOR = new Color(0, 0, 0);
    private static final Paint DEFAULT_EDGE_COLOR = new Color(0, 182, 255);
    private static final Paint GRID_COLOR = new Color(200, 200, 200);
    private static final Paint BIG_GRID_COLOR = new Color(100, 100, 100);
    private static final Paint BACKGROUND_COLOR = new Color(255, 255, 255, 255);
    private static final int POINT_SIZE = 40;
    private static final Stroke EDGE_STROKE = new BasicStroke(5);
    private static final Stroke GRID_STROKE = new BasicStroke(3);
    private static final double EMPTY_RATIO = 0.05;
    private static final Font DEFAULT_FONT = FontLoader.getFont("Cousine-Bold.ttf", 25);
    
    // Function key constants.
    private static final Key NEXT_KEY = Key.RIGHT;
    private static final Key FAST_NEXT_KEY = Key.RIGHT.setMask(Key.CTRL_MASK);
    private static final Key PREV_KEY = Key.LEFT;
    private static final Key FAST_PREV_KEY = Key.LEFT.setMask(Key.CTRL_MASK);
    private static final Key FIRST_KEY = Key.HOME;
    private static final Key LAST_KEY = Key.END;
    private static final Key SAVE_KEY = Key.S.setMask(Key.CTRL_MASK);
    private static final Key DEL_KEY = Key.D.setMask(Key.CTRL_MASK);
    
    /** The default directory for saving images.. */
    private static final File DEFAULT_DIR = new File(System.getProperty("user.dir") + Var.FS + "user_runs" + Var.FS);
    static {
        DEFAULT_DIR.mkdirs();
    }

    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    // Swing GUI variables.
    /** The frame used for the visualizer. */
    private final JFrame frame;
    /** The canvas being drawn on. */
    private final Canvas canvas;
    /** The label showing the image count. */
    private final JLabel label;
    
    // Concurrent variables.
    private final Lock lock = new ReentrantLock();
    
    // Data variables.
    /** List storing the sets of points to draw. */
    private List<Iterable<Vector>> points = List.of();
    /** List storing the colors of the sets of points to draw. */
    private List<Paint> pointColors = ArrayTools.asList(null, new Color(170, 158, 78), new Color(0, 255, 199),
            new Color(201, 7, 255), new Color(255, 123, 118));
    /** List storing the sets of edges to draw. */
    private List<Iterable<Edge>> edges = List.of();
    /** List storing the colors of the sets of edges to draw. */
    private List<Paint> edgeColors = ArrayTools.asList(null, new Color(255, 0, 0), new Color(0, 152, 35),
            new Color(86, 5, 255), new Color(100, 255, 0));
    /** List storing the labels of the points to draw. */
    private List<Iterable<String>> labels = List.of();
    
    // State variables during redrawing. Reading/writing should only be done by the thread
    // currently redrawing the image.
    private double minX = Integer.MIN_VALUE;
    private double maxX = Integer.MAX_VALUE;
    private double minY = Integer.MIN_VALUE;
    private double maxY = Integer.MAX_VALUE;
    
    private List<BufferedImage> imgs = new ArrayList<>();
    private int imgsIndex = -1;
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new visualizer.
     */
    public Visualizer() {
        frame = new JFrame("testing frame");
        canvas = new Canvas(frame);
        canvas.setLocation(0, 0);
        
        label = new JLabel("0 / 0");
        label.setSize(100, 20);
        canvas.add(label);
        label.setLocation(0, 0);

        KeyAdapter kl = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                Key eKey = new Key(e);
                if (NEXT_KEY.equals(eKey)) {
                    setImg(imgsIndex + 1);
                } else if (FAST_NEXT_KEY.equals(eKey)) {
                    setImg(imgsIndex + 10);
                }else if (PREV_KEY.equals(eKey)) {
                    setImg(imgsIndex - 1);
                } else if (FAST_PREV_KEY.equals(eKey)) {
                    setImg(imgsIndex - 10);
                }  else if (FIRST_KEY.equals(eKey)) {
                    setImg(0);
                } else if (LAST_KEY.equals(eKey)) {
                    setImg(Integer.MAX_VALUE);
                } else if (SAVE_KEY.equals(eKey)) {
                    new Thread(() -> save(), "save-thread").start();
                } else if (DEL_KEY.equals(eKey)) {
                    deleteAll();
                }
            }
        };
        frame.addKeyListener(kl);
        
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setSize(960, 540);
            frame.repaint();
        });
    }
    
    /**
     * Creates a new visualizer from the given point and edge sets.
     * 
     * @param points The initial point set.
     * @param edges  The initial edge set.
     */
    public Visualizer(List<Iterable<Vector>> points, List<Iterable<Edge>> edges) {
        this();
        this.points = points;
        this.edges = edges;
    }
    
    /**
     * Creates a new visualizer from the given data.
     * 
     * @param points      The initial point set.
     * @param pointColors The colors of the points.
     * @param edges       The initial edge set.
     * @param edgeColors  The colors of the edges.
     */
    public Visualizer(List<Iterable<Vector>> points, List<Paint> pointColors,
                      List<Iterable<Edge>> edges, List<Paint> edgeColors) {
        this(points, edges);
        this.pointColors = pointColors;
        this.edgeColors = edgeColors;
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Sets the index of the images to show.
     * 
     * @param i The new index.
     */
    private void setImg(int i) {
        lock.lock();
        try {
            if (imgs.size() == 0) return;
            if (i < 0) i = 0;
            else if (i > imgs.size() - 1) i = imgs.size() - 1;
            if (i != imgsIndex) {
                label.setText((i+1) + " / " + imgs.size());
                canvas.setCanvas(imgs.get(imgsIndex = i));
            }
            
        } finally {
            lock.unlock();
        }
        SwingUtilities.invokeLater(() -> canvas.repaint());
    }

    /**
     * Deletes all generated images.
     */
    private void deleteAll() {
        lock.lock();
        try {
            imgs.clear();
            imgsIndex = -1;
            
        } finally {
            lock.unlock();
        }
    }

    /**
     * Saves all images to a folder.
     * The starting folder is {@link #DEFAULT_DIR}.
     */
    private void save() {
        JFileChooser chooser = new JFileChooser();
        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directories";
            }
        };
        String dirName;
        File[] files = DEFAULT_DIR.listFiles();
        if (files == null) {
            dirName = "run_0000" + Var.FS;
            
        } else {
            int run = 0;
            List<String> fileNames = new ArrayList<>(files.length);
            for (File file : files) {
                if (ff.accept(file)) fileNames.add(file.getName());
            }
            String postFix = "run_0000";
            Collections.sort(fileNames, String.CASE_INSENSITIVE_ORDER);
            for (String name : fileNames) {
                if (name.startsWith(postFix)) {
                    postFix = "run_" + MultiTool.fillZero(++run, 4);
                }
            }
            dirName = postFix + Var.FS;
        }
        
        File guessedDir = new File(DEFAULT_DIR.getPath() + Var.FS + dirName);

        guessedDir.mkdirs();
        chooser.setFileFilter(ff);
        chooser.setCurrentDirectory(DEFAULT_DIR);
        chooser.setSelectedFile(new File(dirName));
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        File selectedDir;
        do {
            if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
                guessedDir.delete();
                return;
            }
            if (!(selectedDir = chooser.getSelectedFile()).exists()) break;
            int option = JOptionPane.showConfirmDialog(frame,
                    "Are you sure that you want to overwrite all png-images in the directory "
                            + selectedDir.getName() + "?");
            if (option == JOptionPane.OK_OPTION) {
                File[] dirFiles = selectedDir.listFiles();
                if (dirFiles == null) break;
                for (File file : dirFiles) {
                    if (file.getName().endsWith("png")) {
                        file.delete();
                    }
                }
                break;
            }
        } while (true);
        if (!selectedDir.equals(guessedDir)) {
            guessedDir.delete();
        }
        
        selectedDir.mkdirs();
        String prefix = selectedDir.getPath();
        if (!prefix.endsWith(Var.FS)) prefix += Var.FS;
        
        lock.lock();
        try {
            int i = 0;
            int idLength = (int) Math.ceil(Math.log10(imgs.size()));
            for (BufferedImage img : imgs) {
                File file = new File(prefix + MultiTool.fillZero(i++, idLength) + ".png");
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                    ImageIO.write(img, "png", bos);
                    
                } catch (IOException e) {
                    Logger.write(e);
                }
            }
            
        } finally {
            lock.unlock();
        }
    }

    /**
     * Redraws the image on the canvas.
     */
    public synchronized void redraw() {
        Logger.write("Draw image " + (imgs.size() + 1));
        if (MultiTool.maxFreeMemory() < 100*MultiTool.MB) {
            Runtime r = Runtime.getRuntime();
            Logger.write(new String[] {
                    "**************************************",
                    "**  CRITICAL  MEMORY  WARNING  !!!  **",
                    "**************************************",
                    "Memory available: " + MultiTool.memToString(MultiTool.maxFreeMemory()) + " / " + 
                            MultiTool.memToString(r.maxMemory()),
                    "Drawing of image is aborted."
            }, Logger.Type.WARNING);
            return;
        }
        final BufferedImage rendering = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        int index= 0;
        for (Iterable<Vector> col : points) {
            for (Vector vec : col) {
                index++;
                minX = Math.min(minX, vec.x());
                maxX = Math.max(maxX, vec.x());
                minY = Math.min(minY, vec.y());
                maxY = Math.max(maxY, vec.y());
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return;
        }
        if (maxX == minX) {
            minX -= 50;
            maxX += 50;
        }
        if (maxY == minY) {
            minY -= 50;
            maxY += 50;
        }
        
        Graphics2D g2d = rendering.createGraphics();
        g2d.setPaint(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw grid lines.
        double dx = maxX - minX;
        double dy = maxY - minY;
        double gridX = dx / 20; //(int) Math.pow(5, ((int) (Math.log(dx) / Math.log(5)) - 1));
        double gridY = dy / 20; //(int) Math.pow(5, ((int) (Math.log(dy) / Math.log(5)) - 1));
        
        g2d.setStroke(GRID_STROKE);
        for (int i = 0; i*gridX <= maxX; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            int x = sci(i*gridX, true);
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        for (int i = 1; -i*gridX >= minX; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            int x = sci(-i*gridX, true);
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        for (int i = 0; i*gridY <= maxY; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            int y = sci(i*gridY, false);
            g2d.drawLine(0, y, WIDTH, y);
        }
        for (int i = 1; -i*gridY >= minY; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            int y = sci(-i*gridY, false);
            g2d.drawLine(0, y, WIDTH, y);
        }
        
        // Draw edges.
        g2d.setStroke(EDGE_STROKE);
        for (int i = 0; i < edges.size(); i++) {
            if (i < edgeColors.size() && edgeColors.get(i) != null) g2d.setPaint(edgeColors.get(i));
            else g2d.setPaint(DEFAULT_EDGE_COLOR);
            for (Edge e : edges.get(i)) {
                g2d.drawLine(sci(e.v1().x(), true), sci(e.v1().y(), false),
                        sci(e.v2().x(), true), sci(e.v2().y(), false));
            }
        }
        
        // Draw points.
        g2d.setStroke(new BasicStroke(1));
        double dTrans = POINT_SIZE / 2.0;
        for (int i = 0; i < points.size(); i++) {
            if (i < pointColors.size() && pointColors.get(i) != null) g2d.setPaint(pointColors.get(i));
            else g2d.setPaint(DEFAULT_POINT_COLOR);
            for (Vector v : points.get(i)) {
                g2d.fillOval((int) (sci(v.x(), true) - dTrans), (int) (sc(v.y(), false) - dTrans),
                        POINT_SIZE, POINT_SIZE);
            }
        }
        
        // Draw labels.
        g2d.setStroke(new BasicStroke(1));
        g2d.setFont(DEFAULT_FONT);
        {
            FontMetrics fm = g2d.getFontMetrics();
            int dh = fm.getDescent();//fm.getAscent() - fm.getHeight();
            for (int i = 0; i < points.size(); i++) {
                if (i < pointColors.size() && pointColors.get(i) != null) {
                    Paint p = pointColors.get(i);
                    if (p instanceof Color) {
                        g2d.setPaint(invertColor((Color) p));
                    } else {
                        g2d.setPaint(p);
                    }
                    
                } else g2d.setPaint(invertColor((Color) DEFAULT_POINT_COLOR));
                
                Iterator<String> lIt = (i < labels.size() ? labels.get(i).iterator() : labelGenerator());
                Iterator<Vector> vIt = points.get(i).iterator();
                while (lIt.hasNext() && vIt.hasNext()) {
                    String label = lIt.next();
                    Vector v = vIt.next();
                    int width = fm.stringWidth(label);
                    g2d.drawString(label, sci(v.x(), true) - width/2, sci(v.y(), false) + dh);
                }
            }
        }
        
        g2d.dispose();
        
        lock.lock();
        try {
            imgs.add(rendering);
            if (imgsIndex == imgs.size() - 2 || imgs.size() == 1) setImg(Integer.MAX_VALUE);
            else if (imgsIndex < 0) setImg(0);
            else setImg(imgsIndex);
            
        } finally {
            lock.unlock();
        }
        canvas.repaint();
    }

    /**
     * @return A generator which generates labels.
     */
    private Iterator<String> labelGenerator() {
        return new Iterator<>() {
            private long i = 0;
            @Override
            public boolean hasNext() {
                return true;
            }
            
            @Override
            public String next() {
                return Long.toString(i++);
            }
        };
    }

    /**
     * Inverts the given color.
     * 
     * @param c The color to invert.
     * 
     * @return The inverted color.
     */
    private Color invertColor(Color c) {
        return new Color(
                255 - c.getRed(),
                255 - c.getGreen(),
                255 - c.getBlue(),
                c.getAlpha());
    }

    /**
     * Integer casted variant of {@link #sc(double, boolean)}.
     *
     * @param point The point to convert.
     * @param width Whether this is an x- or y-coordinate.
     * @return The converted point, casted to an integer.
     */
    private int sci(double point, boolean width) {
        return (int) sc(point, width);
    }
    
    /**
     * Converts a point to canvas coordinates relative to the minimum and maximum allowed values.
     * 
     * @param point The point to convert.
     * @param width Whether this is an x- or y-coordinate.
     * @return The converted point.
     */
    private double sc(double point, boolean width) {
        if (width) {
            double d = maxX - minX;
            double r = d * EMPTY_RATIO;
            return (point - minX + r) / (d + 2*r) * WIDTH;
            
        } else {
            double d = maxY - minY;
            double r = d * EMPTY_RATIO;
            return HEIGHT - (point - minY + r) / (d + 2*r) * HEIGHT;
        }
    }
    
    /**
     * Sets the points to be shown. <br>
     * Each collection in the list can get a separate color by using the {@link #setPointColors(List)} function.
     * If no color is specified, or the color is {@code null}, then the default color is used.
     * 
     * @param points The list of collection of points to be shown.
     */
    public void setPoints(List<Iterable<Vector>> points) {
        this.points = points;
    }

    /**
     * Adds the given point list to the editor.
     *
     * @param pointList The extra point list to display.
     */
    public void addPoint(Iterable<Vector> pointList) {
        if (!(points instanceof ArrayList)) {
            if (points == null) points = new ArrayList<>();
            else points = new ArrayList<>(points);
        }
        points.add(pointList);
    }
    
    /**
     * Sets the colors of the point collections. <br>
     * The n'th element in the list corresponds to the color of the n'th collection of points.
     * The amount of colors doesn't have to match the total number of collections of points.
     * The {@code null} value denotes the default color.
     * 
     * @param pointColors The colors of the points.
     */
    public void setPointColors(List<Paint> pointColors) {
        this.pointColors = pointColors;
    }
    
    /**
     * Sets the edges to be shown. <br>
     * Each collection in the list can get a separate color by using the {@link #setEdgeColors(List)} function.
     * If no color is specified, or the color is {@code null}, then the default color is used.
     *
     * @param edges The list of collection of edges to be shown.
     */
    public void setEdges(List<Iterable<Edge>> edges) {
        this.edges = edges;
    }
    
    /**
     * Adds the given edge list to the editor.
     * 
     * @param edgeList The extra edge list to display.
     */
    public void addEdge(Iterable<Edge> edgeList) {
        if (!(edges instanceof ArrayList)) {
            if (edges == null) edges = new ArrayList<>();
            else edges = new ArrayList<>(edges);
        }
        edges.add(edgeList);
    }
    
    /**
     * Sets the colors of the edge collections. <br>
     * The n'th element in the list corresponds to the color of the n'th collection of edges.
     * The amount of colors doesn't have to match the total number of collections of edges.
     * The {@code null} value denotes the default color.
     *
     * @param edgeColors The colors of the edges.
     */
    public void setEdgeColors(List<Paint> edgeColors) {
        this.edgeColors = edgeColors;
    }

    /**
     * Sets the labels to be shown.
     *
     * @param labelList The list of collection of labels to be shown.
     */
    public void setLabels(List<Iterable<String>> labelList) {
        labels = labelList;
    }
    
    /**
     * Adds the given label list to the editor.
     *
     * @param edgeList The extra edge list to display.
     */
    public void addLabel(Iterable<String> labelList) {
        if (!(labels instanceof ArrayList)) {
            if (labels == null) labels = new ArrayList<>();
            else labels = new ArrayList<>(labels);
        }
        labels.add(labelList);
    }

    /**
     * Clears all current data and sets the new data.
     * 
     * @param data The data to be set for the visualizer.
     */
    public void setData(List<Iterable<? extends BaseInputVertex>> data) {
        clear();
        addData(data);
    }

    /**
     * Adds the given data to the current data.
     * 
     * @param data The data to be added.
     */
    public void addData(List<Iterable<? extends BaseInputVertex>> data) {
        for (Iterable<? extends BaseInputVertex> d : data) {
            Iterable<Vector> vecIt = toVec(d);
            addPoint(vecIt);
            addEdge(connectEdges(vecIt));
            addLabel(toLabel(d));
        }
    }
    

    /**
     * Converts a collection of {@link BaseInputVertex} to a collection of {@link Vector}. <br>
     * This function clones the data, which implies that the data <b>won't</b> be modified when the
     * original collection is modified.
     * 
     * @param in The collection to be converted.
     * @return The underlying data of the input collection.
     * 
     * @see #toVec(Iterable) 
     */
    public static Iterable<Vector> cloneToVec(Iterable<? extends BaseInputVertex> in) {
        List<Vector> out = new ArrayList<>();
        for (BaseInputVertex iv : in) {
            out.add(iv.getV().clone());
        }
        return out;
    }

    /**
     * Converts a collection of {@link BaseInputVertex} to a collection of {@link Vector}. <br>
     * This function does not clone the data, which implies that the data <b>will</b> be updated
     * when the original collection is modified.
     *
     * @param in The collection to be converted.
     * 
     * @return The underlying data of the input collection.
     *
     * @see #cloneToVec(Iterable)
     */
    public static Iterable<Vector> toVec(final Iterable<? extends BaseInputVertex> in) {
        return () -> new Iterator<Vector>() {
            private final Iterator<? extends BaseInputVertex> it = in.iterator();
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Vector next() {
                return it.next().getV();
            }
        };
    }

    /**
     * Converts a collection of {@link BaseInputVertex} to a collection of labels. <br>
     * This function does not clone the data, which implies that the data <b>will</b> be updated
     * when the original collection is modified.
     * 
     * @param in The collection to be converted.
     * 
     * @return The labels matching the given input vertices.
     */
    public static Iterable<String> toLabel(final Iterable<? extends BaseInputVertex> in) {
        return () -> new Iterator<String>() {
            private final Iterator<? extends BaseInputVertex> it = in.iterator();
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            
            @Override
            public String next() {
                return Long.toString(it.next().getId());
            }
        };
    }
    
    /**
     * Converts a collection of {@link OutputEdge} to a collection of {@link Edge}. <br>
     * This function clones the data, which implies that the data <b>won't</b> be modified when the
     * original collection is modified.
     *
     * @param in The collection to be converted.
     * @return The underlying data of the input collection.
     * 
     * @see #toEdge(Iterable)
     */
    public static Iterable<Edge> cloneToEdge(Iterable<OutputEdge> in) {
        List<Edge> out = new ArrayList<>();
        for (OutputEdge e : in) {
            out.add(new Edge(e.getV1().getV().clone(), e.getV2().getV().clone()));
        }
        return out;
    }
    
    /**
     * Converts a collection of {@link OutputEdge} to a collection of {@link Edge}. <br>
     * This function does not clone the data, which implies that the data <b>will</b> be updated
     * when the original collection is modified.
     *
     * @param in The collection to be converted.
     * 
     * @return The underlying data of the input collection.
     * 
     * @see #cloneToEdge(Iterable)
     */
    public static Iterable<Edge> toEdge(final Iterable<OutputEdge> in) {
        return () -> new Iterator<>() {
            private final Iterator<OutputEdge> it = in.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Edge next() {
                OutputEdge e = it.next();
                return new Edge(e.getV1().getV(), e.getV2().getV());
            }
        };
    }

    /**
     * Connects the vectors given in the input in the order they are given.
     * 
     * @param in The vectors to connect.
     * 
     * @return An iterable over the connections between the vectors.
     */
    public static Iterable<Edge> connectEdges(final Iterable<Vector> in) {
        return () -> new Iterator<>() {
            private final Iterator<Vector> it = in.iterator();
            private Vector prev = (it.hasNext() ? it.next() : null);

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Edge next() {
                Vector cur = it.next();
                Edge e = new Edge(prev, cur);
                prev = cur;
                return e;
            }
        };
    }

    /**
     * Clears all point, edge and label data, but leaves the colors and images untouched.
     */
    public void clear() {
        points = List.of();
        edges = List.of();
        labels = List.of();
    }

    /**
     * Clears all data except for the images.
     */
    public void clearAll() {
        clear();
        imgs.clear();
    }
    
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // Init logger.
        Logger.setDefaultLogger(new StreamLogger(System.out));
        
        // Initialize the vector and edge lists.
        final int amt = 25;
        List<Vector> vecs1 = new ArrayList<>(amt);
        List<Vector> vecs2 = new ArrayList<>(amt);
        List<Edge> edges1 = new ArrayList<>(amt);
        List<Edge> edges2 = new ArrayList<>(amt);
        List<Edge> edges3 = new ArrayList<>(amt);
        
        // Initialize the visualizer
        Visualizer vis = new Visualizer(
                ArrayTools.asList(vecs1, vecs2),
                ArrayTools.asList(edges1, edges2, edges3)
        );
        
        // Generating test data set.
        for (int i = 0; i < amt; i++) {
            double y2 = Math.pow(i - (amt - 1)/2.0, 2);
            vecs1.add(new Vector(i, y2 + 10));
            vecs2.add(new Vector(i, -y2 - 10));
            if (i > 0) {
                if (i % 2 == 0) edges3.add(new Edge(vecs1.get(i - 1), vecs2.get(i)));
                else edges3.add(new Edge(vecs2.get(i - 1), vecs1.get(i)));
                edges1.add(new Edge(vecs1.get(i - 1), vecs1.get(i)));
                edges2.add(new Edge(vecs2.get(i - 1), vecs2.get(i)));
            }
            vis.redraw();
        }
        
        // The colors used.
        Color cp1 = new Color(255, 200, 0);
        Color cp2 = new Color(0, 255, 255);
        Color ce1 = new Color(255, 0, 0);
        Color ce2 = new Color(0, 255, 0);
        Color ce3 = new Color(0, 0, 255);
    }
    
    
}
