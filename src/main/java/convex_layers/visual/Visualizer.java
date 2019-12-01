package convex_layers.visual;

import convex_layers.InputVertex;
import convex_layers.OutputEdge;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import tools.MultiTool;
import tools.Var;
import tools.data.array.ArrayTools;
import tools.data.file.DirFileTree;
import tools.data.file.FileTree;
import tools.data.file.TreeFile;
import tools.event.Key;
import tools.log.Logger;
import tools.log.StreamLogger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class can visualize different sets of points and edges with different colors, auto-scaling and
 * auto adjusted grid.
 */
public class Visualizer {
    // Constants used to tweak the visuals.
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final Paint DEFAULT_POINT_COLOR = new Color(0, 0, 0);
    private static final Paint DEFAULT_EDGE_COLOR = new Color(0, 154, 255);
    private static final Paint GRID_COLOR = new Color(200, 200, 200);
    private static final Paint BIG_GRID_COLOR = new Color(100, 100, 100);
    private static final Paint BACKGROUND_COLOR = new Color(255, 255, 255, 255);
    private static final int POINT_SIZE = 20;
    private static final Stroke EDGE_STROKE = new BasicStroke(5);
    private static final Stroke GRID_STROKE = new BasicStroke(3);
    private static final double EMPTY_RATIO = 0.05;
    
    private static final Key NEXT_KEY = Key.RIGHT.newOnKeyRelease(false);
    private static final Key PREV_KEY = Key.LEFT.newOnKeyRelease(false);
    private static final Key FIRST_KEY = Key.HOME.newOnKeyRelease(false);
    private static final Key LAST_KEY = Key.END.newOnKeyRelease(false);
    private static final Key SAVE_KEY = Key.S.newOnKeyRelease(false).setMask(Key.CTRL_MASK);
    
    private static final File DEFAULT_DIR = new File(System.getProperty("user.dir") + Var.FS + "user_runs" + Var.FS);
    static {
        DEFAULT_DIR.mkdirs();
    }
    
    private final JFrame frame;
    private final Canvas canvas;
    private final JLabel label;
    
    private final Lock lock = new ReentrantLock();
    
    // Data variables.
    private List<Iterable<Vector>> points = List.of();
    private List<Paint> pointColors = ArrayTools.asList(null, new Color(255, 200, 0), new Color(0, 255, 255),
            new Color(255, 0, 255));
    private List<Iterable<Edge>> edges = List.of();
    private List<Paint> edgeColors = ArrayTools.asList(null, new Color(255, 0, 0), new Color(0, 255, 0),
            new Color(0, 0, 255));
    
    // State variables during redrawing. Reading/writing should only be done by the thread
    // currently redrawing the image.
    private double minX = Integer.MIN_VALUE;
    private double maxX = Integer.MAX_VALUE;
    private double minY = Integer.MIN_VALUE;
    private double maxY = Integer.MAX_VALUE;
    
    private List<BufferedImage> imgs = new ArrayList<>();
    private int imgsIndex = 0;

    
    public Visualizer() {
        frame = new JFrame("testing frame");
        canvas = new Canvas(frame);
        canvas.setLocation(0, 0);
        
        label = new JLabel("0 / 0");
        label.setSize(100, 20);
        canvas.add(label);
        label.setLocation(0, 0);
        
        KeyListener kl = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                Key eKey = new Key(e);
                if (NEXT_KEY.equals(eKey)) {
                    setImg(imgsIndex + 1);
                } else if (PREV_KEY.equals(eKey)) {
                    setImg(imgsIndex - 1);
                } else if (FIRST_KEY.equals(eKey)) {
                    setImg(0);
                } else if (LAST_KEY.equals(eKey)) {
                    setImg(Integer.MAX_VALUE);
                } else if (SAVE_KEY.equals(eKey)) {
                    new Thread(() -> save(), "save-thread").start();
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
    
    public Visualizer(List<Iterable<Vector>> points, List<Iterable<Edge>> edges) {
        this();
        this.points = points;
        this.edges = edges;
    }
    
    public Visualizer(List<Iterable<Vector>> points, List<Paint> pointColors,
                      List<Iterable<Edge>> edges, List<Paint> edgeColors) {
        this(points, edges);
        this.pointColors = pointColors;
        this.edgeColors = edgeColors;
    }

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
        canvas.repaint();
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
    private void redraw() {
        if (MultiTool.maxFreeMemory() < 100*MultiTool.MB) {
            Logger.write(new String[] {
                    "**************************************",
                    "**  CRITICAL  MEMORY  WARNING  !!!  **",
                    "**************************************",
                    "Memory: " + MultiTool.memToString(MultiTool.memoryInUse()) + " / " + 
                            MultiTool.memToString(MultiTool.maxFreeMemory()),
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
        
        g2d.dispose();
        
        lock.lock();
        try {
            imgs.add(rendering);
            if (imgsIndex == imgs.size() - 2) setImg(imgs.size() - 1);
            
        } finally {
            lock.unlock();
        }
        canvas.repaint();
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
     * Converts a collection of {@link InputVertex} to a collection of {@link Vector}. <br>
     * This function clones the data, which implies that the data <b>won't</b> be modified when the
     * original collection is modified.
     * 
     * @param in The collection to be converted.
     * @return The underlying data of the input collection.
     * 
     * @see #toVec(Iterable) 
     */
    public static Iterable<Vector> cloneToVec(Iterable<InputVertex> in) {
        List<Vector> out = new ArrayList<>();
        for (InputVertex iv : in) {
            out.add(iv.getV().clone());
        }
        return out;
    }

    /**
     * Converts a collection of {@link InputVertex} to a collection of {@link Vector}. <br>
     * This function does not clone the data, which implies that the data <b>will</b> be updated
     * when the original collection is modified.
     *
     * @param in The collection to be converted.
     * @return The underlying data of the input collection.
     *
     * @see #cloneToVec(Iterable)
     */
    public static Iterable<Vector> toVec(final Iterable<InputVertex> in) {
        return () -> new Iterator<Vector>() {
            private final Iterator<InputVertex> it = in.iterator();
            
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
