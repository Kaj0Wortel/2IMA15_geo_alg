package convex_layers.visual;


import tools.MultiTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

/**
 * Panel used to draw the buffered image on.
 * Also redraws the buffered image when resized.
 */
public class Canvas
        extends JPanel {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The canvas to draw. */
    private BufferedImage canvas = null;
    
    
    /* ----------------------------------------------------------------------
     * Constructor.
     * ----------------------------------------------------------------------
     */

    /**
     * Creates a new canvas which scaled to the given parent.
     * 
     * @param parent The parent component of the canvas.
     */
    Canvas(Container parent) {
        super(null);
        if (parent == null) return;
        SwingUtilities.invokeLater(() -> {
            parent.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    Insets in = parent.getInsets();
                    setBounds(0, 0,
                            parent.getWidth() - in.left - in.right,
                            parent.getHeight() - in.top - in.bottom);
                }
            });
            parent.add(this);
        });
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Sets the canvas image.
     * 
     * @param canvas The new canvas.
     */
    public void setCanvas(BufferedImage canvas) {
        this.canvas = canvas;
    }
    
    /** 
     * @return The current canvas.
     */
    public BufferedImage getCanvas() {
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
