package presentacion;

import dto.JugadorDTO;
import dto.SalaDTO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import negocios.MatchmakingLocal;
import negocios.ObservadorSala;
import negocios.RedUtil;

public class VistaSalaEspera extends javax.swing.JFrame implements MatchmakingLocal.Listener, ObservadorSala {

    private final int numJugadores;
    private final String usuario;
    private final String color;
    private final String codigoSala;
    private MatchmakingLocal matchmaking;
    private JButton btnIniciar;
    private BotonPill btnListo;

    public VistaSalaEspera() {
        this(2, "Jugador", "AZUL");
    }

    public VistaSalaEspera(int numJugadores, String usuario, String color) {
        this.numJugadores = numJugadores;
        this.usuario = (usuario != null && !usuario.isEmpty()) ? usuario : "Jugador";
        this.color = (color != null && !color.isEmpty()) ? color : "AZUL";
        this.codigoSala = RedUtil.obtenerCodigo();
        initComponents();
        personalizar();
        iniciarMatchmaking();
    }

    private void personalizar() {
        setTitle("Parchís – Sala de Espera (" + numJugadores + " Jugadores)");
        setLocationRelativeTo(null);

        BufferedImage boardImg = Recursos.boardModal();
        if (boardImg != null) {
            lblBoard.setIcon(Recursos.icono(boardImg, 140, 115));
        }
        lblBoard.setPreferredSize(new Dimension(180, 115));
        lblConteo.setPreferredSize(new Dimension(330, 18));

        lblTitulo.setText("SALA DE ESPERA");
        lblTitulo.setFont(new Font("Arial Black", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(255, 220, 80));

        lblSubtitulo.setText("CÓDIGO DE SALA: " + codigoSala + " • CLIC PARA COPIAR");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSubtitulo.setForeground(new Color(175, 150, 255));
        lblSubtitulo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSubtitulo.setToolTipText("Haz clic para copiar tu código de sala (" + codigoSala + ") y pasárselo a tu amigo");
        lblSubtitulo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(codigoSala), null);
                JOptionPane.showMessageDialog(VistaSalaEspera.this,
                        "¡Código de sala " + codigoSala + " copiado!\nPásaselo a tu amigo para que lo ingrese en '+ Unirse con código'.",
                        "Código de Sala Copiado", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnCodigo = new JButton("+ Unirse con código");
        btnCodigo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCodigo.setForeground(new Color(180, 165, 245));
        btnCodigo.setContentAreaFilled(false);
        btnCodigo.setBorderPainted(false);
        btnCodigo.setFocusPainted(false);
        btnCodigo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCodigo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnCodigo.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnCodigo.setForeground(new Color(180, 165, 245));
            }
        });
        btnCodigo.addActionListener(e -> {
            String entrada = JOptionPane.showInputDialog(this,
                    "Ingresa el código de sala de tu amigo (ejemplo: " + codigoSala + "):",
                    "Unirse con Código de Sala",
                    JOptionPane.QUESTION_MESSAGE);
            if (entrada != null && !entrada.trim().isEmpty()) {
                if (matchmaking != null) {
                    matchmaking.cancelar();
                }
                String codigoLimpio = entrada.trim().toUpperCase();
                lblSubtitulo.setText("CONECTANDO A SALA " + codigoLimpio + "...");
                lblSubtitulo.setForeground(new Color(255, 220, 100));
                if (btnIniciar != null) {
                    btnIniciar.setVisible(false);
                }
                actualizarBotonListo(false);
                matchmaking = new MatchmakingLocal(numJugadores, usuario, color, codigoLimpio, false, this);
                matchmaking.iniciar();
            }
        });
        panelHeader.add(btnCodigo, BorderLayout.EAST);

        lblContador.setText("1 / " + numJugadores + " Jugadores conectados");
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblContador.setForeground(Color.WHITE);

        if (numJugadores == 2) {
            panelSlot3.setVisible(false);
            panelSlot4.setVisible(false);
            panelSlots.setPreferredSize(new Dimension(330, 134));
        } else {
            panelSlots.setPreferredSize(new Dimension(330, 266));
            lblBoard.setPreferredSize(new Dimension(160, 96));
            if (boardImg != null) {
                lblBoard.setIcon(Recursos.icono(boardImg, 128, 96));
            }
        }

        estilizarSlot(panelSlot1, true, true);
        estilizarSlot(panelSlot2, false, false);
        estilizarSlot(panelSlot3, false, false);
        estilizarSlot(panelSlot4, false, false);

        configurarSlot(panelSlot1, lblSlot1Avatar, lblSlot1Nombre, lblSlot1Estado, usuario + " (Tú)", color, true, false, true);

        MouseAdapter clicMiSlot = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alAlternarListo();
            }
        };
        panelSlot1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSlot1.addMouseListener(clicMiSlot);
        panelSlot2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSlot2.addMouseListener(clicMiSlot);
        panelSlot3.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSlot3.addMouseListener(clicMiSlot);
        panelSlot4.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSlot4.addMouseListener(clicMiSlot);

        btnCancelar.setText("← Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setForeground(new Color(180, 165, 245));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnCancelar.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnCancelar.setForeground(new Color(180, 165, 245));
            }
        });
        btnCancelar.addActionListener(e -> alCancelar());

        btnIniciar = new JButton();
        BufferedImage imgIniciar = Recursos.btnIniciar();
        if (imgIniciar != null) {
            btnIniciar.setText("");
            btnIniciar.setIcon(Recursos.icono(imgIniciar, 210, 56));
            btnIniciar.setContentAreaFilled(false);
            btnIniciar.setBorder(null);
            btnIniciar.setFocusPainted(false);
            btnIniciar.setPreferredSize(new Dimension(210, 56));
        } else {
            btnIniciar.setText("INICIAR");
            btnIniciar.setPreferredSize(new Dimension(210, 50));
        }
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciar.addActionListener(e -> {
            if (matchmaking != null) {
                if (!matchmaking.todosListos()) {
                    JOptionPane.showMessageDialog(this,
                            "Todos los jugadores en la sala deben marcar 'ESTOY LISTO' antes de iniciar.",
                            "Jugadores no listos",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                btnIniciar.setEnabled(false);
                matchmaking.iniciarAhora();
            }
        });
        panelContenido.add(btnIniciar);

        btnListo = new BotonPill("ESTOY LISTO");
        btnListo.setColorNormal(new Color(245, 190, 25));
        btnListo.setColorHover(new Color(255, 210, 50));
        btnListo.setColorPresionado(new Color(185, 135, 10));
        btnListo.setForeground(new Color(25, 18, 55));
        btnListo.setFont(new Font("Arial Black", Font.BOLD, 14));
        btnListo.setPreferredSize(new Dimension(210, 44));
        btnListo.addActionListener(e -> alAlternarListo());
        panelContenido.add(btnListo);
    }

    private void alAlternarListo() {
        if (matchmaking != null) {
            matchmaking.alternarListo();
            actualizarBotonListo(matchmaking.isMiListo());
        }
    }

    private void actualizarBotonListo(boolean listo) {
        if (btnListo == null) return;
        if (listo) {
            btnListo.setText("● LISTO (CAMBIAR)");
            btnListo.setColorNormal(new Color(60, 200, 95));
            btnListo.setColorHover(new Color(75, 220, 110));
            btnListo.setColorPresionado(new Color(40, 150, 65));
            btnListo.setForeground(Color.WHITE);
        } else {
            btnListo.setText("ESTOY LISTO");
            btnListo.setColorNormal(new Color(245, 190, 25));
            btnListo.setColorHover(new Color(255, 210, 50));
            btnListo.setColorPresionado(new Color(185, 135, 10));
            btnListo.setForeground(new Color(25, 18, 55));
        }
        btnListo.repaint();
    }

    private void estilizarSlot(JPanel slot, boolean conectado, boolean esLocal) {
        slot.setOpaque(false);
        slot.setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 20;
                if (conectado) {
                    g2.setColor(new Color(24, 14, 65, 230));
                    g2.fillRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                    g2.setColor(esLocal ? new Color(172, 149, 255) : new Color(100, 255, 120));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                } else {
                    g2.setColor(new Color(10, 16, 48, 200));
                    g2.fillRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                    g2.setColor(new Color(35, 45, 95));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
                }
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(6, 12, 6, 12);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });
    }

    private void configurarSlot(JPanel panel, JLabel lblAvatar, JLabel lblNombre, JLabel lblEstado, String nombre, String colorJugador, boolean conectado, boolean listo, boolean esLocal) {
        lblNombre.setText(nombre);
        if (conectado) {
            if (listo) {
                lblEstado.setText("● LISTO");
                lblEstado.setForeground(new Color(120, 255, 120));
            } else {
                lblEstado.setText("● NO LISTO");
                lblEstado.setForeground(new Color(255, 215, 70));
            }
            BufferedImage imgPawn = Recursos.pawn(colorJugador);
            if (imgPawn != null) {
                lblAvatar.setIcon(Recursos.icono(imgPawn, 22, 38));
            } else {
                BufferedImage av = Recursos.perfil();
                lblAvatar.setIcon(Recursos.icono(av, 34, 34));
            }
            estilizarSlot(panel, true, esLocal);
        } else {
            lblEstado.setText("BUSCANDO...");
            lblEstado.setForeground(new Color(255, 220, 100));
            lblAvatar.setIcon(null);
            estilizarSlot(panel, false, false);
        }
        panel.repaint();
    }

    private void iniciarMatchmaking() {
        matchmaking = new MatchmakingLocal(numJugadores, usuario, color, codigoSala, true, this);
        matchmaking.iniciar();
    }

    @Override
    public void onJugadorUnido(String id, String nombre, String colorJugador, boolean listo, int conectados, int requeridos) {
        SwingUtilities.invokeLater(() -> {
            lblContador.setText(conectados + " / " + requeridos + " Jugadores conectados");
            String miId = (matchmaking != null) ? matchmaking.getMiIdLocal() : "J1";
            boolean esHost = (matchmaking == null) || matchmaking.isEsHost();
            if (btnIniciar != null) {
                btnIniciar.setVisible(esHost);
            }

            boolean soyYo = id.equals(miId);
            if (soyYo) {
                actualizarBotonListo(listo);
            }
            String etiqueta = soyYo ? (nombre + " (Tú)") : nombre;

            if ("J1".equals(id)) {
                configurarSlot(panelSlot1, lblSlot1Avatar, lblSlot1Nombre, lblSlot1Estado, etiqueta, colorJugador, true, listo, soyYo);
            } else if ("J2".equals(id)) {
                configurarSlot(panelSlot2, lblSlot2Avatar, lblSlot2Nombre, lblSlot2Estado, etiqueta, colorJugador, true, listo, soyYo);
            } else if ("J3".equals(id)) {
                configurarSlot(panelSlot3, lblSlot3Avatar, lblSlot3Nombre, lblSlot3Estado, etiqueta, colorJugador, true, listo, soyYo);
            } else if ("J4".equals(id)) {
                configurarSlot(panelSlot4, lblSlot4Avatar, lblSlot4Nombre, lblSlot4Estado, etiqueta, colorJugador, true, listo, soyYo);
            }

            if (conectados >= 2 && !esHost) {
                lblSubtitulo.setText(listo ? "¡LISTO! ESPERANDO AL ANFITRIÓN..." : "CONECTADO • PRESIONA 'ESTOY LISTO'");
                lblSubtitulo.setForeground(new Color(120, 255, 120));
            }
        });
    }

    @Override
    public void onConteoRegresivo(int segundos) {
        SwingUtilities.invokeLater(() -> {
            lblSubtitulo.setText("¡PARTIDA ENCONTRADA!");
            lblSubtitulo.setForeground(new Color(120, 255, 120));
            lblConteo.setText("Iniciando en " + segundos + "...");
            btnCancelar.setEnabled(false);
        });
    }

    @Override
    public void onIniciarPartida(ModeloPartida modelo, String idLocal) {
        SwingUtilities.invokeLater(() -> {
            ControladorPartida ctrl = new ControladorPartida(idLocal, modelo);
            VistaPartida vistaPartida = new VistaPartida(idLocal, "Parchís – " + usuario + " (" + color + ")", ctrl, modelo);
            vistaPartida.setLocationRelativeTo(null);
            vistaPartida.setVisible(true);
            dispose();
        });
    }

    @Override
    public void onError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            lblSubtitulo.setText(mensaje);
            lblSubtitulo.setForeground(new Color(255, 110, 110));
        });
    }

    @Override
    public void salaActualizada(SalaDTO sala) {
        if (sala == null) return;
        SwingUtilities.invokeLater(() -> {
            List<JugadorDTO> list = sala.getJugadores();
            for (JugadorDTO j : list) {
                onJugadorUnido(j.getIdJugador(), j.getNombreUsuario(), j.getColor(), j.isActivo(), list.size(), sala.getRequeridos());
            }
        });
    }

    @Override
    public void conteoRegresivo(int seg) {
        onConteoRegresivo(seg);
    }

    @Override
    public void partidaIniciada(ModeloPartida mod, String idLocal) {
        onIniciarPartida(mod, idLocal);
    }

    @Override
    public void errorRecibido(String mensaje) {
        onError(mensaje);
    }

    private void alCancelar() {
        if (matchmaking != null) {
            matchmaking.cancelar();
        }
        VistaInicio inicio = new VistaInicio(usuario);
        inicio.setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelContenido = new javax.swing.JPanel();
        panelHeader = new javax.swing.JPanel();
        btnCancelar = new javax.swing.JButton();
        lblBoard = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblContador = new javax.swing.JLabel();
        panelSlots = new javax.swing.JPanel();
        panelSlot1 = new javax.swing.JPanel();
        lblSlot1Avatar = new javax.swing.JLabel();
        lblSlot1Nombre = new javax.swing.JLabel();
        lblSlot1Estado = new javax.swing.JLabel();
        panelSlot2 = new javax.swing.JPanel();
        lblSlot2Avatar = new javax.swing.JLabel();
        lblSlot2Nombre = new javax.swing.JLabel();
        lblSlot2Estado = new javax.swing.JLabel();
        panelSlot3 = new javax.swing.JPanel();
        lblSlot3Avatar = new javax.swing.JLabel();
        lblSlot3Nombre = new javax.swing.JLabel();
        lblSlot3Estado = new javax.swing.JLabel();
        panelSlot4 = new javax.swing.JPanel();
        lblSlot4Avatar = new javax.swing.JLabel();
        lblSlot4Nombre = new javax.swing.JLabel();
        lblSlot4Estado = new javax.swing.JLabel();
        lblConteo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís - Sala de Espera");
        setMinimumSize(new java.awt.Dimension(380, 720));
        setPreferredSize(new java.awt.Dimension(400, 750));
        setResizable(false);

        panelFondo.setLayout(new java.awt.GridBagLayout());

        panelContenido.setOpaque(false);
        panelContenido.setPreferredSize(new java.awt.Dimension(350, 700));
        panelContenido.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 6));

        panelHeader.setOpaque(false);
        panelHeader.setPreferredSize(new java.awt.Dimension(330, 32));
        panelHeader.setLayout(new java.awt.BorderLayout());

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnCancelar.setForeground(new java.awt.Color(180, 165, 245));
        btnCancelar.setText("← Cancelar");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setFocusPainted(false);
        panelHeader.add(btnCancelar, java.awt.BorderLayout.WEST);

        panelContenido.add(panelHeader);

        lblBoard.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBoard.setPreferredSize(new java.awt.Dimension(180, 120));
        panelContenido.add(lblBoard);

        lblTitulo.setFont(new java.awt.Font("Arial Black", 1, 22));
        lblTitulo.setForeground(new java.awt.Color(255, 220, 80));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("SALA DE ESPERA");
        lblTitulo.setPreferredSize(new java.awt.Dimension(330, 28));
        panelContenido.add(lblTitulo);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblSubtitulo.setForeground(new java.awt.Color(150, 123, 242));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("BUSCANDO OPONENTES...");
        lblSubtitulo.setPreferredSize(new java.awt.Dimension(330, 20));
        panelContenido.add(lblSubtitulo);

        lblContador.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblContador.setForeground(new java.awt.Color(255, 255, 255));
        lblContador.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblContador.setText("1 / 2 Jugadores");
        lblContador.setPreferredSize(new java.awt.Dimension(330, 22));
        panelContenido.add(lblContador);

        panelSlots.setOpaque(false);
        panelSlots.setPreferredSize(new java.awt.Dimension(330, 266));
        panelSlots.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 8));

        panelSlot1.setOpaque(false);
        panelSlot1.setPreferredSize(new java.awt.Dimension(320, 56));
        panelSlot1.setLayout(new java.awt.BorderLayout(10, 0));

        lblSlot1Avatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot1Avatar.setPreferredSize(new java.awt.Dimension(50, 56));
        panelSlot1.add(lblSlot1Avatar, java.awt.BorderLayout.WEST);

        lblSlot1Nombre.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSlot1Nombre.setForeground(new java.awt.Color(255, 255, 255));
        lblSlot1Nombre.setText("Jugador (Tú)");
        panelSlot1.add(lblSlot1Nombre, java.awt.BorderLayout.CENTER);

        lblSlot1Estado.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblSlot1Estado.setForeground(new java.awt.Color(255, 215, 70));
        lblSlot1Estado.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSlot1Estado.setText("● NO LISTO");
        lblSlot1Estado.setPreferredSize(new java.awt.Dimension(90, 56));
        panelSlot1.add(lblSlot1Estado, java.awt.BorderLayout.EAST);

        panelSlots.add(panelSlot1);

        panelSlot2.setOpaque(false);
        panelSlot2.setPreferredSize(new java.awt.Dimension(320, 56));
        panelSlot2.setLayout(new java.awt.BorderLayout(10, 0));

        lblSlot2Avatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot2Avatar.setPreferredSize(new java.awt.Dimension(50, 56));
        panelSlot2.add(lblSlot2Avatar, java.awt.BorderLayout.WEST);

        lblSlot2Nombre.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSlot2Nombre.setForeground(new java.awt.Color(200, 200, 200));
        lblSlot2Nombre.setText("Esperando rival...");
        panelSlot2.add(lblSlot2Nombre, java.awt.BorderLayout.CENTER);

        lblSlot2Estado.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblSlot2Estado.setForeground(new java.awt.Color(255, 220, 100));
        lblSlot2Estado.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSlot2Estado.setText("BUSCANDO...");
        lblSlot2Estado.setPreferredSize(new java.awt.Dimension(90, 56));
        panelSlot2.add(lblSlot2Estado, java.awt.BorderLayout.EAST);

        panelSlots.add(panelSlot2);

        panelSlot3.setOpaque(false);
        panelSlot3.setPreferredSize(new java.awt.Dimension(320, 56));
        panelSlot3.setLayout(new java.awt.BorderLayout(10, 0));

        lblSlot3Avatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot3Avatar.setPreferredSize(new java.awt.Dimension(50, 56));
        panelSlot3.add(lblSlot3Avatar, java.awt.BorderLayout.WEST);

        lblSlot3Nombre.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSlot3Nombre.setForeground(new java.awt.Color(200, 200, 200));
        lblSlot3Nombre.setText("Esperando jugador...");
        panelSlot3.add(lblSlot3Nombre, java.awt.BorderLayout.CENTER);

        lblSlot3Estado.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblSlot3Estado.setForeground(new java.awt.Color(255, 220, 100));
        lblSlot3Estado.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSlot3Estado.setText("BUSCANDO...");
        lblSlot3Estado.setPreferredSize(new java.awt.Dimension(90, 56));
        panelSlot3.add(lblSlot3Estado, java.awt.BorderLayout.EAST);

        panelSlots.add(panelSlot3);

        panelSlot4.setOpaque(false);
        panelSlot4.setPreferredSize(new java.awt.Dimension(320, 56));
        panelSlot4.setLayout(new java.awt.BorderLayout(10, 0));

        lblSlot4Avatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot4Avatar.setPreferredSize(new java.awt.Dimension(50, 56));
        panelSlot4.add(lblSlot4Avatar, java.awt.BorderLayout.WEST);

        lblSlot4Nombre.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSlot4Nombre.setForeground(new java.awt.Color(200, 200, 200));
        lblSlot4Nombre.setText("Esperando jugador...");
        panelSlot4.add(lblSlot4Nombre, java.awt.BorderLayout.CENTER);

        lblSlot4Estado.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblSlot4Estado.setForeground(new java.awt.Color(255, 220, 100));
        lblSlot4Estado.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSlot4Estado.setText("BUSCANDO...");
        lblSlot4Estado.setPreferredSize(new java.awt.Dimension(90, 56));
        panelSlot4.add(lblSlot4Estado, java.awt.BorderLayout.EAST);

        panelSlots.add(panelSlot4);

        panelContenido.add(panelSlots);

        lblConteo.setFont(new java.awt.Font("Arial Black", 1, 14));
        lblConteo.setForeground(new java.awt.Color(120, 255, 120));
        lblConteo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblConteo.setText(" ");
        lblConteo.setPreferredSize(new java.awt.Dimension(330, 14));
        panelContenido.add(lblConteo);

        panelFondo.add(panelContenido, new java.awt.GridBagConstraints());

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaSalaEspera(2, "Jugador", "AZUL").setVisible(true);
        });
    }

    private javax.swing.JButton btnCancelar;
    private javax.swing.JLabel lblBoard;
    private javax.swing.JLabel lblContador;
    private javax.swing.JLabel lblConteo;
    private javax.swing.JLabel lblSlot1Avatar;
    private javax.swing.JLabel lblSlot1Estado;
    private javax.swing.JLabel lblSlot1Nombre;
    private javax.swing.JLabel lblSlot2Avatar;
    private javax.swing.JLabel lblSlot2Estado;
    private javax.swing.JLabel lblSlot2Nombre;
    private javax.swing.JLabel lblSlot3Avatar;
    private javax.swing.JLabel lblSlot3Estado;
    private javax.swing.JLabel lblSlot3Nombre;
    private javax.swing.JLabel lblSlot4Avatar;
    private javax.swing.JLabel lblSlot4Estado;
    private javax.swing.JLabel lblSlot4Nombre;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelContenido;
    private presentacion.PanelFondo panelFondo;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelSlot1;
    private javax.swing.JPanel panelSlot2;
    private javax.swing.JPanel panelSlot3;
    private javax.swing.JPanel panelSlot4;
    private javax.swing.JPanel panelSlots;

}
