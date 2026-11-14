package presentacion;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class VistaInicioSesion extends javax.swing.JFrame {

    public VistaInicioSesion() {
        initComponents();
        personalizar();
    }

    private void personalizar() {
        setTitle("Parchís – Inicia Sesión");
        setLocationRelativeTo(null);

        BufferedImage logoImg = Recursos.logoLogin();
        if (logoImg != null) {
            lblLogo.setIcon(Recursos.icono(logoImg, 180, 150));
        }

        BufferedImage titImg = Recursos.tituloParchis();
        if (titImg != null) {
            lblTituloParchis.setIcon(Recursos.icono(titImg, 230, 62));
            lblTituloParchis.setText("");
        }

        BufferedImage mailImg = Recursos.iconMail();
        if (mailImg != null) {
            lblIconMail.setIcon(Recursos.icono(mailImg, 20, 15));
        }

        BufferedImage lockImg = Recursos.iconLock();
        if (lockImg != null) {
            lblIconLock.setIcon(Recursos.icono(lockImg, 18, 18));
        }

        lblLinkRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLinkRegistro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirRegistro();
            }
        });

        lblLinkGuest.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLinkGuest.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirGuest();
            }
        });

        btnIniciarSesion.addActionListener(e -> alIniciarSesion());
    }

    private void alIniciarSesion() {
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (correo.isEmpty() || correo.equalsIgnoreCase("CORREO")) {
            JOptionPane.showMessageDialog(this, "Es obligatorio ingresar tu correo.", "Campo obligatorio", JOptionPane.WARNING_MESSAGE);
            txtCorreo.requestFocus();
            return;
        }

        if (password.isEmpty() || password.equals("CONTRASEÑA")) {
            JOptionPane.showMessageDialog(this, "Es obligatorio ingresar tu contraseña.", "Campo obligatorio", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }

        String nombreUsuario = correo.contains("@") ? correo.substring(0, correo.indexOf('@')) : correo;
        SesionUsuario.setUsuario(nombreUsuario);

        try {
            dto.JugadorDTO j = new dto.JugadorDTO("J1", nombreUsuario, SesionUsuario.getColorElegido(), "avatar", 0, true);
            negocios.FabricaSubsistemas.crearPerfilBO().obtenerOCrear(j);
        } catch (Exception ignored) {
        }

        VistaInicio vistaInicio = new VistaInicio(nombreUsuario);
        vistaInicio.setVisible(true);
        dispose();
    }

    private void abrirRegistro() {
        VistaRegistro registro = new VistaRegistro();
        registro.setVisible(true);
        dispose();
    }

    private void abrirGuest() {
        VistaModoGuest guest = new VistaModoGuest();
        guest.setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelContenido = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblTituloParchis = new javax.swing.JLabel();
        lblIniciaSesion = new javax.swing.JLabel();
        panelEspacio1 = new javax.swing.JPanel();
        panelCorreo = new presentacion.PanelInputRedondeado();
        lblIconMail = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        panelEspacio2 = new javax.swing.JPanel();
        panelPassword = new presentacion.PanelInputRedondeado();
        lblIconLock = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        panelEspacio3 = new javax.swing.JPanel();
        btnIniciarSesion = new presentacion.BotonPill();
        panelEspacio4 = new javax.swing.JPanel();
        lblLinkRegistro = new javax.swing.JLabel();
        lblLinkGuest = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís - Inicia Sesión");
        setMinimumSize(new java.awt.Dimension(380, 720));
        setPreferredSize(new java.awt.Dimension(400, 750));
        setResizable(false);

        panelFondo.setLayout(new java.awt.GridBagLayout());

        panelContenido.setOpaque(false);
        panelContenido.setPreferredSize(new java.awt.Dimension(340, 680));
        panelContenido.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 8));

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setPreferredSize(new java.awt.Dimension(220, 150));
        panelContenido.add(lblLogo);

        lblTituloParchis.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTituloParchis.setPreferredSize(new java.awt.Dimension(280, 65));
        panelContenido.add(lblTituloParchis);

        lblIniciaSesion.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblIniciaSesion.setForeground(new java.awt.Color(255, 255, 255));
        lblIniciaSesion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIniciaSesion.setText("INICIA SESIÓN");
        lblIniciaSesion.setPreferredSize(new java.awt.Dimension(320, 32));
        panelContenido.add(lblIniciaSesion);

        panelEspacio1.setOpaque(false);
        panelEspacio1.setPreferredSize(new java.awt.Dimension(320, 12));
        panelContenido.add(panelEspacio1);

        panelCorreo.setPreferredSize(new java.awt.Dimension(292, 52));
        panelCorreo.setLayout(new java.awt.BorderLayout(12, 0));

        lblIconMail.setPreferredSize(new java.awt.Dimension(24, 24));
        panelCorreo.add(lblIconMail, java.awt.BorderLayout.WEST);

        txtCorreo.setFont(new java.awt.Font("Segoe UI", 1, 13));
        txtCorreo.setForeground(new java.awt.Color(90, 95, 115));
        txtCorreo.setText("CORREO");
        txtCorreo.setBorder(null);
        txtCorreo.setOpaque(false);
        txtCorreo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtCorreo.getText().equals("CORREO")) {
                    txtCorreo.setText("");
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtCorreo.getText().trim().isEmpty()) {
                    txtCorreo.setText("CORREO");
                }
            }
        });
        panelCorreo.add(txtCorreo, java.awt.BorderLayout.CENTER);

        panelContenido.add(panelCorreo);

        panelEspacio2.setOpaque(false);
        panelEspacio2.setPreferredSize(new java.awt.Dimension(320, 10));
        panelContenido.add(panelEspacio2);

        panelPassword.setPreferredSize(new java.awt.Dimension(292, 52));
        panelPassword.setLayout(new java.awt.BorderLayout(12, 0));

        lblIconLock.setPreferredSize(new java.awt.Dimension(24, 24));
        panelPassword.add(lblIconLock, java.awt.BorderLayout.WEST);

        txtPassword.setFont(new java.awt.Font("Segoe UI", 1, 13));
        txtPassword.setForeground(new java.awt.Color(90, 95, 115));
        txtPassword.setText("CONTRASEÑA");
        txtPassword.setEchoChar((char) 0);
        txtPassword.setBorder(null);
        txtPassword.setOpaque(false);
        txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                String pass = new String(txtPassword.getPassword());
                if (pass.equals("CONTRASEÑA")) {
                    txtPassword.setText("");
                    txtPassword.setEchoChar('•');
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                String pass = new String(txtPassword.getPassword());
                if (pass.trim().isEmpty()) {
                    txtPassword.setEchoChar((char) 0);
                    txtPassword.setText("CONTRASEÑA");
                }
            }
        });
        panelPassword.add(txtPassword, java.awt.BorderLayout.CENTER);

        panelContenido.add(panelPassword);

        panelEspacio3.setOpaque(false);
        panelEspacio3.setPreferredSize(new java.awt.Dimension(320, 16));
        panelContenido.add(panelEspacio3);

        btnIniciarSesion.setText("INICIAR SESIÓN    →");
        btnIniciarSesion.setPreferredSize(new java.awt.Dimension(200, 44));
        panelContenido.add(btnIniciarSesion);

        panelEspacio4.setOpaque(false);
        panelEspacio4.setPreferredSize(new java.awt.Dimension(320, 20));
        panelContenido.add(panelEspacio4);

        lblLinkRegistro.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblLinkRegistro.setForeground(new java.awt.Color(220, 225, 255));
        lblLinkRegistro.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLinkRegistro.setText("<html>¿No tienes cuenta? <b style='color:#63b3ed;'>Crear cuenta.</b></html>");
        lblLinkRegistro.setPreferredSize(new java.awt.Dimension(320, 30));
        panelContenido.add(lblLinkRegistro);

        lblLinkGuest.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblLinkGuest.setForeground(new java.awt.Color(180, 195, 245));
        lblLinkGuest.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLinkGuest.setText("<html>¿Entrar sin cuenta? <u style='color:#90cdf4;'>Jugar como Invitado</u></html>");
        lblLinkGuest.setPreferredSize(new java.awt.Dimension(320, 26));
        panelContenido.add(lblLinkGuest);

        panelFondo.add(panelContenido, new java.awt.GridBagConstraints());

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaInicioSesion().setVisible(true);
        });
    }

    private presentacion.BotonPill btnIniciarSesion;
    private javax.swing.JLabel lblIconLock;
    private javax.swing.JLabel lblIconMail;
    private javax.swing.JLabel lblIniciaSesion;
    private javax.swing.JLabel lblLinkGuest;
    private javax.swing.JLabel lblLinkRegistro;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTituloParchis;
    private javax.swing.JPanel panelContenido;
    private presentacion.PanelInputRedondeado panelCorreo;
    private javax.swing.JPanel panelEspacio1;
    private javax.swing.JPanel panelEspacio2;
    private javax.swing.JPanel panelEspacio3;
    private javax.swing.JPanel panelEspacio4;
    private presentacion.PanelFondo panelFondo;
    private presentacion.PanelInputRedondeado panelPassword;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JPasswordField txtPassword;

}
