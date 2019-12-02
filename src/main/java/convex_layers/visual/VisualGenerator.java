package convex_layers.visual;

import convex_layers.math.Vector;
import tools.MultiTool;
import tools.Var;
import tools.event.Key;

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
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int POINT_SIZE = 20;
    private static final File DEFAULT_DIR = new File(System.getProperty("user.dir") + Var.FS + "gen_data" + Var.FS);

    private static final Key SAVE_KEY = Key.S.setMask(Key.CTRL_MASK);
    private static final Key DEL_KEY = Key.Q.setMask(Key.CTRL_MASK);
    
    static {
        DEFAULT_DIR.mkdirs();
    }
    
    private final JFrame frame;
    private final Canvas canvas;
    private final List<Vector> points = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    
    
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
            public void keyReleased(KeyEvent e) {
                Key eKey = new Key(e, false);
                if (SAVE_KEY.equals(eKey)) {
                    save();
                    
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
                System.err.println(e);
            } finally {
                lock.unlock();
            }
            
        }).start();
    }
    
    private void del() {
        new Thread(() -> {
            lock.lock();
            try {
                System.out.println("DEL");
                points.clear();
                wipeCanvas();
                canvas.repaint();
                
            } finally {
                lock.unlock();
            }
        }).start();
    }
    
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
    
    private void wipeCanvas() {
        canvas.setCanvas(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));
    }
    
    public static void main(String[] args) {
        new VisualGenerator();
    }
    
    
}
