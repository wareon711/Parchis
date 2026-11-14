package presentacion;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class BotonPill extends JButton {

    private Color colorNormal = new Color(2, 52, 62);
    private Color colorHover = new Color(5, 78, 92);
    private Color colorPresionado = new Color(1, 38, 45);
    private Color colorBorde = null;

    public BotonPill() {
        this("BOTÓN");
    }

    public BotonPill(String texto) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 15));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(180, 44));
    }

    public Color getColorNormal() {
        return colorNormal;
    }

    public void setColorNormal(Color colorNormal) {
        this.colorNormal = colorNormal;
        repaint();
    }

    public Color getColorHover() {
        return colorHover;
    }

    public void setColorHover(Color colorHover) {
        this.colorHover = colorHover;
        repaint();
    }

    public Color getColorPresionado() {
        return colorPresionado;
    }

    public void setColorPresionado(Color colorPresionado) {
        this.colorPresionado = colorPresionado;
        repaint();
    }

    public Color getColorBorde() {
        return colorBorde;
    }

    public void setColorBorde(Color colorBorde) {
        this.colorBorde = colorBorde;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (getModel().isPressed()) {
            g2.setColor(colorPresionado);
        } else if (getModel().isRollover()) {
            g2.setColor(colorHover);
        } else {
            g2.setColor(colorNormal);
        }

        g2.fillRoundRect(0, 0, w, h, h, h);

        if (colorBorde != null) {
            g2.setColor(colorBorde);
            g2.setStroke(new java.awt.BasicStroke(1.4f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}

