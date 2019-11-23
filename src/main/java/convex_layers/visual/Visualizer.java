package convex_layers.visual;

import convex_layers.InputVertex;
import convex_layers.OutputEdge;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import tools.MultiTool;
import tools.data.array.ArrayTools;
import tools.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
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
    private static final int POINT_SIZE = 20;
    private static final Stroke EDGE_STROKE = new BasicStroke(5);
    private static final Stroke GRID_STROKE = new BasicStroke(3);
    private static final double EMPTY_RATIO = 0.05;
    
    private final JFrame frame;
    private final Canvas canvas;
    
    // Variables used in concurrent redrawing.
    private final Lock lock = new ReentrantLock();
    private final Condition beginExe = lock.newCondition();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isWaiting = new AtomicBoolean(false);
    private final AtomicInteger running = new AtomicInteger(0);
    
    // Data variables.
    private List<Collection<Vector>> points = List.of();
    private List<Paint> pointColors = ArrayTools.asList(null, new Color(255, 200, 0), new Color(0, 255, 255),
            new Color(255, 0, 255));
    private List<Collection<Edge>> edges = List.of();
    private List<Paint> edgeColors = ArrayTools.asList(null, new Color(255, 0, 0), new Color(0, 255, 0),
            new Color(0, 0, 255));
    
    // State variables during redrawing. Reading/writing should only be done by the thread
    // currently redrawing the image.
    private double minX = Integer.MIN_VALUE;
    private double maxX = Integer.MAX_VALUE;
    private double minY = Integer.MIN_VALUE;
    private double maxY = Integer.MAX_VALUE;

    /**
     * Panel used to draw the buffered image on.
     * Also redraws the buffered image when resized.
     */
    private static class Canvas
            extends JPanel {
        private BufferedImage canvas = null;
        Canvas() {
            super(null);
        }
        
        private void setCanvas(BufferedImage canvas) {
            this.canvas = canvas;
        }
        
        private BufferedImage getCanvas() {
            return canvas;
        }
        
        @Override
        public void setBounds(int x, int y, int width, int height) {
            boolean changed = MultiTool.boundsChanged(this, x, y, width, height);
            super.setBounds(x, y, width, height);
            if (changed) this.repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            if (canvas != null) g2d.drawImage(canvas, 0, 0, getWidth(), getHeight(), null);
        }
        
        
    }
    
    public Visualizer() {
        frame = new JFrame("testing frame");
        canvas = new Canvas();
        frame.add(canvas);
        canvas.setLocation(0, 0);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Insets in = frame.getInsets();
                canvas.setBounds(0, 0,
                        frame.getWidth() - in.left - in.right,
                        frame.getHeight() - in.top - in.bottom);
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(960, 540);
        frame.repaint();
    }
    
    public Visualizer(List<Collection<Vector>> points, List<Collection<Edge>> edges) {
        this();
        this.points = points;
        this.edges = edges;
    }
    
    public Visualizer(List<Collection<Vector>> points, List<Paint> pointColors,
                      List<Collection<Edge>> edges, List<Paint> edgeColors) {
        this(points, edges);
        this.pointColors = pointColors;
        this.edgeColors = edgeColors;
    }
    
    /**
     * Redraws the image on the canvas. <br>
     * Since this might be a computation insensitive task, it is executed on another thread.
     */
    public void redraw() {
        new Thread(() -> {
            lock.lock();
            try {
                if (isWaiting.get()) {
                    Logger.write("Invoked redraw, but previous cycle not yet finished.", Logger.Type.INFO);
                    return;
                }
                while (isRunning.get()) {
                    try {
                        isWaiting.set(true);
                        beginExe.await();
                        isWaiting.set(false);

                    } catch (InterruptedException e) {
                        Logger.write(e);
                    }
                }
                
            } finally {
                lock.unlock();
            }
            
            redraw0();
            
            lock.lock();
            try {
                if (isWaiting.get()) beginExe.signal();
                
            } finally {
                lock.unlock();
            }
            
        }, "visual-runner").start(); 
    }

    /**
     * Implementation of the redraw function.
     * 
     * @see #redraw()
     */
    private void redraw0() {
        final BufferedImage rendering = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        for (Collection<Vector> col : points) {
            for (Vector vec : col) {
                minX = Math.min(minX, vec.x());
                maxX = Math.max(maxX, vec.x());
                minY = Math.min(minY, vec.y());
                maxY = Math.max(maxY, vec.y());
            }
        }
        
        System.out.println(minX + ", " + maxX + ", " + minY + ", " + maxY);
        
        Graphics2D g2d = rendering.createGraphics();
        
        // Draw grid lines.
        double dx = maxX - minX;
        double dy = maxY - minY;
        int gridX = (int) Math.pow(5, ((int) (Math.log(dx) / Math.log(5)) - 1));
        int gridY = (int) Math.pow(5, ((int) (Math.log(dy) / Math.log(5)) - 1));
        System.out.println(gridX);
        System.out.println(gridY);
        
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
        canvas.setCanvas(rendering);
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
    public void setPoints(List<Collection<Vector>> points) {
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
    public void setEdges(List<Collection<Edge>> edges) {
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
     * Additionally clones the data.
     * 
     * @param in The collection to be converted.
     * @return The underlying data of the input collection.
     */
    public static Collection<Vector> toVec(Collection<InputVertex> in) {
        List<Vector> out = new ArrayList<>(in.size());
        for (InputVertex iv : in) {
            out.add(iv.getV().clone());
        }
        return out;
    }
    
    /**
     * Converts a collection of {@link OutputEdge} to a collection of {@link Edge}. <br>
     * Additionally clones the data.
     *
     * @param in The collection to be converted.
     * @return The underlying data of the input collection.
     */
    public static Collection<Edge> toEdge(Collection<OutputEdge> in) {
        List<Edge> out = new ArrayList<>(in.size());
        for (OutputEdge e : in) {
            out.add(new Edge(e.getV1().getV().clone(), e.getV2().getV().clone()));
        }
        return out;
    }
    
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // Generating test data set.
        final int amt = 25;
        List<Vector> vecs1 = new ArrayList<>(amt);
        List<Vector> vecs2 = new ArrayList<>(amt);
        for (int i = 0; i < amt; i++) {
            double y2 = Math.pow(i - (amt - 1)/2.0, 2);
            vecs1.add(new Vector(i, y2 + 10));
            vecs2.add(new Vector(i, -y2 - 10));
        }
        List<Edge> edges1 = new ArrayList<>(amt);
        List<Edge> edges2 = new ArrayList<>(amt);
        List<Edge> edges3 = new ArrayList<>(amt);
        for (int i = 0; i < amt - 1; i++) {
            edges1.add(new Edge(vecs1.get(i), vecs1.get(i + 1)));
            edges2.add(new Edge(vecs2.get(i), vecs2.get(i + 1)));
            edges3.add(new Edge(vecs1.get(i), vecs2.get(i + 1)));
        }
        
        //System.out.println(vecs1.toString().replaceAll(", ", "," + Var.LS));
        //System.out.println(vecs2.toString().replaceAll(", ", "," + Var.LS));
        //System.out.println(edges1.toString().replaceAll("\\}, \\{", "}," + Var.LS + "{"));
        //System.out.println(edges2.toString().replaceAll("\\}, \\{", "}," + Var.LS + "{"));
        //System.out.println(edges3.toString().replaceAll("\\}, \\{", "}," + Var.LS + "{"));
        
        // The colors used.
        Color cp1 = new Color(255, 200, 0);
        Color cp2 = new Color(0, 255, 255);
        Color ce1 = new Color(255, 0, 0);
        Color ce2 = new Color(0, 255, 0);
        Color ce3 = new Color(0, 0, 255);
        
        // Displaying the data set.
        Visualizer vis = new Visualizer();
        vis.setPoints(ArrayTools.asList(vecs1, vecs2));
        vis.setEdges(ArrayTools.asList(edges1, edges2, edges3));
        //vis.setPointColors(ArrayTools.asList(cp1, cp2));
        //vis.setEdgeColors(ArrayTools.asList(ce1, ce2, ce3));
        vis.redraw();
    }
    
    
}
