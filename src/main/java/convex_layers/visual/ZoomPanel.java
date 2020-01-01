package convex_layers.visual;


import convex_layers.math.Edge;
import convex_layers.math.Vector;
import tools.concurrent.ThreadTimer;
import tools.data.array.ArrayTools;
import tools.event.Key;
import tools.font.FontLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class ZoomPanel
        extends JPanel {
    
    /* ----------------------------------------------------------------------
     * Constants.
     * ----------------------------------------------------------------------
     */
    private static final double DEFAULT_PAN = 10;
    private static final double DEFAULT_ZOOM = 1.1;
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
    
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    // Panning and scaling variables.
    /** The current displacement x-coordinate. */
    private double x = 0;
    /** The current displacement y-coordinate. */
    private double y = 0;
    /** The current scaling of the rendering.. */
    private double zoom = 1;
    

    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    public ZoomPanel(JFrame frame) {
        super(null);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Key key = new Key(e);
                if (key.equals(Key.UP)) panVertical(-DEFAULT_PAN);
                else if (key.equals(Key.RIGHT)) panHorizontal(DEFAULT_PAN);
                else if (key.equals(Key.DOWN)) panVertical(DEFAULT_PAN);
                else if (key.equals(Key.LEFT)) panHorizontal(-DEFAULT_PAN);
                else if (key.equals(Key.MINUS)) zoom(DEFAULT_ZOOM);
                else if (key.equals(Key.EQUAL)) zoom(1/DEFAULT_ZOOM);
                repaint();
            }
        });
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    private void zoom(double zoom) {
        this.zoom *= zoom;
    }
    
    private void panHorizontal(double amt) {
        x -= amt;
    }
    
    private void panVertical(double amt) {
        y -= amt;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        /*
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
        */
    }
    
    
}
