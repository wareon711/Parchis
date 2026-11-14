package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class VistaModoGuest extends javax.swing.JFrame {

    public VistaModoGuest() {
        initComponents();
        personalizar();
    }

    private void personalizar() {
        setTitle("Parchís – Modo Guest");
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

        BufferedImage usrImg = Recursos.iconUser();
        if (usrImg != null) {
            lblIconUser.setIcon(Recursos.icono(usrImg, 18, 20));
        }

        lblLinkLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLinkLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirLogin();
            }
        });

        btnComenzar.addActionListener(e -> alComenzar());
    }

    private void alComenzar() {
        String usuario = txtUsuario.getText().trim();
        if (usuario.isEmpty() || usuario.equalsIgnoreCase("NOMBRE DE USUARIO")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Es obligatorio ingresar un nombre de usuario para continuar.",
                    "Campo obligatorio",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            txtUsuario.requestFocus();
            return;
        }
        SesionUsuario.setUsuario(usuario);
        VistaInicio vistaInicio = new VistaInicio(usuario);
        vistaInicio.setVisible(true);
        dispose();
    }

    private void abrirLogin() {
        VistaInicioSesion login = new VistaInicioSesion();
        login.setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelContenido = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblTituloParchis = new javax.swing.JLabel();
        lblModoGuest = new javax.swing.JLabel();
        panelEspacio1 = new javax.swing.JPanel();
        panelInput = new presentacion.PanelInputRedondeado();
        lblIconUser = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        panelEspacio2 = new javax.swing.JPanel();
        btnComenzar = new presentacion.BotonPill();
        panelEspacio3 = new javax.swing.JPanel();
        lblLinkLogin = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís - Modo Guest");
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

        lblModoGuest.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblModoGuest.setForeground(new java.awt.Color(255, 255, 255));
        lblModoGuest.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblModoGuest.setText("MODO GUEST");
        lblModoGuest.setPreferredSize(new java.awt.Dimension(320, 32));
        panelContenido.add(lblModoGuest);

        panelEspacio1.setOpaque(false);
        panelEspacio1.setPreferredSize(new java.awt.Dimension(320, 16));
        panelContenido.add(panelEspacio1);

        panelInput.setPreferredSize(new java.awt.Dimension(292, 52));
        panelInput.setLayout(new java.awt.BorderLayout(12, 0));

        lblIconUser.setPreferredSize(new java.awt.Dimension(24, 24));
        panelInput.add(lblIconUser, java.awt.BorderLayout.WEST);

        txtUsuario.setFont(new java.awt.Font("Segoe UI", 1, 13));
        txtUsuario.setForeground(new java.awt.Color(90, 95, 115));
        txtUsuario.setText("NOMBRE DE USUARIO");
        txtUsuario.setBorder(null);
        txtUsuario.setOpaque(false);
        txtUsuario.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtUsuario.getText().equals("NOMBRE DE USUARIO")) {
                    txtUsuario.setText("");
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtUsuario.getText().trim().isEmpty()) {
                    txtUsuario.setText("NOMBRE DE USUARIO");
                }
            }
        });
        panelInput.add(txtUsuario, java.awt.BorderLayout.CENTER);

        panelContenido.add(panelInput);

        panelEspacio2.setOpaque(false);
        panelEspacio2.setPreferredSize(new java.awt.Dimension(320, 16));
        panelContenido.add(panelEspacio2);

        btnComenzar.setText("COMENZAR    →");
        btnComenzar.setPreferredSize(new java.awt.Dimension(180, 44));
        panelContenido.add(btnComenzar);

        panelEspacio3.setOpaque(false);
        panelEspacio3.setPreferredSize(new java.awt.Dimension(320, 40));
        panelContenido.add(panelEspacio3);

        lblLinkLogin.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblLinkLogin.setForeground(new java.awt.Color(220, 225, 255));
        lblLinkLogin.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLinkLogin.setText("<html>¿Ya tienes una cuenta? <b style='color:#63b3ed;'>Inicia sesión.</b></html>");
        lblLinkLogin.setPreferredSize(new java.awt.Dimension(320, 40));
        panelContenido.add(lblLinkLogin);

        panelFondo.add(panelContenido, new java.awt.GridBagConstraints());

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaModoGuest().setVisible(true);
        });
    }

    private presentacion.BotonPill btnComenzar;
    private javax.swing.JLabel lblIconUser;
    private javax.swing.JLabel lblLinkLogin;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblModoGuest;
    private javax.swing.JLabel lblTituloParchis;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelEspacio1;
    private javax.swing.JPanel panelEspacio2;
    private javax.swing.JPanel panelEspacio3;
    private presentacion.PanelFondo panelFondo;
    private presentacion.PanelInputRedondeado panelInput;
    private javax.swing.JTextField txtUsuario;

}
