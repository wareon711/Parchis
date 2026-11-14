package presentacion;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;

public class VistaInicio extends javax.swing.JFrame {

    private String usuario = SesionUsuario.getUsuario();

    public VistaInicio() {
        this(SesionUsuario.getUsuario());
    }

    public VistaInicio(String usuario) {
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w instanceof VistaInicio && w != this) {
                w.dispose();
            }
        }
        this.usuario = usuario != null && !usuario.isEmpty() ? usuario : SesionUsuario.getUsuario();
        SesionUsuario.setUsuario(this.usuario);
        initComponents();
        personalizar();
    }

    private void personalizar() {
        setTitle("Parchís – Inicio");
        setLocationRelativeTo(null);

        BufferedImage imgCuenta = Recursos.btnCuenta();
        if (imgCuenta != null) {
            configurarBotonAnimado(btnPerfil, imgCuenta, 42, 45, this::abrirPerfil);
        } else {
            btnPerfil.addActionListener(e -> abrirPerfil());
        }
        btnPerfil.setToolTipText("Mi Perfil / Cerrar Sesión");

        BufferedImage img2J = Recursos.btnModo2J();
        configurarBotonAnimado(btnModo2J, img2J, 220, 254, () -> {
            VistaSeleccionarEntrada vista2j = new VistaSeleccionarEntrada(2, usuario);
            vista2j.setVisible(true);
            dispose();
        });

        BufferedImage img4J = Recursos.btnModo4J();
        configurarBotonAnimado(btnModo4J, img4J, 220, 256, () -> {
            VistaSeleccionarEntrada vista4j = new VistaSeleccionarEntrada(4, usuario);
            vista4j.setVisible(true);
            dispose();
        });
    }

    private void configurarBotonAnimado(JButton btn, BufferedImage img, int wBase, int hBase, Runnable accion) {
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final boolean[] animandoClick = {false};

        if (img != null) {
            btn.setIcon(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    boolean presionado = btn.getModel().isPressed() || animandoClick[0];
                    boolean hover = btn.getModel().isRollover() && !presionado;

                    double escala = presionado ? 0.93 : (hover ? 1.03 : 1.0);
                    int offsetY = presionado ? 6 : (hover ? -3 : 0);

                    int dw = (int) Math.round(wBase * escala);
                    int dh = (int) Math.round(hBase * escala);
                    int dx = (c.getWidth() - dw) / 2;
                    int dy = (c.getHeight() - dh) / 2 + offsetY;

                    g2.drawImage(img, dx, dy, dw, dh, null);

                    if (presionado) {
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.14f));
                        g2.setColor(java.awt.Color.BLACK);
                        g2.fillRoundRect(dx, dy, dw, dh, 28, 28);
                    }
                    g2.dispose();
                }

                @Override
                public int getIconWidth() {
                    return wBase;
                }

                @Override
                public int getIconHeight() {
                    return hBase;
                }
            });
        }

        btn.getModel().addChangeListener(e -> btn.repaint());

        btn.addActionListener(e -> {
            if (animandoClick[0]) {
                return;
            }
            animandoClick[0] = true;
            SonidoUtil.paso();
            btn.repaint();
            Timer t = new Timer(115, ev -> {
                animandoClick[0] = false;
                btn.repaint();
                accion.run();
            });
            t.setRepeats(false);
            t.start();
        });
    }

    private void abrirPerfil() {
        DialogPerfil dlg = new DialogPerfil(this, usuario, this::alCerrarSesion);
        dlg.setVisible(true);
    }

    private void alCerrarSesion() {
        VistaInicioSesion login = new VistaInicioSesion();
        login.setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelHeader = new presentacion.PanelHeaderInicio();
        btnPerfil = new javax.swing.JButton();
        panelCenter = new javax.swing.JPanel();
        panelCards = new javax.swing.JPanel();
        btnModo2J = new javax.swing.JButton();
        btnModo4J = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís - Inicio");
        setMinimumSize(new java.awt.Dimension(360, 720));
        setPreferredSize(new java.awt.Dimension(384, 780));
        setResizable(false);

        panelFondo.setLayout(new java.awt.BorderLayout());

        panelHeader.setPreferredSize(new java.awt.Dimension(384, 66));
        panelHeader.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 11));

        btnPerfil.setBorder(null);
        btnPerfil.setContentAreaFilled(false);
        btnPerfil.setFocusPainted(false);
        btnPerfil.setPreferredSize(new java.awt.Dimension(44, 46));
        panelHeader.add(btnPerfil);

        panelFondo.add(panelHeader, java.awt.BorderLayout.NORTH);

        panelCenter.setOpaque(false);
        panelCenter.setLayout(new java.awt.GridBagLayout());

        panelCards.setOpaque(false);
        panelCards.setPreferredSize(new java.awt.Dimension(340, 620));
        panelCards.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 24));

        btnModo2J.setContentAreaFilled(false);
        btnModo2J.setBorder(null);
        btnModo2J.setFocusPainted(false);
        btnModo2J.setPreferredSize(new java.awt.Dimension(240, 260));
        panelCards.add(btnModo2J);

        btnModo4J.setContentAreaFilled(false);
        btnModo4J.setBorder(null);
        btnModo4J.setFocusPainted(false);
        btnModo4J.setPreferredSize(new java.awt.Dimension(240, 260));
        panelCards.add(btnModo4J);

        panelCenter.add(panelCards, new java.awt.GridBagConstraints());

        panelFondo.add(panelCenter, java.awt.BorderLayout.CENTER);

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaInicio().setVisible(true);
        });
    }

    private javax.swing.JButton btnModo2J;
    private javax.swing.JButton btnModo4J;
    private javax.swing.JButton btnPerfil;
    private javax.swing.JPanel panelCards;
    private javax.swing.JPanel panelCenter;
    private presentacion.PanelFondo panelFondo;
    private presentacion.PanelHeaderInicio panelHeader;

}
