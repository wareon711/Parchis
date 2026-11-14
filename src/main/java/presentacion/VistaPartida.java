package presentacion;

import dto.EstadoPartidaDTO;
import dto.JugadorDTO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class VistaPartida extends javax.swing.JFrame implements IObservadorPartida {

    public static final int TIEMPO_TURNO_MS = 15000;

    private String idJugadorLocal;
    private String colorLocal;
    private ControladorPartida controlador;
    private ModeloPartida modelo;
    private int ultimaTiradaId = 0;
    private boolean resultadoMostrado = false;

    private Timer timerTurno;
    private Timer timerPausa;
    private Timer timerResultado;
    private long inicioTurno;
    private String turnoActual;

    private PanelJugador[] panelesJugadores;
    private ControlDado[] controlesDados;

    public VistaPartida() {
        this("J1", "Parchís", null, null);
    }

    public VistaPartida(String idJugadorLocal, String titulo, ControladorPartida controlador, ModeloPartida modelo) {
        this.idJugadorLocal = idJugadorLocal != null ? idJugadorLocal : "J1";
        this.colorLocal = SesionUsuario.getColorElegido();
        if (modelo != null && modelo.getEstadoActual() != null) {
            for (JugadorDTO j : modelo.getEstadoActual().getJugadores()) {
                if (this.idJugadorLocal.equals(j.getIdJugador()) && j.getColor() != null) {
                    this.colorLocal = j.getColor();
                    SesionUsuario.setColorElegido(this.colorLocal);
                    break;
                }
            }
        }
        this.controlador = controlador;
        this.modelo = modelo;
        initComponents();
        inicializar(titulo);
    }

    private void inicializar(String titulo) {
        setTitle(titulo);
        setLocationRelativeTo(null);

        btnSalir.setText("← Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSalir.setForeground(new Color(180, 165, 245));
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSalir.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSalir.setForeground(new Color(180, 165, 245));
            }
        });
        btnSalir.addActionListener(e -> alSalir());

        lblTurno.setText("PARTIDA EN CURSO");
        lblTurno.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTurno.setForeground(new Color(255, 220, 80));
        lblTurno.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        topCentro.removeAll();
        topCentro.setLayout(new java.awt.BorderLayout());
        topCentro.add(lblTurno, java.awt.BorderLayout.CENTER);

        BotonPill btnReaccion = new BotonPill("Chat");
        btnReaccion.setIcon(crearIconoChat());
        btnReaccion.setIconTextGap(6);
        btnReaccion.setColorNormal(new Color(42, 25, 96));
        btnReaccion.setColorHover(new Color(68, 42, 145));
        btnReaccion.setColorPresionado(new Color(28, 16, 68));
        btnReaccion.setColorBorde(new Color(172, 149, 255, 180));
        btnReaccion.setForeground(new Color(255, 230, 110));
        btnReaccion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReaccion.setMargin(new java.awt.Insets(0, 8, 0, 8));
        btnReaccion.setPreferredSize(new java.awt.Dimension(92, 30));
        btnReaccion.setToolTipText("Enviar mensaje rápido");
        btnReaccion.addActionListener(e -> {
            javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
            menu.setBackground(new Color(22, 13, 56));
            menu.setBorder(new javax.swing.border.Border() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(165, 135, 255));
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, 14, 14);
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

            OpcionMensaje[] opciones = {
                new OpcionMensaje("¡Buena jugada!", crearIconoBadge("ESTRELLA", new Color(68, 48, 8), new Color(255, 215, 65))),
                new OpcionMensaje("¡Apúrate!", crearIconoBadge("RAYO", new Color(75, 40, 8), new Color(255, 160, 30))),
                new OpcionMensaje("¡Nooo!", crearIconoBadge("EXCLAMACION", new Color(75, 16, 28), new Color(255, 80, 105))),
                new OpcionMensaje("¡Suerte!", crearIconoBadge("TREBOL", new Color(10, 55, 45), new Color(45, 220, 185)))
            };

            for (OpcionMensaje op : opciones) {
                javax.swing.JMenuItem item = new javax.swing.JMenuItem(op.texto, op.icono) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int w = getWidth();
                        int h = getHeight();
                        if (getModel().isArmed() || getModel().isRollover()) {
                            g2.setColor(new Color(72, 45, 155));
                            g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
                            g2.setColor(new Color(185, 155, 255));
                            g2.setStroke(new BasicStroke(1.0f));
                            g2.drawRoundRect(2, 2, w - 5, h - 5, 10, 10);
                            setForeground(new Color(255, 235, 120));
                        } else {
                            g2.setColor(new Color(22, 13, 56));
                            g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
                            setForeground(Color.WHITE);
                        }
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                item.setOpaque(false);
                item.setFont(new Font("Segoe UI", Font.BOLD, 13));
                item.setIconTextGap(10);
                item.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 14));
                item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                item.addActionListener(ev -> {
                    lblTurno.setText("» " + SesionUsuario.getUsuario().toUpperCase() + ": " + op.texto);
                    lblTurno.setForeground(new Color(140, 235, 255));
                    SonidoUtil.paso();
                });
                menu.add(item);
            }
            menu.show(btnReaccion, -40, btnReaccion.getHeight() + 4);
        });

        BotonPill btnSonido = new BotonPill("");
        btnSonido.setIcon(crearIconoSonido(SonidoUtil.isActivo()));
        btnSonido.setColorNormal(new Color(42, 25, 96));
        btnSonido.setColorHover(new Color(68, 42, 145));
        btnSonido.setColorPresionado(new Color(28, 16, 68));
        btnSonido.setColorBorde(new Color(172, 149, 255, 180));
        btnSonido.setPreferredSize(new java.awt.Dimension(36, 30));
        btnSonido.setToolTipText(SonidoUtil.isActivo() ? "Sonido activado (clic para silenciar)" : "Sonido silenciado (clic para activar)");
        btnSonido.addActionListener(e -> {
            SonidoUtil.setActivo(!SonidoUtil.isActivo());
            btnSonido.setIcon(crearIconoSonido(SonidoUtil.isActivo()));
            btnSonido.setToolTipText(SonidoUtil.isActivo() ? "Sonido activado (clic para silenciar)" : "Sonido silenciado (clic para activar)");
        });

        topDer.removeAll();
        topDer.setPreferredSize(new java.awt.Dimension(146, 36));
        topDer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 2));
        topDer.add(btnReaccion);
        topDer.add(btnSonido);
        panelSuperior.setPreferredSize(new java.awt.Dimension(500, 132));

        panelesJugadores = new PanelJugador[]{
            jugadorAbajo,
            jugadorArribaIzq,
            jugadorArriba,
            jugadorAbajoDer
        };

        controlesDados = new ControlDado[]{
            controlDado,
            dadoArribaIzq,
            dadoRival,
            dadoAbajoDer
        };

        controlDado.setIzquierda(true);
        controlDado.setEsOponente(false);
        controlDado.configurar(this::alClicDado);

        dadoArribaIzq.setIzquierda(true);
        dadoArribaIzq.setEsOponente(true);
        dadoArribaIzq.setOcultar(true);

        dadoRival.setIzquierda(false);
        dadoRival.setEsOponente(true);
        dadoRival.setOcultar(true);

        dadoAbajoDer.setIzquierda(false);
        dadoAbajoDer.setEsOponente(true);
        dadoAbajoDer.setOcultar(true);

        panelTablero.configurar(idJugadorLocal, this::alClicFicha);
        panelTablero.setColorLocal(colorLocal);

        if (modelo != null) {
            modelo.agregarObservador(this);
        }
    }

    public static int calcularEsquina(String colorJugador, String colorLocal) {
        if (colorJugador == null || colorLocal == null) {
            return 0;
        }
        double angulo = 0.0;
        if ("VERDE".equalsIgnoreCase(colorLocal)) {
            angulo = Math.PI;
        } else if ("ROJO".equalsIgnoreCase(colorLocal)) {
            angulo = 3.0 * Math.PI / 2.0;
        } else if ("AMARILLO".equalsIgnoreCase(colorLocal)) {
            angulo = Math.PI / 2.0;
        }

        double dx, dy;
        switch (colorJugador.toUpperCase()) {
            case "ROJO":
                dx = -4.624; dy = -4.783;
                break;
            case "VERDE":
                dx = 4.581; dy = -4.783;
                break;
            case "AMARILLO":
                dx = 4.581; dy = 4.529;
                break;
            default:
                dx = -4.624; dy = 4.529;
                break;
        }

        double rx = 7.5 + dx * Math.cos(angulo) - dy * Math.sin(angulo);
        double ry = 7.5 + dx * Math.sin(angulo) + dy * Math.cos(angulo);

        if (rx < 7.5 && ry >= 7.5) {
            return 0;
        } else if (rx < 7.5 && ry < 7.5) {
            return 1;
        } else if (rx >= 7.5 && ry < 7.5) {
            return 2;
        } else {
            return 3;
        }
    }

    private void alSalir() {
        if (timerTurno != null) {
            timerTurno.stop();
        }
        int conf = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas abandonar la partida?\nEl otro jugador ganará por abandono.", "Salir de la partida", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            if (timerTurno != null) {
                timerTurno.stop();
            }
            if (timerPausa != null) {
                timerPausa.stop();
            }
            if (timerResultado != null) {
                timerResultado.stop();
            }
            resultadoMostrado = true;
            if (modelo != null) {
                modelo.abandonarPartida(idJugadorLocal);
            }
            EstadoPartidaDTO estadoFinal = modelo != null ? modelo.getEstadoActual() : null;
            VistaResultados res = new VistaResultados(false, SesionUsuario.getUsuario(), null, estadoFinal);
            res.setVisible(true);
            dispose();
        } else if (turnoActual != null) {
            reiniciarTimer(turnoActual);
        }
    }

    @Override
    public void dispose() {
        if (timerTurno != null) {
            timerTurno.stop();
        }
        if (timerPausa != null) {
            timerPausa.stop();
        }
        if (timerResultado != null) {
            timerResultado.stop();
        }
        super.dispose();
    }

    @Override
    public void actualizar(EstadoPartidaDTO estado) {
        for (JugadorDTO j : estado.getJugadores()) {
            if (idJugadorLocal.equals(j.getIdJugador()) && j.getColor() != null) {
                this.colorLocal = j.getColor();
                panelTablero.setColorLocal(this.colorLocal);
                break;
            }
        }

        panelTablero.dibujarFichas(estado);

        boolean[] esquinaOcupada = new boolean[4];
        esquinaOcupada[0] = true;

        for (JugadorDTO j : estado.getJugadores()) {
            int esq = calcularEsquina(j.getColor(), colorLocal);
            esquinaOcupada[esq] = true;
            panelesJugadores[esq].setIdJugador(j.getIdJugador());
            panelesJugadores[esq].setEsLocal(idJugadorLocal.equals(j.getIdJugador()));
            panelesJugadores[esq].actualizar(estado);
        }

        panelArribaIzq.setVisible(esquinaOcupada[1]);
        panelArribaDer.setVisible(esquinaOcupada[2]);
        panelAbajoDer.setVisible(esquinaOcupada[3]);

        if (estado.getIdGanador() != null) {
            if (timerTurno != null) {
                timerTurno.stop();
            }
            for (PanelJugador pj : panelesJugadores) {
                if (pj != null) {
                    pj.setFraccionTiempo(0.0);
                }
            }
        } else {
            String nuevoTurno = estado.getIdJugadorEnTurno();
            boolean cambioTurno = !nuevoTurno.equals(turnoActual);
            boolean esNuevaTirada = estado.getTiradaId() > 0 && estado.getTiradaId() != ultimaTiradaId;

            if (cambioTurno && idJugadorLocal.equals(nuevoTurno)) {
                SonidoUtil.turno();
            }
            if (cambioTurno || esNuevaTirada) {
                reiniciarTimer(nuevoTurno);
            }
        }

        boolean esNuevaTirada = estado.getTiradaId() > 0 && estado.getTiradaId() != ultimaTiradaId;
        if (timerPausa != null && timerPausa.isRunning()) {
            timerPausa.stop();
        }
        if (esNuevaTirada) {
            ultimaTiradaId = estado.getTiradaId();
            SonidoUtil.dado();
            String tirador = estado.getIdJugadorTirador() != null ? estado.getIdJugadorTirador() : estado.getIdJugadorEnTurno();
            JugadorDTO jTirador = buscarJugador(estado, tirador);
            int esqTirador = (jTirador != null) ? calcularEsquina(jTirador.getColor(), colorLocal) : 0;

            for (int i = 0; i < 4; i++) {
                if (i != esqTirador) {
                    controlesDados[i].setOcultar(true);
                }
            }

            ControlDado dadoActivo = controlesDados[esqTirador];
            dadoActivo.setOcultar(false);
            dadoActivo.setMostrarFlecha(false);

            if (esqTirador == 0) {
                lblTurno.setText("LANZANDO DADO...");
                lblTurno.setForeground(new Color(180, 255, 180));
            } else {
                String nom = (jTirador != null) ? jTirador.getNombreUsuario().toUpperCase() : "OPONENTE";
                lblTurno.setText("TURNO DE " + nom);
                lblTurno.setForeground(new Color(255, 220, 100));
            }

            dadoActivo.animarTirada(estado.getValorDado(), () -> {
                EstadoPartidaDTO actual = (modelo != null && modelo.getEstadoActual() != null) ? modelo.getEstadoActual() : estado;
                dadoActivo.mostrarValor(estado.getValorDado());
                if (esqTirador != 0 && jTirador != null) {
                    lblTurno.setText(jTirador.getNombreUsuario().toUpperCase() + " SACÓ " + estado.getValorDado());
                    lblTurno.setForeground(new Color(255, 220, 100));
                }
                finTirada(actual);
            });
        } else {
            String msg = estado.getMensaje();
            if (msg != null && (msg.contains("Capturó") || msg.contains("llegó a la meta"))) {
                SonidoUtil.premio();
            } else {
                SonidoUtil.paso();
            }
            for (ControlDado cd : controlesDados) {
                if (cd != null) {
                    cd.detenerAnimacion();
                }
            }
            actualizarDados(estado);
        }

        if (estado.getIdGanador() != null && !resultadoMostrado) {
            programarResultado(estado);
        }
    }

    private void programarResultado(EstadoPartidaDTO estado) {
        if (resultadoMostrado) {
            return;
        }
        resultadoMostrado = true;
        SonidoUtil.victoria();
        boolean gane = idJugadorLocal.equals(estado.getIdGanador());
        if (timerResultado != null && timerResultado.isRunning()) {
            timerResultado.stop();
        }
        timerResultado = new Timer(1200, e -> {
            VistaResultados res = new VistaResultados(gane, SesionUsuario.getUsuario(), estado.getIdGanador(), estado);
            res.setVisible(true);
            dispose();
        });
        timerResultado.setRepeats(false);
        timerResultado.start();
    }

    private void reiniciarTimer(String idJugador) {
        this.turnoActual = idJugador;
        this.inicioTurno = System.currentTimeMillis();

        if (timerTurno != null && timerTurno.isRunning()) {
            timerTurno.stop();
        }

        for (PanelJugador pj : panelesJugadores) {
            if (pj != null) {
                pj.setFraccionTiempo(pj.getIdJugador().equals(idJugador) ? 1.0 : 0.0);
            }
        }

        timerTurno = new Timer(35, e -> {
            if (modelo == null) {
                ((Timer) e.getSource()).stop();
                return;
            }

            long transcurrido = System.currentTimeMillis() - inicioTurno;
            long restante = Math.max(0, TIEMPO_TURNO_MS - transcurrido);
            double fraccion = (double) restante / (double) TIEMPO_TURNO_MS;

            for (PanelJugador pj : panelesJugadores) {
                if (pj != null && pj.getIdJugador().equals(turnoActual)) {
                    pj.setFraccionTiempo(fraccion);
                }
            }

            if (idJugadorLocal.equals(turnoActual) && fraccion <= 0.18 && fraccion > 0.0) {
                lblTurno.setText("● ¡TIEMPO AGOTÁNDOSE!");
                lblTurno.setForeground(new Color(255, 65, 65));
            }

            if (restante <= 0) {
                ((Timer) e.getSource()).stop();
                finTiempo();
            }
        });
        timerTurno.start();
    }

    private void finTiempo() {
        if (modelo == null) {
            return;
        }
        EstadoPartidaDTO estado = modelo.getEstadoActual();
        if (estado != null && estado.getIdGanador() == null) {
            String enTurno = estado.getIdJugadorEnTurno();
            if (enTurno != null && enTurno.equals(turnoActual) && idJugadorLocal.equals(enTurno)) {
                lblTurno.setText("● TIEMPO AGOTADO");
                lblTurno.setForeground(new Color(255, 65, 65));
                modelo.pasarTurno(enTurno);
            }
        }
    }

    private void finTirada(EstadoPartidaDTO estado) {
        if (timerPausa != null && timerPausa.isRunning()) {
            timerPausa.stop();
        }
        if (!estado.isEsperaFicha()) {
            timerPausa = new Timer(650, e -> {
                EstadoPartidaDTO actual = (modelo != null && modelo.getEstadoActual() != null) ? modelo.getEstadoActual() : estado;
                actualizarDados(actual);
                reiniciarTimer(actual.getIdJugadorEnTurno());
            });
            timerPausa.setRepeats(false);
            timerPausa.start();
        } else {
            actualizarDados(estado);
        }

        if (estado.getIdGanador() != null && !resultadoMostrado) {
            programarResultado(estado);
        }
    }

    private void actualizarDados(EstadoPartidaDTO estado) {
        boolean esMiTurno = idJugadorLocal.equals(estado.getIdJugadorEnTurno()) && estado.getIdGanador() == null;

        if (esMiTurno) {
            for (int i = 1; i < 4; i++) {
                controlesDados[i].setOcultar(true);
            }
            controlDado.setOcultar(false);

            if (estado.isEsperaFicha()) {
                lblTurno.setText("● ELIGE UNA FICHA PARA MOVER");
                lblTurno.setForeground(new Color(255, 220, 80));
                controlDado.mostrarValor(estado.getValorDado());
                controlDado.setMostrarFlecha(false);
                controlDado.habilitar(false);
            } else {
                lblTurno.setText("● TU TURNO - TIRA EL DADO");
                lblTurno.setForeground(new Color(120, 255, 120));
                controlDado.mostrarValor(0);
                controlDado.setMostrarFlecha(true);
                controlDado.habilitar(true);
            }
        } else {
            controlDado.setOcultar(true);
            JugadorDTO jTurno = buscarJugador(estado, estado.getIdJugadorEnTurno());
            int esqTurno = (jTurno != null) ? calcularEsquina(jTurno.getColor(), colorLocal) : 2;

            for (int i = 0; i < 4; i++) {
                if (i != esqTurno) {
                    controlesDados[i].setOcultar(true);
                }
            }

            ControlDado dadoTurno = controlesDados[esqTurno];
            dadoTurno.setOcultar(estado.getValorDado() == 0);
            dadoTurno.setMostrarFlecha(false);
            dadoTurno.mostrarValor(estado.getValorDado());
            dadoTurno.habilitar(false);

            String nom = (jTurno != null) ? jTurno.getNombreUsuario().toUpperCase() : "OPONENTE";
            if (estado.isEsperaFicha()) {
                lblTurno.setText("● " + nom + " ELIGIENDO FICHA...");
                lblTurno.setForeground(new Color(255, 220, 80));
            } else {
                lblTurno.setText("TURNO DE " + nom);
                lblTurno.setForeground(new Color(255, 220, 100));
            }
        }
    }

    private JugadorDTO buscarJugador(EstadoPartidaDTO estado, String idJugador) {
        if (estado == null || estado.getJugadores() == null || idJugador == null) {
            return null;
        }
        for (JugadorDTO j : estado.getJugadores()) {
            if (idJugador.equals(j.getIdJugador())) {
                return j;
            }
        }
        return null;
    }

    public void alClicDado() {
        if (controlador != null) {
            controlDado.habilitar(false);
            controlador.lanzarDado();
        }
    }

    public void alClicFicha(String idFicha) {
        if (controlador != null) {
            controlador.moverFicha(idFicha);
        }
    }

    public void mostrarMensaje(String texto) {
        setTitle(texto != null && !texto.isEmpty() ? texto : "Parchís");
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {

        panelFondo = new presentacion.PanelFondo();
        panelSuperior = new javax.swing.JPanel();
        panelTopBar = new javax.swing.JPanel();
        topIzq = new javax.swing.JPanel();
        btnSalir = new javax.swing.JButton();
        topCentro = new javax.swing.JPanel();
        lblTurno = new javax.swing.JLabel();
        topDer = new javax.swing.JPanel();
        panelArribaIzq = new javax.swing.JPanel();
        jugadorArribaIzq = new presentacion.PanelJugador();
        dadoArribaIzq = new presentacion.ControlDado();
        panelArribaDer = new javax.swing.JPanel();
        dadoRival = new presentacion.ControlDado();
        jugadorArriba = new presentacion.PanelJugador();
        panelCentro = new javax.swing.JPanel();
        panelTablero = new presentacion.PanelTablero();
        panelInferior = new javax.swing.JPanel();
        panelAbajoIzq = new javax.swing.JPanel();
        jugadorAbajo = new presentacion.PanelJugador();
        controlDado = new presentacion.ControlDado();
        panelAbajoDer = new javax.swing.JPanel();
        dadoAbajoDer = new presentacion.ControlDado();
        jugadorAbajoDer = new presentacion.PanelJugador();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís");
        setMinimumSize(new java.awt.Dimension(500, 720));

        panelFondo.setPreferredSize(new java.awt.Dimension(500, 720));
        panelFondo.setLayout(new java.awt.BorderLayout());

        panelSuperior.setOpaque(false);
        panelSuperior.setPreferredSize(new java.awt.Dimension(500, 122));
        panelSuperior.setLayout(new java.awt.BorderLayout());

        panelTopBar.setOpaque(false);
        panelTopBar.setPreferredSize(new java.awt.Dimension(500, 38));
        panelTopBar.setLayout(new java.awt.BorderLayout());

        topIzq.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 16, 0, 0));
        topIzq.setOpaque(false);
        topIzq.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnSalir.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnSalir.setForeground(new java.awt.Color(180, 165, 245));
        btnSalir.setText("← Salir");
        btnSalir.setBorderPainted(false);
        btnSalir.setContentAreaFilled(false);
        btnSalir.setFocusPainted(false);
        topIzq.add(btnSalir);

        panelTopBar.add(topIzq, java.awt.BorderLayout.WEST);

        topCentro.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        topCentro.setOpaque(false);
        topCentro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));

        lblTurno.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblTurno.setForeground(new java.awt.Color(255, 220, 80));
        lblTurno.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTurno.setText("PARTIDA EN CURSO");
        topCentro.add(lblTurno);

        panelTopBar.add(topCentro, java.awt.BorderLayout.CENTER);

        topDer.setOpaque(false);
        topDer.setPreferredSize(new java.awt.Dimension(148, 38));
        topDer.setLayout(new java.awt.FlowLayout());
        panelTopBar.add(topDer, java.awt.BorderLayout.EAST);

        panelSuperior.add(panelTopBar, java.awt.BorderLayout.NORTH);

        panelArribaIzq.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 0));
        panelArribaIzq.setOpaque(false);
        panelArribaIzq.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jugadorArribaIzq.setIdJugador("J3");
        jugadorArribaIzq.setPreferredSize(new java.awt.Dimension(112, 82));
        panelArribaIzq.add(jugadorArribaIzq);

        dadoArribaIzq.setPreferredSize(new java.awt.Dimension(84, 92));
        panelArribaIzq.add(dadoArribaIzq);

        panelSuperior.add(panelArribaIzq, java.awt.BorderLayout.WEST);

        panelArribaDer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 12));
        panelArribaDer.setOpaque(false);
        panelArribaDer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        dadoRival.setPreferredSize(new java.awt.Dimension(84, 92));
        panelArribaDer.add(dadoRival);

        jugadorArriba.setIdJugador("J2");
        jugadorArriba.setPreferredSize(new java.awt.Dimension(112, 82));
        panelArribaDer.add(jugadorArriba);

        panelSuperior.add(panelArribaDer, java.awt.BorderLayout.EAST);

        panelFondo.add(panelSuperior, java.awt.BorderLayout.NORTH);

        panelCentro.setOpaque(false);
        panelCentro.setLayout(new java.awt.BorderLayout());
        panelCentro.add(panelTablero, java.awt.BorderLayout.CENTER);

        panelFondo.add(panelCentro, java.awt.BorderLayout.CENTER);

        panelInferior.setOpaque(false);
        panelInferior.setPreferredSize(new java.awt.Dimension(500, 112));
        panelInferior.setLayout(new java.awt.BorderLayout());

        panelAbajoIzq.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 4, 0));
        panelAbajoIzq.setOpaque(false);
        panelAbajoIzq.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jugadorAbajo.setPreferredSize(new java.awt.Dimension(112, 82));
        panelAbajoIzq.add(jugadorAbajo);

        controlDado.setPreferredSize(new java.awt.Dimension(84, 92));
        panelAbajoIzq.add(controlDado);

        panelInferior.add(panelAbajoIzq, java.awt.BorderLayout.WEST);

        panelAbajoDer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 12));
        panelAbajoDer.setOpaque(false);
        panelAbajoDer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        dadoAbajoDer.setPreferredSize(new java.awt.Dimension(84, 92));
        panelAbajoDer.add(dadoAbajoDer);

        jugadorAbajoDer.setIdJugador("J4");
        jugadorAbajoDer.setPreferredSize(new java.awt.Dimension(112, 82));
        panelAbajoDer.add(jugadorAbajoDer);

        panelInferior.add(panelAbajoDer, java.awt.BorderLayout.EAST);

        panelFondo.add(panelInferior, java.awt.BorderLayout.SOUTH);

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaPartida().setVisible(true);
        });
    }

    private static class OpcionMensaje {
        final String texto;
        final Icon icono;
        OpcionMensaje(String texto, Icon icono) {
            this.texto = texto;
            this.icono = icono;
        }
    }

    private static Icon crearIconoChat() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int bx = x + 1;
                int by = y + 2;
                int w = 15;
                int h = 11;

                g2.setColor(new Color(255, 215, 65));
                g2.fillRoundRect(bx, by, w, h, 6, 6);

                Path2D cola = new Path2D.Double();
                cola.moveTo(bx + 3, by + h - 1);
                cola.lineTo(bx + 1, by + h + 3);
                cola.lineTo(bx + 6, by + h - 1);
                cola.closePath();
                g2.fill(cola);

                g2.setColor(new Color(38, 22, 92));
                g2.fillOval(bx + 3, by + 4, 2, 2);
                g2.fillOval(bx + 7, by + 4, 2, 2);
                g2.fillOval(bx + 11, by + 4, 2, 2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 18; }

            @Override
            public int getIconHeight() { return 18; }
        };
    }

    private static Icon crearIconoSonido(boolean activo) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = x + 2;
                int cy = y + 2;

                g2.setColor(activo ? Color.WHITE : new Color(205, 195, 230));
                Path2D bocina = new Path2D.Double();
                bocina.moveTo(cx + 1, cy + 5);
                bocina.lineTo(cx + 4, cy + 5);
                bocina.lineTo(cx + 8, cy + 2);
                bocina.lineTo(cx + 8, cy + 12);
                bocina.lineTo(cx + 4, cy + 9);
                bocina.lineTo(cx + 1, cy + 9);
                bocina.closePath();
                g2.fill(bocina);

                if (activo) {
                    g2.setColor(new Color(130, 235, 255));
                    g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawArc(cx + 7, cy + 4, 5, 6, -50, 100);
                    g2.drawArc(cx + 8, cy + 2, 8, 10, -50, 100);
                } else {
                    g2.setColor(new Color(255, 85, 95));
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx + 10, cy + 4, cx + 15, cy + 10);
                    g2.drawLine(cx + 15, cy + 4, cx + 10, cy + 10);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 18; }

            @Override
            public int getIconHeight() { return 18; }
        };
    }

    private static Icon crearIconoBadge(String tipo, Color colFondo, Color colIcono) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int s = 22;
                g2.setColor(colFondo);
                g2.fillOval(x + 1, y + 1, s, s);
                g2.setColor(new Color(colIcono.getRed(), colIcono.getGreen(), colIcono.getBlue(), 140));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(x + 1, y + 1, s, s);

                g2.setColor(colIcono);
                int cx = x + 1 + s / 2;
                int cy = y + 1 + s / 2;

                switch (tipo) {
                    case "ESTRELLA": {
                        Path2D star = new Path2D.Double();
                        double rExt = 6.2;
                        double rInt = 2.8;
                        for (int i = 0; i < 10; i++) {
                            double r = (i % 2 == 0) ? rExt : rInt;
                            double ang = -Math.PI / 2.0 + i * Math.PI / 5.0;
                            double px = cx + r * Math.cos(ang);
                            double py = cy + r * Math.sin(ang);
                            if (i == 0) star.moveTo(px, py);
                            else star.lineTo(px, py);
                        }
                        star.closePath();
                        g2.fill(star);
                        break;
                    }
                    case "RAYO": {
                        Path2D bolt = new Path2D.Double();
                        bolt.moveTo(cx + 1, cy - 6);
                        bolt.lineTo(cx - 3, cy);
                        bolt.lineTo(cx, cy);
                        bolt.lineTo(cx - 1, cy + 6);
                        bolt.lineTo(cx + 4, cy - 1);
                        bolt.lineTo(cx + 1, cy - 1);
                        bolt.closePath();
                        g2.fill(bolt);
                        break;
                    }
                    case "EXCLAMACION": {
                        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx, cy - 5, cx, cy + 1);
                        g2.fillOval(cx - 1, cy + 4, 3, 3);
                        break;
                    }
                    case "TREBOL": {
                        Path2D diamond = new Path2D.Double();
                        diamond.moveTo(cx, cy - 6);
                        diamond.lineTo(cx + 5, cy);
                        diamond.lineTo(cx, cy + 6);
                        diamond.lineTo(cx - 5, cy);
                        diamond.closePath();
                        g2.fill(diamond);
                        break;
                    }
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 24; }

            @Override
            public int getIconHeight() { return 24; }
        };
    }

    private javax.swing.JButton btnSalir;
    private presentacion.ControlDado controlDado;
    private presentacion.ControlDado dadoAbajoDer;
    private presentacion.ControlDado dadoRival;
    private presentacion.ControlDado dadoArribaIzq;
    private javax.swing.JLabel lblTurno;
    private javax.swing.JPanel panelAbajoDer;
    private javax.swing.JPanel panelAbajoIzq;
    private javax.swing.JPanel panelCentro;
    private presentacion.PanelFondo panelFondo;
    private javax.swing.JPanel panelInferior;
    private presentacion.PanelJugador jugadorAbajoDer;
    private presentacion.PanelJugador jugadorArribaIzq;
    private presentacion.PanelJugador jugadorAbajo;
    private presentacion.PanelJugador jugadorArriba;
    private javax.swing.JPanel panelSuperior;
    private presentacion.PanelTablero panelTablero;
    private javax.swing.JPanel panelTopBar;
    private javax.swing.JPanel topCentro;
    private javax.swing.JPanel topDer;
    private javax.swing.JPanel topIzq;
    private javax.swing.JPanel panelArribaDer;
    private javax.swing.JPanel panelArribaIzq;

}
