package presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

public class DialogPerfil extends javax.swing.JDialog {

    private String usuario = "Jugador";
    private Runnable onCerrarSesion;

    private final JButton btnColorAzul = new JButton();
    private final JButton btnColorRojo = new JButton();
    private final JButton btnColorVerde = new JButton();
    private final JButton btnColorAmarillo = new JButton();

    public DialogPerfil(java.awt.Frame parent, boolean modal) {
        this(parent, "Jugador", null);
    }

    public DialogPerfil(java.awt.Frame parent, String usuario, Runnable onCerrarSesion) {
        super(parent, true);
        this.usuario = usuario != null && !usuario.isEmpty() ? usuario : "Jugador";
        this.onCerrarSesion = onCerrarSesion;
        cargarDatosDB();
        initComponents();
        personalizar();
    }

    private void cargarDatosDB() {
        try {
            java.util.Optional<dto.PerfilDTO> opt = negocios.FabricaSubsistemas.crearPerfilBO().consultarPerfil(usuario);
            if (opt.isPresent()) {
                dto.PerfilDTO p = opt.get();
                if (p.colorPreferido() != null && !p.colorPreferido().isEmpty()) {
                    SesionUsuario.setColorElegido(p.colorPreferido());
                }
                while (SesionUsuario.getPartidasJugadas() < p.partidasJugadas()) {
                    SesionUsuario.sumarPartida();
                }
                while (SesionUsuario.getVictorias() < p.partidasGanadas()) {
                    SesionUsuario.sumarVictoria();
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void personalizar() {
        setTitle("Parchís – Mi Perfil");
        setLocationRelativeTo(getParent());

        lblTitulo.setText("MI PERFIL");
        lblTitulo.setFont(new Font("Arial Black", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(255, 220, 80));

        lblNombreUsuario.setText(usuario);
        lblNombreUsuario.setFont(new Font("Arial Black", Font.BOLD, 20));
        lblNombreUsuario.setForeground(Color.WHITE);

        BufferedImage imgPerfil = Recursos.perfil();
        if (imgPerfil != null) {
            lblAvatar.setIcon(Recursos.icono(imgPerfil, 56, 56));
        }

        panelAvatarBorder.setOpaque(false);
        panelAvatarBorder.setBackground(new Color(0, 0, 0, 0));

        panelStats.setColorFondo(new Color(24, 14, 65, 230));
        panelStats.setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(172, 149, 255, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, 20, 20);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(10, 14, 10, 14);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });

        panelEspacio.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
        panelEspacio.setPreferredSize(new Dimension(300, 48));
        configurarColor(btnColorAzul, "AZUL");
        configurarColor(btnColorRojo, "ROJO");
        configurarColor(btnColorVerde, "VERDE");
        configurarColor(btnColorAmarillo, "AMARILLO");

        panelEspacio.removeAll();
        panelEspacio.add(btnColorAzul);
        panelEspacio.add(btnColorRojo);
        panelEspacio.add(btnColorVerde);
        panelEspacio.add(btnColorAmarillo);

        btnCerrarSesion.setColorNormal(new Color(195, 38, 48));
        btnCerrarSesion.setColorHover(new Color(225, 52, 62));
        btnCerrarSesion.setColorPresionado(new Color(155, 25, 35));
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrarSesion.setText("CERRAR SESIÓN");
        btnCerrarSesion.setPreferredSize(new Dimension(210, 40));
        btnCerrarSesion.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas cerrar sesión?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                dispose();
                if (onCerrarSesion != null) {
                    onCerrarSesion.run();
                }
            }
        });

        btnVolver.setColorNormal(new Color(55, 40, 115));
        btnVolver.setColorHover(new Color(75, 55, 145));
        btnVolver.setColorPresionado(new Color(40, 28, 85));
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnVolver.setText("VOLVER");
        btnVolver.setPreferredSize(new Dimension(140, 36));
        btnVolver.addActionListener(e -> dispose());

        aplicarTema();
    }

    private void configurarColor(JButton btn, String color) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(42, 42));
        btn.addActionListener(e -> {
            SesionUsuario.setColorElegido(color);
            aplicarTema();
        });
    }

    private void aplicarTema() {
        String colorUsuario = SesionUsuario.getColorElegido();
        boolean esInvitado = usuario.toLowerCase().startsWith("invitado");
        String tipo = esInvitado ? "CUENTA INVITADO" : "JUGADOR REGISTRADO";
        lblTipoCuenta.setText("● " + tipo + "  |  COLOR: " + colorUsuario);
        lblTipoCuenta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTipoCuenta.setForeground(new Color(180, 165, 245));

        final Color colorBorde;
        final String hexColor;
        switch (colorUsuario.toUpperCase()) {
            case "VERDE":
                colorBorde = new Color(50, 215, 90);
                hexColor = "#48bb78";
                break;
            case "ROJO":
                colorBorde = new Color(245, 60, 60);
                hexColor = "#f56565";
                break;
            case "AMARILLO":
                colorBorde = new Color(255, 210, 40);
                hexColor = "#ecc94b";
                break;
            default:
                colorBorde = new Color(50, 140, 255);
                hexColor = "#4299e1";
                break;
        }

        panelAvatarBorder.setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                int diametro = Math.min(width, height) - 8;
                int px = (width - diametro) / 2;
                int py = (height - diametro) / 2;

                g2.setColor(new Color(colorBorde.getRed(), colorBorde.getGreen(), colorBorde.getBlue(), 60));
                g2.fillOval(px - 3, py - 3, diametro + 6, diametro + 6);

                g2.setColor(new Color(20, 14, 55));
                g2.fillOval(px, py, diametro, diametro);

                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawOval(px, py, diametro, diametro);

                g2.setColor(new Color(255, 215, 64, 180));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(px + 4, py + 4, diametro - 8, diametro - 8);

                BufferedImage imgPawn = Recursos.pawn(colorUsuario);
                if (imgPawn != null) {
                    int bw = 24;
                    int bh = 24;
                    int bx = px + diametro - 20;
                    int by = py + diametro - 20;

                    g2.setColor(new Color(15, 10, 45));
                    g2.fillOval(bx, by, bw, bh);
                    g2.setColor(colorBorde);
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawOval(bx, by, bw, bh);

                    g2.drawImage(imgPawn, bx + 6, by + 3, 12, 18, null);
                }
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(6, 6, 6, 6);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });

        lblPartidas.setText("<html><center><b style='color:#ffffff; font-size:16px;'>" + SesionUsuario.getPartidasJugadas() + "</b><br><span style='color:#b4a5f5; font-size:10px; font-weight:bold;'>PARTIDAS</span></center></html>");
        lblVictorias.setText("<html><center><b style='color:#50e386; font-size:16px;'>" + SesionUsuario.getVictorias() + "</b><br><span style='color:#b4a5f5; font-size:10px; font-weight:bold;'>VICTORIAS</span></center></html>");
        lblRango.setText("<html><center><b style='color:" + hexColor + "; font-size:15px;'>" + colorUsuario + "</b><br><span style='color:#b4a5f5; font-size:10px; font-weight:bold;'>TU COLOR</span></center></html>");

        btnColorAzul.setIcon(crearIconoPawnMini(Recursos.pawn("AZUL"), "AZUL".equals(colorUsuario)));
        btnColorRojo.setIcon(crearIconoPawnMini(Recursos.pawn("ROJO"), "ROJO".equals(colorUsuario)));
        btnColorVerde.setIcon(crearIconoPawnMini(Recursos.pawn("VERDE"), "VERDE".equals(colorUsuario)));
        btnColorAmarillo.setIcon(crearIconoPawnMini(Recursos.pawn("AMARILLO"), "AMARILLO".equals(colorUsuario)));

        panelAvatarBorder.repaint();
        panelStats.repaint();
        panelEspacio.repaint();
    }

    private javax.swing.Icon crearIconoPawnMini(BufferedImage imgPawn, boolean seleccionado) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getIconWidth();
                int h = getIconHeight();
                int arc = 12;

                if (seleccionado) {
                    g2.setColor(new Color(172, 149, 255));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                } else {
                    g2.setColor(new Color(12, 16, 48, 220));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                    g2.setColor(new Color(40, 48, 95));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
                }

                if (imgPawn != null) {
                    int pw = 16;
                    int ph = 28;
                    int px = x + (w - pw) / 2;
                    int py = y + (h - ph) / 2;
                    g2.drawImage(imgPawn, px, py, pw, ph, null);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 40;
            }

            @Override
            public int getIconHeight() {
                return 40;
            }
        };
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelContenido = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        panelAvatarBorder = new javax.swing.JPanel();
        lblAvatar = new javax.swing.JLabel();
        lblNombreUsuario = new javax.swing.JLabel();
        lblTipoCuenta = new javax.swing.JLabel();
        panelStats = new presentacion.PanelInputRedondeado();
        lblPartidas = new javax.swing.JLabel();
        lblVictorias = new javax.swing.JLabel();
        lblRango = new javax.swing.JLabel();
        panelEspacio = new javax.swing.JPanel();
        btnCerrarSesion = new presentacion.BotonPill();
        btnVolver = new presentacion.BotonPill();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Parchís – Mi Perfil");
        setMinimumSize(new java.awt.Dimension(360, 520));
        setPreferredSize(new java.awt.Dimension(360, 520));
        setResizable(false);

        panelFondo.setLayout(new java.awt.GridBagLayout());

        panelContenido.setOpaque(false);
        panelContenido.setPreferredSize(new java.awt.Dimension(320, 480));
        panelContenido.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 8));

        lblTitulo.setFont(new java.awt.Font("Arial Black", 1, 22));
        lblTitulo.setForeground(new java.awt.Color(255, 220, 80));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("MI PERFIL");
        lblTitulo.setPreferredSize(new java.awt.Dimension(300, 28));
        panelContenido.add(lblTitulo);

        panelAvatarBorder.setBackground(new java.awt.Color(20, 15, 65));
        panelAvatarBorder.setPreferredSize(new java.awt.Dimension(90, 90));
        panelAvatarBorder.setLayout(new java.awt.BorderLayout());

        lblAvatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelAvatarBorder.add(lblAvatar, java.awt.BorderLayout.CENTER);

        panelContenido.add(panelAvatarBorder);

        lblNombreUsuario.setFont(new java.awt.Font("Arial Black", 1, 20));
        lblNombreUsuario.setForeground(new java.awt.Color(255, 255, 255));
        lblNombreUsuario.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNombreUsuario.setText(usuario);
        lblNombreUsuario.setPreferredSize(new java.awt.Dimension(300, 26));
        panelContenido.add(lblNombreUsuario);

        lblTipoCuenta.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblTipoCuenta.setForeground(new java.awt.Color(180, 165, 245));
        lblTipoCuenta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTipoCuenta.setText("Cuenta Registrada");
        lblTipoCuenta.setPreferredSize(new java.awt.Dimension(300, 18));
        panelContenido.add(lblTipoCuenta);

        panelStats.setColorFondo(new java.awt.Color(24, 14, 65));
        panelStats.setPreferredSize(new java.awt.Dimension(280, 85));
        panelStats.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 16, 12));

        lblPartidas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPartidas.setText("<html><center><b style='color:#ffffff; font-size:16px;'>0</b><br><span style='color:#b4a5f5; font-size:10px; font-weight:bold;'>PARTIDAS</span></center></html>");
        lblPartidas.setPreferredSize(new java.awt.Dimension(65, 55));
        panelStats.add(lblPartidas);

        lblVictorias.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblVictorias.setText("<html><center><b style='color:#50e386; font-size:16px;'>0</b><br><span style='color:#b4a5f5; font-size:10px; font-weight:bold;'>VICTORIAS</span></center></html>");
        lblVictorias.setPreferredSize(new java.awt.Dimension(65, 55));
        panelStats.add(lblVictorias);

        lblRango.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRango.setText("<html><center><b style='color:#48bb78; font-size:15px;'>VERDE</b><br><span style='color:#b4a5f5; font-size:10px; font-weight:bold;'>TU COLOR</span></center></html>");
        lblRango.setPreferredSize(new java.awt.Dimension(65, 55));
        panelStats.add(lblRango);

        panelContenido.add(panelStats);

        panelEspacio.setOpaque(false);
        panelEspacio.setPreferredSize(new java.awt.Dimension(300, 48));
        panelContenido.add(panelEspacio);

        btnCerrarSesion.setText("CERRAR SESIÓN");
        btnCerrarSesion.setPreferredSize(new java.awt.Dimension(210, 40));
        panelContenido.add(btnCerrarSesion);

        btnVolver.setText("VOLVER");
        btnVolver.setPreferredSize(new java.awt.Dimension(140, 36));
        panelContenido.add(btnVolver);

        panelFondo.add(panelContenido, new java.awt.GridBagConstraints());

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DialogPerfil dialog = new DialogPerfil(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    private presentacion.BotonPill btnCerrarSesion;
    private presentacion.BotonPill btnVolver;
    private javax.swing.JLabel lblAvatar;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblPartidas;
    private javax.swing.JLabel lblRango;
    private javax.swing.JLabel lblTipoCuenta;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblVictorias;
    private javax.swing.JPanel panelAvatarBorder;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelEspacio;
    private presentacion.PanelFondo panelFondo;
    private presentacion.PanelInputRedondeado panelStats;

}
