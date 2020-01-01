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
                    else if (key.equals(Key.MINUS)) zoom(1 / DEFAULT_ZOOM, 0.5, 0.5);
                    else if (key.equals(Key.EQUAL)) zoom(DEFAULT_ZOOM, 0.5, 0.5);
                    else if (key.equals(Key.SPACE)) reset();
                    repaint();
                }
            });
            frame.addMouseWheelListener((e) -> {
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
                
                double dx = Math.min(1, Math.max(0, ((double) e.getX()) / getWidth()));
                double dy = Math.min(1, Math.max(0, ((double) e.getY()) / getHeight()));
                zoom(scroll, dx, dy);
                repaint();
            });
        }
        
        
        /* ----------------------------------------
         * Functions.
         * ----------------------------------------
         */
        @Override
        public void setBounds(int x, int y, int width, int height) {
            if (getWidth() != 0 && getHeight() != 0) {
                this.x = this.x * width / getWidth();
                this.y = this.y * height / getHeight();

                super.setBounds(x, y, width, height);
                repaint();

            } else {
                super.setBounds(x, y, width, height);
            }
        }
        
        private void zoom(double amt, double dw, double dh) {
            double oldZoom = zoom;
            zoom *= amt;
            x += ((1 / zoom - 1 / oldZoom) * getWidth() * dw);
            y += ((1 / zoom - 1 / oldZoom) * getHeight() * dh);
        }
        
        private void panHorizontal(double amt) {
            x -= amt / zoom;
        }
        
        private void panVertical(double amt) {
            y -= amt / zoom;
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
            g2d.scale(zoom, zoom);
            g2d.translate(x, y);
            redraw(g2d, 1/zoom, getWidth(), getHeight());
        }
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    public VisualRender() {
        openCounter.incrementAndGet();
        frame = new JFrame("Visual render");
        frame.setLayout(null);
        zp = new ZoomPanel(frame);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Insets in = frame.getInsets();
                zp.setBounds(0, 0,
                        frame.getWidth() - in.left - in.right,
                        frame.getHeight() - in.top - in.bottom
                );
//                zp.setSize(frame.getWidth(), frame.getHeight());
            }
        });
        
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
            frame.add(zp);
            frame.setSize(800, 600);
            frame.setVisible(true);
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
