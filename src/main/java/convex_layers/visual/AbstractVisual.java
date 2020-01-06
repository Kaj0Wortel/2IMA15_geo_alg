package convex_layers.visual;

import convex_layers.BaseInputVertex;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import tools.data.array.ArrayTools;
import tools.data.file.FileTree;
import tools.font.FontLoader;
import tools.log.Logger;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract visualizer class which converts the input requests, sets
 * the internal state, and provides an easy-to-use redraw function
 * which redraws the current state.
 */
public abstract class AbstractVisual
        implements Visual {
    
    /* ----------------------------------------------------------------------
     * Constants.
     * ----------------------------------------------------------------------
     */
    // Constants used to tweak the visuals.
    protected static final Paint DEFAULT_POINT_COLOR = new Color(0, 0, 0);
    protected static final Paint DEFAULT_EDGE_COLOR = new Color(0, 182, 255);
    protected static final Paint GRID_COLOR = new Color(200, 200, 200);
    protected static final Paint BIG_GRID_COLOR = new Color(100, 100, 100);
    protected static final Paint BACKGROUND_COLOR = new Color(255, 255, 255, 255);
    protected static final int POINT_SIZE = 40;
    protected static final int EDGE_STROKE_SIZE = 5; //new BasicStroke(5);
    protected static final int GRID_STROKE_SIZE = 3; //new BasicStroke(3);
    protected static final double EMPTY_RATIO = 0.05;
    protected static final Font DEFAULT_FONT = FontLoader.getFont("Cousine-Bold.ttf", 25);
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    // Data variables.
    /** List storing the sets of points to draw. */
    protected List<Iterable<Vector>> points = List.of();
    /** List storing the colors of the sets of points to draw. */
    protected List<Paint> pointColors = ArrayTools.asList(null, new Color(170, 158, 78), new Color(0, 255, 199),
            new Color(201, 7, 255), new Color(255, 123, 118));
    /** List storing the sets of edges to draw. */
    protected List<Iterable<Edge>> edges = List.of();
    /** List storing the colors of the sets of edges to draw. */
    protected List<Paint> edgeColors = ArrayTools.asList(null, new Color(255, 0, 0), new Color(0, 152, 35),
            new Color(86, 5, 255), new Color(100, 255, 0));
    /** List storing the labels of the points to draw. */
    protected List<Iterable<String>> labels = List.of();
    
    // State variables during redrawing. Reading/writing should only be done by the thread
    // currently redrawing the image.
    private double minX = Integer.MIN_VALUE;
    private double maxX = Integer.MAX_VALUE;
    private double minY = Integer.MIN_VALUE;
    private double maxY = Integer.MAX_VALUE;
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * @return A generator which generates labels.
     */
    protected Iterator<String> labelGenerator() {
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
    protected Color invertColor(Color c) {
        return new Color(
                255 - c.getRed(),
                255 - c.getGreen(),
                255 - c.getBlue(),
                c.getAlpha());
    }

    /**
     * Redraws the current state on the given graphics object.
     * 
     * @param g2d    The graphics object to draw on.
     * @param scale  The relative size of the elements to draw.
     * @param width  The width of the image.
     * @param height The height of the image.
     */
    protected synchronized void redraw(Graphics2D g2d, double scale, int width, int height) {
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        for (Iterable<Vector> col : points) {
            for (Vector vec : col) {
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
        
        g2d.setPaint(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, width, height);
        
        // Draw grid lines.
        double dx = maxX - minX;
        double dy = maxY - minY;
        double gridX = dx * scale / 20; //(int) Math.pow(5, ((int) (Math.log(dx) / Math.log(5)) - 1));
        double gridY = dy * scale / 20; //(int) Math.pow(5, ((int) (Math.log(dy) / Math.log(5)) - 1));
        
        g2d.setStroke(new BasicStroke((float) (GRID_STROKE_SIZE * scale)));
        for (int i = 0; i*gridX <= maxX; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            double x = sc(i*gridX, width, height, scale, true);
            g2d.draw(new Line2D.Double(x, 0, x, height));
        }
        for (int i = 1; -i*gridX >= minX; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            double x = sc(-i*gridX, width, height, scale, true);
            g2d.draw(new Line2D.Double(x, 0, x, height));
        }
        for (int i = 0; i*gridY <= maxY; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            double y = sc(i*gridY, width, height, scale, false);
            g2d.draw(new Line2D.Double(0, y, width, y));
        }
        for (int i = 1; -i*gridY >= minY; i++) {
            if (i % 5 == 0) g2d.setPaint(BIG_GRID_COLOR);
            else g2d.setPaint(GRID_COLOR);
            double y = sc(-i*gridY, width, height, scale, false);
            g2d.draw(new Line2D.Double(0, y, width, y));
        }
        
        // Draw edges.
        g2d.setStroke(new BasicStroke((float) (EDGE_STROKE_SIZE * scale)));
        for (int i = 0; i < edges.size(); i++) {
            g2d.setPaint(DEFAULT_EDGE_COLOR);
            if (!edgeColors.isEmpty() && edgeColors.get(i % edgeColors.size()) != null) {
                Paint c = edgeColors.get(i % edgeColors.size());
                if (c != null) g2d.setPaint(c);
            }
            
            for (Edge e : edges.get(i)) {
                g2d.draw(new Line2D.Double(
                        sc(e.v1().x(), width, height, scale, true),
                        sc(e.v1().y(), width, height, scale, false),
                        sc(e.v2().x(), width, height, scale, true),
                        sc(e.v2().y(), width, height, scale, false)
                ));
            }
        }
        
        // Draw points.
        g2d.setStroke(new BasicStroke((float) (1 * scale)));
        double dTrans = POINT_SIZE * scale / 2.0;
        for (int i = 0; i < points.size(); i++) {
            g2d.setPaint(DEFAULT_POINT_COLOR);
            if (!pointColors.isEmpty()) {
                Paint c = pointColors.get(i % pointColors.size());
                if (c != null) g2d.setPaint(c);
            }
            for (Vector v : points.get(i)) {
                g2d.fill(new Arc2D.Double(
                        sc(v.x(), width, height, scale, true) - dTrans,
                        sc(v.y(), width, height, scale, false) - dTrans,
                        POINT_SIZE * scale, POINT_SIZE * scale, 0, 360, Arc2D.CHORD
                ));
            }
        }
        
        // Draw labels.
        g2d.setStroke(new BasicStroke((float) (0.95 * scale)));
        g2d.setFont(DEFAULT_FONT.deriveFont((float) (DEFAULT_FONT.getSize() * scale)));
        {
            FontMetrics fm = g2d.getFontMetrics();
            float dh = (float) ((POINT_SIZE * scale - fm.getHeight()) / 2 + fm.getMaxAscent());
            for (int i = 0; i < points.size(); i++) {
                {
                    Paint c = null;
                    if (!pointColors.isEmpty()) {
                        c = pointColors.get(i % pointColors.size());
                    }
                    if (c == null) c = DEFAULT_POINT_COLOR;
                    g2d.setPaint(invertColor((Color) c));
                }

                Iterator<String> lIt = (i < labels.size() ? labels.get(i).iterator() : labelGenerator());
                Iterator<Vector> vIt = points.get(i).iterator();
                while (lIt.hasNext() && vIt.hasNext()) {
                    String label = lIt.next();
                    Vector v = vIt.next();
                    float w = fm.stringWidth(label);
                    g2d.drawString(label,
                            (float) (sc(v.x(), width, height, scale, true) - w/2),
                            (float) (sc(v.y(), width, height, scale, false) - dTrans + dh)
                    );
                }
            }
        }
    }
    
//    /**
//     * Integer casted variant of {@link #sc(double, int, int, double, boolean)}.
//     *
//     * @param point The point to convert.
//     * @param width Whether this is an x- or y-coordinate.
//     * @return The converted point, casted to an integer.
//     */
//    private int sci(double point, int w, int h, double scale, boolean width) {
//        return (int) sc(point, w, h, scale, width);
//    }
    
    /**
     * Converts a point to canvas coordinates relative to the minimum and maximum allowed values.
     *
     * @param point The point to convert.
     * @param width Whether this is an x- or y-coordinate.
     * @return The converted point.
     */
    private double sc(double point, int w, int h, double scale, boolean width) {
        if (width) {
            double d = maxX - minX;
            double r = d * EMPTY_RATIO;
            return (point - minX + r) / (d + 2*r) * w;
            
        } else {
            double d = maxY - minY;
            double r = d * EMPTY_RATIO;
            return h - (point - minY + r) / (d + 2*r) * h;
        }
    }
    
    @Override
    public void setPoints(List<Iterable<Vector>> points) {
        this.points = points;
    }
    
    @Override
    public void addPoint(Iterable<Vector> pointList) {
        if (!(points instanceof ArrayList)) {
            if (points == null) points = new ArrayList<>();
            else points = new ArrayList<>(points);
        }
        points.add(pointList);
    }
    
    @Override
    public void addPoint(Iterable<Vector> pointList, Iterable<String> labelList) {
        addPoint(pointList);
        if (labels == null) labels = new ArrayList<>();
        else if (!(labels instanceof ArrayList)) labels = new ArrayList<>(labels);
        while (labels.size() < points.size()) {
            labels.add(null);
        }
        labels.set(points.size() - 1, labelList);
    }
    
    @Override
    public void addPoint(Iterable<Vector> pointList, Iterable<String> labelList, Paint paint) {
        addPoint(pointList, labelList);
        if (pointColors == null) pointColors = new ArrayList<>();
        else if (!(pointColors instanceof ArrayList)) pointColors = new ArrayList<>(pointColors);
        while (pointColors.size() < points.size()) {
            pointColors.add(null);
        }
        pointColors.set(points.size() - 1, paint);
    }
    
    @Override
    public void setPointColors(List<Paint> pointColors) {
        this.pointColors = pointColors;
    }
    
    @Override
    public void setEdges(List<Iterable<Edge>> edges) {
        this.edges = edges;
    }
    
    @Override
    public void addEdge(Iterable<Edge> edgeList) {
        if (!(edges instanceof ArrayList)) {
            if (edges == null) edges = new ArrayList<>();
            else edges = new ArrayList<>(edges);
        }
        edges.add(edgeList);
    }
    
    @Override
    public void addEdge(Iterable<Edge> edgeList, Paint paint) {
        addEdge(edgeList);
        if (edgeColors == null) edgeColors = new ArrayList<>();
        else if (!(edgeColors instanceof ArrayList)) edgeColors = new ArrayList<>(edgeColors);
        while (edgeColors.size() < edges.size()) {
            edgeColors.add(null);
        }
        edgeColors.set(edges.size() - 1, paint);
    }
    
    @Override
    public void setEdgeColors(List<Paint> edgeColors) {
        this.edgeColors = edgeColors;
    }
    
    @Override
    public void setLabels(List<Iterable<String>> labelList) {
        labels = labelList;
    }
    
    @Override
    public void addLabel(Iterable<String> labelList) {
        if (!(labels instanceof ArrayList)) {
            if (labels == null) labels = new ArrayList<>();
            else labels = new ArrayList<>(labels);
        }
        labels.add(labelList);
    }
    
    @Override
    public void setData(List<Iterable<? extends BaseInputVertex>> data) {
        clear();
        addData(data);
    }
    
    @Override
    public void addData(List<Iterable<? extends BaseInputVertex>> data) {
        for (Iterable<? extends BaseInputVertex> d : data) {
            Iterable<Vector> vecIt = Visual.toVec(d);
            addPoint(vecIt);
            addEdge(Visual.connectEdges(vecIt));
            addLabel(Visual.toLabel(d));
        }
    }
    
    @Override
    public void clear() {
        points = List.of();
        edges = List.of();
        labels = List.of();
    }
    
    @Override
    public void clearAll() {
        clear();
        pointColors = List.of();
        edgeColors = List.of();
    }
    
    
}
