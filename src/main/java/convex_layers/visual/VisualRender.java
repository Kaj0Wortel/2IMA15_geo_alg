package convex_layers.visual;

import convex_layers.math.Edge;
import convex_layers.math.Vector;
import tools.event.Key;
import tools.log.Logger;
import tools.log.StreamLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class VisualRender
        extends AbstractVisual {
    
    private final JFrame frame;
    private final ZoomPanel zp;


    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    public class ZoomPanel
            extends JPanel {
        
        /* ----------------------------------------
         * Constants.
         * ----------------------------------------
         */
        private static final double DEFAULT_PAN = 30;
        private static final double DEFAULT_ZOOM = 1.1;
        
        
        /* ----------------------------------------
         * Variables.
         * ----------------------------------------
         */
        // Panning and scaling variables.
        /** The current displacement x-coordinate. */
        private double x = 0;
        /** The current displacement y-coordinate. */
        private double y = 0;
        /** The current scaling of the rendering. */
        private double zoom = 1;

        // State variables during redrawing. Reading/writing should only be done by the thread
        // currently redrawing the image.
        private double minX = Integer.MIN_VALUE;
        private double maxX = Integer.MAX_VALUE;
        private double minY = Integer.MIN_VALUE;
        private double maxY = Integer.MAX_VALUE;
        
        
        /* ----------------------------------------
         * Constructors.
         * ----------------------------------------
         */
        public ZoomPanel(JFrame frame) {
            super(null);
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    Key key = new Key(e);
                    if (key.equalsAny(Key.UP, Key.W)) panVertical(-DEFAULT_PAN);
                    else if (key.equalsAny(Key.RIGHT,Key.D)) panHorizontal(DEFAULT_PAN);
                    else if (key.equalsAny(Key.DOWN, Key.S)) panVertical(DEFAULT_PAN);
                    else if (key.equalsAny(Key.LEFT, Key.A)) panHorizontal(-DEFAULT_PAN);
                    else if (key.equals(Key.MINUS)) zoom(1 / DEFAULT_ZOOM);
                    else if (key.equals(Key.EQUAL)) zoom(DEFAULT_ZOOM);
                    else if (key.equals(Key.SPACE)) reset();
                    repaint();
                }
            });
            frame.addMouseWheelListener((e) -> {
                e.getUnitsToScroll();
                e.getScrollAmount();
                double scroll;
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    scroll = e.getUnitsToScroll();
                } else {
                    scroll = e.getPreciseWheelRotation() * e.getScrollAmount(); // TODO: test
                }
                
                if (scroll == 0) return;
                else if (scroll < 0) {
                    scroll = Math.log(-scroll);
                } else {
                    scroll = 1/Math.log(scroll);
                }
                
                zoom(scroll);
                repaint();
            });
        }
        
        
        /* ----------------------------------------
         * Functions.
         * ----------------------------------------
         */
        @Override
        public void setBounds(int x, int y, int width, int height) {
            if (zoom > 1) {
                double dw = getWidth() - width;
                double dh = getHeight() - height;
                this.x = this.x * width / getWidth();
                this.y = this.y * height / getHeight();
                
                super.setBounds(x, y, width, height);
                repaint();
                
            } else {
                super.setBounds(x, y, width, height);
            }
        }
        
        private void zoom(double amt) {
            double oldZoom = zoom;
            zoom *= amt;
            if (zoom > 1 && oldZoom > 1 && false) {
                double ratioX = (this.x) / (getWidth());
                double ratioY = (this.y) / (getHeight());
                Logger.write(ratioX);
                panHorizontal((zoom - oldZoom) * getWidth() * ratioX);
                panVertical((zoom - oldZoom) * getHeight() * ratioY);
            } else {
                panHorizontal((zoom - oldZoom) * getWidth() / 2);
                panVertical((zoom - oldZoom) * getHeight() / 2);
            }
        }
        
        private void panHorizontal(double amt) {
            x -= amt;
        }
        
        private void panVertical(double amt) {
            y -= amt;
        }
        
        private void reset() {
            x = 0;
            y = 0;
            zoom = 1;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(x, y);
            g2d.scale(zoom, zoom);
            redraw(g2d, 1/zoom, getWidth(), getHeight());
        }
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    public VisualRender() {
        frame = new JFrame("Visual render");
        zp = new ZoomPanel(frame);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Insets in = frame.getInsets();
                zp.setBounds(in.left, in.top,
                        frame.getWidth() - in.left - in.right,
                        frame.getHeight() - in.top - in.bottom
                );
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
        SwingUtilities.invokeLater(() -> {
            frame.add(zp);
            frame.setSize(500, 500);
        });
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public void redraw() {
        frame.repaint();
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
        Visual vis = new VisualRender();
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
        }
        vis.redraw();

        // The colors used.
        Color cp1 = new Color(255, 200, 0);
        Color cp2 = new Color(0, 255, 255);
        Color ce1 = new Color(255, 0, 0);
        Color ce2 = new Color(0, 255, 0);
        Color ce3 = new Color(0, 0, 255);
    }
    
    
}
