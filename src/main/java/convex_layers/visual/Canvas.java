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
    private BufferedImage canvas = null;
    
    Canvas(Container parent) {
        super(null);
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

    public void setCanvas(BufferedImage canvas) {
        this.canvas = canvas;
    }

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
