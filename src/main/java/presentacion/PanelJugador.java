package presentacion;

import dto.EstadoPartidaDTO;
import dto.JugadorDTO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class PanelJugador extends JPanel {

    private String idJugador = "J1";
    private JugadorDTO jugador;
    private boolean enTurno;
    private boolean esLocal;
    private double fraccionTiempo = 1.0;

    public PanelJugador() {
        this("J1");
    }

    public PanelJugador(String idJugador) {
        this.idJugador = idJugador != null ? idJugador : "J1";
        setOpaque(false);
        setPreferredSize(new Dimension(110, 82));
    }

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador != null ? idJugador : "J1";
        repaint();
    }

    public void setEsLocal(boolean esLocal) {
        this.esLocal = esLocal;
        repaint();
    }

    public void setFraccionTiempo(double fraccionTiempo) {
        this.fraccionTiempo = Math.max(0.0, Math.min(1.0, fraccionTiempo));
        repaint();
    }

    public double getFraccionTiempo() {
        return fraccionTiempo;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(110, 82);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(110, 82);
    }

    public void actualizar(EstadoPartidaDTO estado) {
        this.jugador = null;
        for (JugadorDTO j : estado.getJugadores()) {
            if (j.getIdJugador().equals(idJugador)) {
                this.jugador = j;
                break;
            }
        }
        boolean previoEnTurno = this.enTurno;
        this.enTurno = idJugador.equals(estado.getIdJugadorEnTurno()) && estado.getIdGanador() == null;
        if (!this.enTurno) {
            this.fraccionTiempo = 0.0;
        } else if (!previoEnTurno) {
            this.fraccionTiempo = 1.0;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (jugador == null) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int diametro = 46;
        int posX = (getWidth() - diametro) / 2;
        int posY = 2;

        BufferedImage avatar = Recursos.avatar(jugador.getAvatar());
        if (avatar != null) {
            g.drawImage(avatar, posX, posY, diametro, diametro, null);
        }

        if (enTurno) {
            g.setStroke(new BasicStroke(3.0f));
            g.setColor(new Color(255, 255, 255, 45));
            g.drawOval(posX - 1, posY - 1, diametro + 2, diametro + 2);

            Color colorTimer;
            if (fraccionTiempo > 0.28) {
                colorTimer = new Color(46, 213, 115);
            } else if (fraccionTiempo > 0.14) {
                colorTimer = new Color(255, 195, 18);
            } else {
                colorTimer = new Color(255, 56, 56);
            }

            int anguloBarrido = (int) Math.round(360.0 * fraccionTiempo);
            if (anguloBarrido > 0) {
                g.setStroke(new BasicStroke(3.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(colorTimer);
                g.drawArc(posX - 1, posY - 1, diametro + 2, diametro + 2, 90, -anguloBarrido);
            }
        } else {
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(new Color(255, 255, 255, 100));
            g.drawOval(posX, posY, diametro, diametro);
        }

        String colorJ = jugador.getColor();
        if (colorJ != null) {
            Color cPawn;
            switch (colorJ.toUpperCase()) {
                case "AZUL": cPawn = new Color(30, 110, 240); break;
                case "VERDE": cPawn = new Color(40, 190, 80); break;
                case "ROJO": cPawn = new Color(230, 45, 45); break;
                case "AMARILLO":
                default: cPawn = new Color(245, 185, 20); break;
            }
            g.setColor(cPawn);
            g.fillOval(posX + diametro - 14, posY + diametro - 14, 16, 16);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(posX + diametro - 14, posY + diametro - 14, 16, 16);
        }

        String nombreBase = jugador.getNombreUsuario() != null ? jugador.getNombreUsuario() : idJugador;
        String texto = esLocal ? (nombreBase + " (Tú)") : nombreBase;

        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        if (!jugador.isActivo()) {
            g.setColor(new Color(160, 160, 180));
        } else if (enTurno) {
            if (fraccionTiempo <= 0.14) {
                g.setColor(new Color(255, 100, 100));
            } else {
                g.setColor(new Color(255, 215, 60));
            }
        } else {
            g.setColor(Color.WHITE);
        }

        FontMetrics fm = g.getFontMetrics();
        int textoX = Math.max(2, (getWidth() - fm.stringWidth(texto)) / 2);
        g.drawString(texto, textoX, posY + diametro + 14);

        String subTexto;
        Color colorSub;
        if (!jugador.isActivo()) {
            subTexto = "RETIRADO";
            colorSub = new Color(255, 110, 110);
        } else if (jugador.getTurnosInactivos() > 0) {
            subTexto = "AFK " + jugador.getTurnosInactivos() + "/6 · Meta " + jugador.getFichasEnMeta() + "/4";
            colorSub = new Color(255, 195, 70);
        } else {
            subTexto = "Meta: " + jugador.getFichasEnMeta() + "/4";
            colorSub = new Color(195, 180, 255);
        }
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g.setColor(colorSub);
        FontMetrics fmSub = g.getFontMetrics();
        int subX = Math.max(2, (getWidth() - fmSub.stringWidth(subTexto)) / 2);
        g.drawString(subTexto, subX, posY + diametro + 27);

        g.dispose();
    }
}
