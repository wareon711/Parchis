package presentacion;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class PanelHeaderInicio extends JPanel {

    public PanelHeaderInicio() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        BufferedImage headerImg = Recursos.headerInicio();
        if (headerImg != null) {
            g2.drawImage(headerImg, 0, 0, getWidth(), getHeight(), null);
        } else {
            GradientPaint gp = new GradientPaint(0, 0, new Color(130, 60, 215), 0, getHeight(), new Color(80, 25, 150));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setColor(new Color(25, 10, 60));
        g2.fillRect(0, getHeight() - 2, getWidth(), 2);
        g2.dispose();
    }
}

