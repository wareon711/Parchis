package presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import negocios.FachadaReglasMock;
import negocios.IFachadaReglas;

public class VistaSeleccionarEntrada extends javax.swing.JFrame {

    private int numJugadores = 2;
    private String usuario = "Jugador";
    private String colorElegido = SesionUsuario.getColorElegido();
    private String avatarElegido = SesionUsuario.getAvatarElegido();
    private JButton[] botonesAvatar;

    public VistaSeleccionarEntrada() {
        this(2, SesionUsuario.getUsuario());
    }

    public VistaSeleccionarEntrada(int numJugadores, String usuario) {
        this.numJugadores = numJugadores;
        this.usuario = usuario != null && !usuario.isEmpty() ? usuario : SesionUsuario.getUsuario();
        this.colorElegido = SesionUsuario.getColorElegido();
        this.avatarElegido = SesionUsuario.getAvatarElegido();
        SesionUsuario.setUltimoModoJugadores(numJugadores);
        initComponents();
        personalizar();
    }

    private void personalizar() {
        String tituloModo = "EN LÍNEA " + numJugadores + " JUGADORES";
        setTitle("Parchís – " + tituloModo);
        lblTituloModo.setText(tituloModo);
        lblTituloModo.setFont(new Font("Arial Black", Font.BOLD, 22));
        lblTituloModo.setForeground(new Color(255, 220, 80));
        setLocationRelativeTo(null);

        lblElegirColor.setText("ELEGIR COLOR");
        lblElegirColor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblElegirColor.setForeground(new Color(150, 123, 242));

        BufferedImage boardImg = Recursos.boardModal();
        if (boardImg != null) {
            lblBoard.setIcon(Recursos.icono(boardImg, 205, 172));
        }

        configurarFicha(btnColorAzul, "AZUL");
        configurarFicha(btnColorRojo, "ROJO");
        configurarFicha(btnColorVerde, "VERDE");
        configurarFicha(btnColorAmarillo, "AMARILLO");

        actualizarColor();
        actualizarAvatar();

        BufferedImage imgContinuar = Recursos.btnContinuar();
        if (imgContinuar != null) {
            btnIniciar.setText("");
            btnIniciar.setIcon(Recursos.icono(imgContinuar, 224, 63));
            btnIniciar.setContentAreaFilled(false);
            btnIniciar.setBorder(null);
            btnIniciar.setFocusPainted(false);
            btnIniciar.setPreferredSize(new Dimension(224, 63));
        }
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciar.addActionListener(e -> alIniciarPartida());

        btnRegresar.setText("← Volver");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegresar.setForeground(new Color(180, 165, 245));
        btnRegresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegresar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnRegresar.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnRegresar.setForeground(new Color(180, 165, 245));
            }
        });
        btnRegresar.addActionListener(e -> {
            VistaInicio inicio = new VistaInicio(usuario);
            inicio.setVisible(true);
            dispose();
        });
    }

    private void configurarFicha(JButton btn, String color) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(54, 54));
        btn.addActionListener(e -> {
            colorElegido = color;
            SesionUsuario.setColorElegido(color);
            actualizarColor();
        });
    }

    private void actualizarColor() {
        btnColorAzul.setIcon(crearIconoPawn(Recursos.pawn("AZUL"), "AZUL".equals(colorElegido)));
        btnColorRojo.setIcon(crearIconoPawn(Recursos.pawn("ROJO"), "ROJO".equals(colorElegido)));
        btnColorVerde.setIcon(crearIconoPawn(Recursos.pawn("VERDE"), "VERDE".equals(colorElegido)));
        btnColorAmarillo.setIcon(crearIconoPawn(Recursos.pawn("AMARILLO"), "AMARILLO".equals(colorElegido)));
    }

    private Icon crearIconoPawn(BufferedImage imgPawn, boolean seleccionado) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                int w = getIconWidth();
                int h = getIconHeight();
                int arc = 16;

                if (seleccionado) {

                    g2.setColor(new Color(172, 149, 255));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);

                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                } else {

                    g2.setColor(new Color(0, 18, 54));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);

                    g2.setColor(new Color(18, 30, 80));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                }

                if (imgPawn != null) {
                    int pw = 24;
                    int ph = 42;
                    int px = x + (w - pw) / 2;
                    int py = y + (h - ph) / 2;
                    g2.drawImage(imgPawn, px, py, pw, ph, null);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 52;
            }

            @Override
            public int getIconHeight() {
                return 52;
            }
        };
    }

    private void actualizarAvatar() {
        if (botonesAvatar == null) return;
        String[] cods = {"AV1", "AV2", "AV3", "AV4"};
        for (int i = 0; i < cods.length; i++) {
            botonesAvatar[i].setIcon(crearIconoAvatar(Recursos.avatar(cods[i]), cods[i].equals(avatarElegido)));
        }
    }

    private Icon crearIconoAvatar(BufferedImage imgAv, boolean seleccionado) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int w = getIconWidth();
                int h = getIconHeight();
                int arc = 14;
                if (seleccionado) {
                    g2.setColor(new Color(172, 149, 255));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                } else {
                    g2.setColor(new Color(0, 18, 54));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                    g2.setColor(new Color(18, 30, 80));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                }
                if (imgAv != null) {
                    g2.drawImage(imgAv, x + 7, y + 7, 34, 34, null);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 48;
            }

            @Override
            public int getIconHeight() {
                return 48;
            }
        };
    }

    private void alIniciarPartida() {
        VistaSalaEspera sala = new VistaSalaEspera(numJugadores, usuario, colorElegido);
        sala.setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelContenido = new javax.swing.JPanel();
        panelHeader = new javax.swing.JPanel();
        btnRegresar = new javax.swing.JButton();
        lblTituloModo = new javax.swing.JLabel();
        lblBoard = new javax.swing.JLabel();
        lblElegirColor = new javax.swing.JLabel();
        panelPawns = new javax.swing.JPanel();
        btnColorAzul = new javax.swing.JButton();
        btnColorRojo = new javax.swing.JButton();
        btnColorVerde = new javax.swing.JButton();
        btnColorAmarillo = new javax.swing.JButton();
        btnIniciar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís - Seleccionar Entrada");
        setMinimumSize(new java.awt.Dimension(380, 720));
        setResizable(false);

        panelFondo.setLayout(new java.awt.GridBagLayout());

        panelContenido.setOpaque(false);
        panelContenido.setPreferredSize(new java.awt.Dimension(340, 680));
        panelContenido.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 14));

        panelHeader.setOpaque(false);
        panelHeader.setPreferredSize(new java.awt.Dimension(330, 44));
        panelHeader.setLayout(new java.awt.BorderLayout());

        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnRegresar.setForeground(new java.awt.Color(200, 215, 255));
        btnRegresar.setText("← Volver");
        btnRegresar.setBorderPainted(false);
        btnRegresar.setContentAreaFilled(false);
        btnRegresar.setFocusPainted(false);
        panelHeader.add(btnRegresar, java.awt.BorderLayout.WEST);

        panelContenido.add(panelHeader);

        lblTituloModo.setFont(new java.awt.Font("Arial Black", 1, 20));
        lblTituloModo.setForeground(new java.awt.Color(255, 215, 64));
        lblTituloModo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTituloModo.setText("EN LÍNEA 2 JUGADORES");
        lblTituloModo.setPreferredSize(new java.awt.Dimension(320, 30));
        panelContenido.add(lblTituloModo);

        lblBoard.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBoard.setPreferredSize(new java.awt.Dimension(220, 148));
        panelContenido.add(lblBoard);

        lblElegirColor.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblElegirColor.setForeground(new java.awt.Color(190, 205, 255));
        lblElegirColor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblElegirColor.setText("ELEGIR COLOR");
        lblElegirColor.setPreferredSize(new java.awt.Dimension(320, 22));
        panelContenido.add(lblElegirColor);

        panelPawns.setOpaque(false);
        panelPawns.setPreferredSize(new java.awt.Dimension(320, 62));
        panelPawns.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 14, 2));

        btnColorAzul.setPreferredSize(new java.awt.Dimension(54, 58));
        panelPawns.add(btnColorAzul);

        btnColorRojo.setPreferredSize(new java.awt.Dimension(54, 58));
        panelPawns.add(btnColorRojo);

        btnColorVerde.setPreferredSize(new java.awt.Dimension(54, 58));
        panelPawns.add(btnColorVerde);

        btnColorAmarillo.setPreferredSize(new java.awt.Dimension(54, 58));
        panelPawns.add(btnColorAmarillo);

        panelContenido.add(panelPawns);

        javax.swing.JLabel lblElegirAvatar = new javax.swing.JLabel("ELEGIR AVATAR");
        lblElegirAvatar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblElegirAvatar.setForeground(new java.awt.Color(150, 123, 242));
        lblElegirAvatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblElegirAvatar.setPreferredSize(new java.awt.Dimension(320, 20));
        panelContenido.add(lblElegirAvatar);

        javax.swing.JPanel panelAvatares = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 2));
        panelAvatares.setOpaque(false);
        panelAvatares.setPreferredSize(new java.awt.Dimension(320, 56));
        String[] cods = {"AV1", "AV2", "AV3", "AV4"};
        botonesAvatar = new JButton[cods.length];
        for (int i = 0; i < cods.length; i++) {
            final String cod = cods[i];
            JButton bAv = new JButton();
            bAv.setContentAreaFilled(false);
            bAv.setBorderPainted(false);
            bAv.setFocusPainted(false);
            bAv.setCursor(new Cursor(Cursor.HAND_CURSOR));
            bAv.setPreferredSize(new Dimension(50, 50));
            bAv.addActionListener(e -> {
                avatarElegido = cod;
                SesionUsuario.setAvatarElegido(cod);
                actualizarAvatar();
            });
            botonesAvatar[i] = bAv;
            panelAvatares.add(bAv);
        }
        panelContenido.add(panelAvatares);

        btnIniciar.setBackground(new java.awt.Color(46, 175, 80));
        btnIniciar.setFont(new java.awt.Font("Arial Black", 1, 20));
        btnIniciar.setForeground(new java.awt.Color(255, 255, 255));
        btnIniciar.setText("INICIAR");
        btnIniciar.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 32, 12, 32));
        btnIniciar.setFocusPainted(false);
        btnIniciar.setPreferredSize(new java.awt.Dimension(240, 56));
        panelContenido.add(btnIniciar);

        panelFondo.add(panelContenido, new java.awt.GridBagConstraints());

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaSeleccionarEntrada(2, "Jugador").setVisible(true);
        });
    }

    private javax.swing.JButton btnColorAmarillo;
    private javax.swing.JButton btnColorAzul;
    private javax.swing.JButton btnColorRojo;
    private javax.swing.JButton btnColorVerde;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JLabel lblBoard;
    private javax.swing.JLabel lblElegirColor;
    private javax.swing.JLabel lblTituloModo;
    private javax.swing.JPanel panelContenido;
    private presentacion.PanelFondo panelFondo;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelPawns;

}

