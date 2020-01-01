package convex_layers.visual;

import convex_layers.math.Vector;
import tools.MultiTool;
import tools.Var;
import tools.event.Key;
import tools.log.Logger;
import tools.log.NullLogger;
import tools.log.StreamLogger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Generator used to easily generate point data sets. <br>
 * Key bindings:
 * <table border="1">
 *   <tr><th>Key</th><th>Function</th></tr>
 *   <tr><td>S+ctrl</td><td>Saves the current displayed dataset.</td></tr>
 *   <tr><td>Q+crtl</td><td>Erases all points on the screen.</td></tr>
 * </table>
 */
public class VisualGenerator {
    
    /* ----------------------------------------------------------------------
     * Constants.
     * ----------------------------------------------------------------------
     */
    /** The width of the canvas. */
    private static final int WIDTH = 1920;
    /** The height of the canvas. */
    private static final int HEIGHT = 1080;
    /** The size of a single point. */
    private static final int POINT_SIZE = 20;
    /** The default directory used to store the data at. */
    private static final File DEFAULT_DIR = new File(System.getProperty("user.dir") + Var.FS + "gen_data" + Var.FS);
    
    /** The key used to open the save menu. */
    private static final Key SAVE_KEY = Key.S.setMask(Key.CTRL_MASK);
    /** The key used to delete the current nodes. */
    private static final Key DEL_KEY = Key.Q.setMask(Key.CTRL_MASK);
    
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The frame used for the application. */
    private final JFrame frame;
    /** The canvas used to paint on. */
    private final Canvas canvas;
    /** The list of points which should be drawn. */
    private final List<Vector> points = new ArrayList<>();
    /** The lock used for concurrent operations. */
    private Lock lock = new ReentrantLock();
    
    
    /* ----------------------------------------------------------------------
     * Static initialization.
     * ----------------------------------------------------------------------
     */
    static {
        if (Logger.getLog() instanceof NullLogger) {
            Logger.setDefaultLogger(new StreamLogger(System.out));
        }
        //noinspection ResultOfMethodCallIgnored
        DEFAULT_DIR.mkdirs();
    }
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new point generation which allows a user to graphically create a new point set.
     */
    public VisualGenerator() {
        frame = new JFrame("point generator");
        canvas = new Canvas(frame);
        canvas.setLocation(0, 0);
        wipeCanvas();
        
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                addPoint(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight());
            }
        };
        canvas.addMouseListener(ma);
        
        KeyListener kl = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                Key eKey = new Key(e, false);
                if (SAVE_KEY.equals(eKey)) {
                    new Thread(() -> save(), "Saving thread").start();
                    
                } else if (DEL_KEY.equals(eKey)) {
                    del();
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
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Adds a point to the dataset.
     * @param x The x-coordinate of the point on the canvas.
     * @param y The y-coordinate of the point on the canvas.
     * @param w The width of the canvas.
     * @param h The height of the canvas.
     */
    private void addPoint(int x, int y, int w, int h) {
        double canvasX = ((double) x * WIDTH) / w;
        double canvasY = ((double) y * HEIGHT) / h;
        new Thread(() -> {
            lock.lock();
            try {
                points.add(new Vector(canvasX, HEIGHT - canvasY));
                
            } finally {
                lock.unlock();
            }
        }).start();
        Graphics2D g2d = (Graphics2D) canvas.getCanvas().getGraphics();
        g2d.setStroke(new BasicStroke(1));
        g2d.setPaint(Color.BLACK);
        g2d.fillOval((int) canvasX - POINT_SIZE/2, (int) canvasY - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
        canvas.repaint();
    }
    
    /**
     * Saves the current point set to a file. <br>
     * <b>WARNING!</b><br>
     * This is a blocking function!
     */
    private void save() {
        JFileChooser chooser = new JFileChooser();
        FileFilter ff = new FileNameExtensionFilter("JSON file", "json");
        chooser.setFileFilter(ff);
        chooser.setCurrentDirectory(DEFAULT_DIR);
        File[] files = DEFAULT_DIR.listFiles();
        if (files != null) {
            String[] fileNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                fileNames[i] = files[i].getName();
            }
            Arrays.sort(fileNames, String.CASE_INSENSITIVE_ORDER);
            
            int id = 0;
            String idStr ="0000";
            for (String name : fileNames) {
                if (name.startsWith(idStr)) {
                    idStr = MultiTool.fillZero(++id, 4, 10);
                }
            }
            
            chooser.setSelectedFile(new File(idStr + "_"
                    + MultiTool.fillZero(points.size(), 4, 10)
                    + ".json"));
        }
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            save(chooser.getSelectedFile());
        } 
    }
    
    /**
     * Saves the current point set to the given. <br>
     * <b>WARNING!</b><br>
     * This is a blocking function!
     */
    private void save(File file) {
        new Thread(() -> {
            lock.lock();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("{\"points\": [");
                int i = 0;
                boolean first = true;
                for (Vector v : points) {
                    if (first) first = false;
                    else bw.write(", ");
                    bw.write(vectorToString(v, i++));
                }
                bw.write("], \"type\": \"Instance\", \"name\": \"user input\"}");
                
            } catch (IOException e) {
                Logger.write(e);
                
            } finally {
                lock.unlock();
            }
            
        }).start();
    }
    
    /**
     * Deletes the current point set.
     */
    private void del() {
        new Thread(() -> {
            lock.lock();
            try {
                points.clear();
                wipeCanvas();
                canvas.repaint();
                
            } finally {
                lock.unlock();
            }
        }).start();
    }
    
    /**
     * Converts a vector to a JSON string.
     * 
     * @param v     The vector to convert.
     * @param index The index of the vector.
     * 
     * @return A JSON string representation of the vector.
     */
    private String vectorToString(Vector v, int index) {
        String[] coords = {
                Double.toString(v.x()),
                Double.toString(v.y())
        };
        
        for (int i = 0; i < coords.length; i++) {
            if (coords[i].contains("\\.")) {
                String[] split = coords[i].split("\\.");
                if (split[1].length() > 2) {
                    coords[i] = split[0] + "." + split[1].substring(0, 2);
                }
            }
        }
        return "{\"i\": " + index + ", \"x\": " + coords[0] + ", \"y\": " + coords[1] + "}";
    }
    
    /**
     * Clears the canvas image.
     */
    private void wipeCanvas() {
        canvas.setCanvas(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));
    }
    
    /**
     * Invokes the program.
     * 
     * @param args Invokation arguments.
     */
    public static void main(String[] args) {
        new VisualGenerator();
    }
    
    
}
