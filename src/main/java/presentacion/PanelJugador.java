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

    public PanelJugador() {
        this("J1");
    }

    public PanelJugador(String idJugador) {
        this.idJugador = idJugador != null ? idJugador : "J1";
        setOpaque(false);
        setPreferredSize(new Dimension(68, 76));
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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(68, 76);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(68, 76);
    }

    public void actualizar(EstadoPartidaDTO estado) {
        for (JugadorDTO j : estado.getJugadores()) {
            if (j.getIdJugador().equals(idJugador)) {
                this.jugador = j;
                break;
            }
        }
        this.enTurno = idJugador.equals(estado.getIdJugadorEnTurno()) && estado.getIdGanador() == null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int diametro = 46;
        int posX = (getWidth() - diametro) / 2;
        int posY = 2;

        BufferedImage avatar = Recursos.perfil();
        if (avatar != null) {
            g.drawImage(avatar, posX, posY, diametro, diametro, null);
        }

        if (enTurno) {
            g.setStroke(new BasicStroke(3.5f));
            g.setColor(new Color(255, 194, 26));
            g.drawOval(posX - 1, posY - 1, diametro + 2, diametro + 2);
        } else {
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(new Color(255, 255, 255, 80));
            g.drawOval(posX, posY, diametro, diametro);
        }

        String texto = esLocal ? "TÚ" : (jugador != null ? jugador.getNombreUsuario() : ("J2".equalsIgnoreCase(idJugador) ? "Jugador 2" : "Jugador 1"));

        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.setColor(enTurno ? new Color(255, 215, 60) : Color.WHITE);

        FontMetrics fm = g.getFontMetrics();
        int textoX = (getWidth() - fm.stringWidth(texto)) / 2;
        g.drawString(texto, textoX, posY + diametro + 16);

        g.dispose();
    }
}
