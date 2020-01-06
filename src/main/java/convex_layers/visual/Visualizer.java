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
 *   <tr><td>CTRL+D</td><td>Delete images</td></tr>
 * </table>
 */
public class Visualizer
        extends AbstractVisual {
    protected static final int WIDTH = 1920;
    protected static final int HEIGHT = 1080;
    
    // Function key constants.
    private static final Key NEXT_KEY = Key.RIGHT;
    private static final Key FAST_NEXT_KEY = Key.RIGHT.setMask(Key.CTRL_MASK);
    private static final Key PREV_KEY = Key.LEFT;
    private static final Key FAST_PREV_KEY = Key.LEFT.setMask(Key.CTRL_MASK);
    private static final Key FIRST_KEY = Key.HOME;
    private static final Key LAST_KEY = Key.END;
    private static final Key SAVE_KEY = Key.S.setMask(Key.CTRL_MASK);
    private static final Key DEL_KEY = Key.D.setMask(Key.CTRL_MASK);
    
    /** The default directory for saving images. */
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
        openCounter.getAndIncrement();
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
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                int amt = openCounter.decrementAndGet();
                if (amt <= 0) {
                    System.exit(0);
                }
            }
        });
        
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            frame.setSize(960, 540);
            frame.repaint();
        });
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
    
    @Override
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
        Graphics2D g2d = rendering.createGraphics();
        redraw(g2d, 1, WIDTH, HEIGHT);
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
    
    @Override
    public void clearAll() {
        super.clearAll();
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
        Visualizer vis = new Visualizer();
        vis.addPoint(vecs1);
        vis.addPoint(vecs2);
        vis.addEdge(edges1);
        vis.addEdge(edges2);
        vis.addEdge(edges3);
        
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
