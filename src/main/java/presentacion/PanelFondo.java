package presentacion;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

public class PanelFondo extends JPanel {

    private static final Color COLOR_CENTRO = new Color(72, 39, 166);
    private static final Color COLOR_BORDE = new Color(12, 5, 87);

    public PanelFondo() {
        setOpaque(true);
        setBackground(COLOR_BORDE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Point2D center = new Point2D.Float(w / 2.0f, h * 0.45f);
        float radius = Math.max(w, h) * 0.75f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {COLOR_CENTRO, COLOR_BORDE};

        RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(rgp);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }
}

