package presentacion;

import dto.EstadoPartidaDTO;
import dto.JugadorDTO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class VistaResultados extends javax.swing.JFrame {

    private boolean esGanador = true;
    private String idGanador = "J1";
    private String miNombre = "TÚ";
    private String nombreGanador = "Ganador";
    private final List<FilaPosicion> posiciones = new ArrayList<>();

    public static class FilaPosicion {
        public String puesto;
        public String nombre;
        public String colorStr;
        public Color colorFicha;
        public int fichasMeta;
        public boolean activo;
        public boolean esLocal;
        public boolean esPrimero;

        public FilaPosicion(String puesto, String nombre, String colorStr, Color colorFicha,
                int fichasMeta, boolean activo, boolean esLocal, boolean esPrimero) {
            this.puesto = puesto;
            this.nombre = nombre;
            this.colorStr = colorStr;
            this.colorFicha = colorFicha;
            this.fichasMeta = fichasMeta;
            this.activo = activo;
            this.esLocal = esLocal;
            this.esPrimero = esPrimero;
        }
    }

    public VistaResultados() {
        this(true, "TÚ", "J1");
    }

    public VistaResultados(boolean esGanador, String miNombre, String idGanador) {
        this(esGanador, miNombre, idGanador, null);
    }

    public VistaResultados(boolean esGanador, String miNombre, String idGanador, EstadoPartidaDTO estado) {
        this.esGanador = esGanador;
        this.miNombre = miNombre != null ? miNombre : SesionUsuario.getUsuario();
        this.idGanador = idGanador != null ? idGanador : "J1";

        SesionUsuario.sumarPartida();
        if (esGanador) {
            SesionUsuario.sumarVictoria();
        }

        try {
            negocios.FabricaSubsistemas.crearPerfilBO().registrarPartidaTerminada(this.miNombre, esGanador);
        } catch (Exception ignored) {
        }

        if (estado != null && estado.getJugadores() != null && !estado.getJugadores().isEmpty()) {
            cargarEstado(estado);
        } else {
            datosEjemplo();
        }
        initComponents();
        personalizar();
    }

    private void cargarEstado(EstadoPartidaDTO estado) {
        posiciones.clear();
        List<JugadorDTO> copia = new ArrayList<>(estado.getJugadores());
        copia.sort((a, b) -> {
            if (a.getIdJugador().equals(idGanador)) return -1;
            if (b.getIdJugador().equals(idGanador)) return 1;
            if (a.isActivo() != b.isActivo()) return a.isActivo() ? -1 : 1;
            return Integer.compare(b.getFichasEnMeta(), a.getFichasEnMeta());
        });

        String[] puestos = {"1.º", "2.º", "3.º", "4.º"};
        for (int i = 0; i < copia.size(); i++) {
            JugadorDTO j = copia.get(i);
            if (i == 0) {
                nombreGanador = j.getNombreUsuario();
            }
            boolean local = j.getNombreUsuario().equalsIgnoreCase(miNombre)
                    || (esGanador && j.getIdJugador().equals(idGanador));
            String cStr = j.getColor() != null ? j.getColor().toUpperCase() : "AZUL";
            Color colorFicha = obtenerColor(cStr);
            String nombreMostrar = j.getNombreUsuario() + (local ? " (Tú)" : "");
            String puestoStr = i < puestos.length ? puestos[i] : (i + 1) + ".º";
            posiciones.add(new FilaPosicion(
                    puestoStr,
                    nombreMostrar,
                    cStr,
                    colorFicha,
                    j.getFichasEnMeta(),
                    j.isActivo(),
                    local,
                    i == 0));
        }
    }

    private void datosEjemplo() {
        posiciones.clear();
        if (esGanador) {
            nombreGanador = miNombre;
            posiciones.add(new FilaPosicion("1.º", miNombre + " (Tú)", "VERDE", obtenerColor("VERDE"), 4, true, true, true));
            posiciones.add(new FilaPosicion("2.º", "SofiaBot", "AZUL", obtenerColor("AZUL"), 1, true, false, false));
        } else {
            nombreGanador = "SofiaBot";
            posiciones.add(new FilaPosicion("1.º", "SofiaBot", "AZUL", obtenerColor("AZUL"), 4, true, false, true));
            posiciones.add(new FilaPosicion("2.º", miNombre + " (Tú)", "VERDE", obtenerColor("VERDE"), 2, true, true, false));
        }
    }

    private Color obtenerColor(String cStr) {
        switch (cStr.toUpperCase()) {
            case "VERDE": return new Color(40, 190, 80);
            case "ROJO": return new Color(230, 45, 45);
            case "AMARILLO": return new Color(245, 185, 20);
            case "AZUL":
            default: return new Color(50, 110, 240);
        }
    }

    private BufferedImage crearTrofeoDerrota(BufferedImage orig) {
        if (orig == null) return null;
        int w = orig.getWidth();
        int h = orig.getHeight();
        BufferedImage gris = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = orig.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int nr = Math.min(255, (int) (lum * 0.65 + 45));
                int ng = Math.min(255, (int) (lum * 0.62 + 38));
                int nb = Math.min(255, (int) (lum * 0.82 + 78));
                gris.setRGB(x, y, (a << 24) | (nr << 16) | (ng << 8) | nb);
            }
        }
        return gris;
    }

    private BufferedImage crearListonDerrota(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(65, 18, 55));
        int[] xIzq = {6, 36, 36, 6, 18};
        int[] yIzq = {14, 14, h - 4, h - 4, (h + 10) / 2};
        g2.fillPolygon(xIzq, yIzq, 5);

        int[] xDer = {w - 6, w - 36, w - 36, w - 6, w - 18};
        int[] yDer = {14, 14, h - 4, h - 4, (h + 10) / 2};
        g2.fillPolygon(xDer, yDer, 5);

        GradientPaint gp = new GradientPaint(0, 4, new Color(135, 32, 82), 0, h - 10, new Color(78, 18, 58));
        g2.setPaint(gp);
        g2.fillRoundRect(24, 4, w - 48, h - 14, 16, 16);

        g2.setColor(new Color(235, 110, 165));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(24, 4, w - 48, h - 14, 16, 16);
        g2.dispose();
        return img;
    }

    private void personalizar() {
        setTitle(esGanador ? "Parchís – ¡Victoria!" : "Parchís – Fin de Partida");
        setLocationRelativeTo(null);

        BufferedImage imgTrofeo = Recursos.trofeo();
        if (imgTrofeo != null) {
            BufferedImage mostrar = esGanador ? imgTrofeo : crearTrofeoDerrota(imgTrofeo);
            lblTrofeo.setIcon(Recursos.icono(mostrar, 116, 116));
        }

        if (esGanador) {
            BufferedImage imgListon = Recursos.liston();
            if (imgListon != null) {
                lblListon.setIcon(Recursos.icono(imgListon, 260, 60));
            }
            lblResultado.setText("¡VICTORIA!");
            lblResultado.setFont(new Font("Arial Black", Font.BOLD, 22));
            lblResultado.setForeground(new Color(125, 25, 15));
            lblSubtitulo.setText("● ¡FELICIDADES, GANASTE LA PARTIDA!");
            lblSubtitulo.setForeground(new Color(120, 255, 130));
        } else {
            BufferedImage listonDerrota = crearListonDerrota(260, 60);
            lblListon.setIcon(Recursos.icono(listonDerrota, 260, 60));
            lblResultado.setText("DERROTA");
            lblResultado.setFont(new Font("Arial Black", Font.BOLD, 22));
            lblResultado.setForeground(new Color(255, 230, 245));
            lblSubtitulo.setText("● GANADOR DE LA PARTIDA: " + nombreGanador.toUpperCase());
            lblSubtitulo.setForeground(new Color(255, 210, 90));
        }

        llenarTabla();

        btnRevancha.setText("JUGAR REVANCHA");
        btnRevancha.setColorNormal(esGanador ? new Color(245, 190, 25) : new Color(45, 28, 105));
        btnRevancha.setColorHover(esGanador ? new Color(255, 210, 50) : new Color(65, 42, 140));
        btnRevancha.setColorPresionado(esGanador ? new Color(185, 135, 10) : new Color(30, 18, 75));
        btnRevancha.setForeground(esGanador ? new Color(25, 18, 55) : new Color(235, 225, 255));
        btnRevancha.setFont(new Font("Arial Black", Font.BOLD, 14));
        btnRevancha.setPreferredSize(new Dimension(224, 46));
        btnRevancha.addActionListener(e -> {
            VistaSalaEspera sala = new VistaSalaEspera(
                    SesionUsuario.getUltimoModoJugadores(),
                    miNombre,
                    SesionUsuario.getColorElegido());
            sala.setVisible(true);
            dispose();
        });

        BufferedImage imgCont = Recursos.btnContinuar();
        if (imgCont != null) {
            btnContinuar.setText("");
            btnContinuar.setIcon(Recursos.icono(imgCont, 224, 60));
            btnContinuar.setContentAreaFilled(false);
            btnContinuar.setBorder(null);
            btnContinuar.setFocusPainted(false);
            btnContinuar.setPreferredSize(new Dimension(224, 60));
        }
        btnContinuar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnContinuar.addActionListener(e -> {
            VistaInicio inicio = new VistaInicio(miNombre);
            inicio.setVisible(true);
            dispose();
        });
    }

    private void estilizarTarjeta(JPanel slot, FilaPosicion fila) {
        slot.setOpaque(false);
        slot.setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 20;
                if (fila.esPrimero) {
                    g2.setColor(new Color(36, 22, 82, 238));
                    g2.fillRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                    g2.setColor(new Color(255, 210, 65));
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                } else if (fila.esLocal) {
                    g2.setColor(new Color(24, 14, 65, 232));
                    g2.fillRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                    g2.setColor(new Color(172, 149, 255));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                } else {
                    g2.setColor(new Color(14, 18, 54, 215));
                    g2.fillRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                    g2.setColor(new Color(50, 65, 125));
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                }
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(6, 14, 6, 14);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });
    }

    private void llenarTabla() {
        panelLista.removeAll();
        for (FilaPosicion fila : posiciones) {
            JPanel pItem = new JPanel(new BorderLayout(10, 0));
            pItem.setPreferredSize(new Dimension(306, 54));
            estilizarTarjeta(pItem, fila);

            JLabel lblPawn = new JLabel();
            lblPawn.setPreferredSize(new Dimension(28, 40));
            lblPawn.setHorizontalAlignment(SwingConstants.CENTER);
            BufferedImage imgPawn = Recursos.pawn(fila.colorStr);
            if (imgPawn != null) {
                lblPawn.setIcon(Recursos.icono(imgPawn, 22, 38));
            }

            JPanel panelTextos = new JPanel(new GridLayout(2, 1, 0, 1));
            panelTextos.setOpaque(false);

            JLabel lblNombre = new JLabel(fila.nombre);
            lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblNombre.setForeground(Color.WHITE);

            String detalle = fila.activo
                    ? ("Meta: " + fila.fichasMeta + " / 4 fichas")
                    : "Abandonó la partida";
            JLabel lblDetalle = new JLabel(detalle);
            lblDetalle.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblDetalle.setForeground(fila.esPrimero
                    ? new Color(255, 220, 95)
                    : (fila.activo ? new Color(185, 170, 245) : new Color(255, 115, 115)));

            panelTextos.add(lblNombre);
            panelTextos.add(lblDetalle);

            JPanel pIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
            pIzquierda.setOpaque(false);
            pIzquierda.add(lblPawn);
            pIzquierda.add(panelTextos);

            String textoPuesto = fila.esPrimero ? ("★ " + fila.puesto) : fila.puesto;
            JLabel lblPuesto = new JLabel(textoPuesto);
            lblPuesto.setFont(new Font("Arial Black", Font.BOLD, 14));
            lblPuesto.setForeground(fila.esPrimero
                    ? new Color(255, 215, 65)
                    : (fila.esLocal ? new Color(195, 180, 255) : new Color(150, 165, 215)));

            pItem.add(pIzquierda, BorderLayout.CENTER);
            pItem.add(lblPuesto, BorderLayout.EAST);

            panelLista.add(pItem);
        }
        panelLista.revalidate();
        panelLista.repaint();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelContenido = new javax.swing.JPanel();
        panelTrofeo = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = 82;
                float radio = 95f;
                Color cCentro = esGanador
                        ? new Color(255, 215, 60, 95)
                        : new Color(195, 45, 115, 80);
                Color cBorde = new Color(20, 10, 55, 0);
                RadialGradientPaint rgp = new RadialGradientPaint(
                        new Point2D.Float(cx, cy),
                        radio,
                        new float[]{0f, 1f},
                        new Color[]{cCentro, cBorde});
                g2.setPaint(rgp);
                g2.fillOval((int) (cx - radio), (int) (cy - radio), (int) (radio * 2), (int) (radio * 2));
                g2.dispose();
            }
        };
        lblTrofeo = new javax.swing.JLabel();
        lblResultado = new javax.swing.JLabel();
        lblListon = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        panelLista = new javax.swing.JPanel();
        btnRevancha = new presentacion.BotonPill("JUGAR REVANCHA");
        btnContinuar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís - Resultados");
        setMinimumSize(new java.awt.Dimension(380, 720));
        setPreferredSize(new java.awt.Dimension(400, 750));
        setResizable(false);

        panelFondo.setLayout(new java.awt.GridBagLayout());

        panelContenido.setOpaque(false);
        panelContenido.setPreferredSize(new java.awt.Dimension(340, 690));
        panelContenido.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 10));

        panelTrofeo.setOpaque(false);
        panelTrofeo.setPreferredSize(new java.awt.Dimension(320, 200));
        panelTrofeo.setLayout(null);

        lblTrofeo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTrofeo.setBounds(100, 8, 120, 120);
        panelTrofeo.add(lblTrofeo);

        lblResultado.setFont(new java.awt.Font("Arial Black", 1, 22));
        lblResultado.setForeground(new java.awt.Color(130, 25, 20));
        lblResultado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblResultado.setText("¡VICTORIA!");
        lblResultado.setBounds(30, 134, 260, 36);
        panelTrofeo.add(lblResultado);

        lblListon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblListon.setBounds(30, 126, 260, 60);
        panelTrofeo.add(lblListon);

        panelContenido.add(panelTrofeo);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblSubtitulo.setForeground(new java.awt.Color(120, 255, 130));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("● FIN DE LA PARTIDA");
        lblSubtitulo.setPreferredSize(new java.awt.Dimension(320, 22));
        panelContenido.add(lblSubtitulo);

        panelLista.setOpaque(false);
        panelLista.setPreferredSize(new java.awt.Dimension(320, 248));
        panelLista.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 8));
        panelContenido.add(panelLista);

        panelContenido.add(btnRevancha);

        btnContinuar.setBackground(new java.awt.Color(46, 175, 80));
        btnContinuar.setFont(new java.awt.Font("Arial Black", 1, 18));
        btnContinuar.setForeground(new java.awt.Color(255, 255, 255));
        btnContinuar.setText("CONTINUAR");
        btnContinuar.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 28, 10, 28));
        btnContinuar.setFocusPainted(false);
        btnContinuar.setPreferredSize(new java.awt.Dimension(224, 60));
        panelContenido.add(btnContinuar);

        panelFondo.add(panelContenido, new java.awt.GridBagConstraints());

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaResultados(true, "TÚ", "J1").setVisible(true);
        });
    }

    private javax.swing.JButton btnContinuar;
    private presentacion.BotonPill btnRevancha;
    private javax.swing.JLabel lblListon;
    private javax.swing.JLabel lblResultado;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTrofeo;
    private javax.swing.JPanel panelContenido;
    private presentacion.PanelFondo panelFondo;
    private javax.swing.JPanel panelTrofeo;
    private javax.swing.JPanel panelLista;

}
