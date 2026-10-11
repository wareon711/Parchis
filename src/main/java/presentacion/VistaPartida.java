package presentacion;

import dto.EstadoPartidaDTO;

public class VistaPartida extends javax.swing.JFrame implements IObservadorPartida {

    private String idJugadorLocal;
    private ControladorPartida controlador;
    private ModeloPartida modelo;
    private int ultimaTiradaId = 0;

    public VistaPartida() {
        this("J1", "Parchís", null, null);
    }

    public VistaPartida(String idJugadorLocal, String titulo, ControladorPartida controlador, ModeloPartida modelo) {
        this.idJugadorLocal = idJugadorLocal;
        this.controlador = controlador;
        this.modelo = modelo;
        initComponents();
        inicializar(titulo);
    }

    private void inicializar(String titulo) {
        setTitle(titulo);

        String idOponente = "J1".equals(idJugadorLocal) ? "J2" : "J1";
        panelJugadorVistaSuperior.setIdJugador(idOponente);
        panelJugadorVistaSuperior.setEsLocal(false);

        panelJugadorVistaInferior.setIdJugador(idJugadorLocal != null ? idJugadorLocal : "J1");
        panelJugadorVistaInferior.setEsLocal(true);

        controlDadoOponente.setOrientacionIzquierda(false);
        controlDadoOponente.setOcultar(true);
        controlDadoOponente.setEsOponente(true);

        controlDado.setOrientacionIzquierda(true);
        controlDado.setEsOponente(false);
        controlDado.configurar(this::alClicDado);

        panelTablero.configurar(idJugadorLocal, this::alClicFicha);

        if (modelo != null) {
            modelo.agregarObservador(this);
        }
    }

    @Override
    public void actualizar(EstadoPartidaDTO estado) {
        panelTablero.dibujarFichas(estado);
        panelJugadorVistaSuperior.actualizar(estado);
        panelJugadorVistaInferior.actualizar(estado);

        boolean esNuevaTirada = estado.getTiradaId() > 0 && estado.getTiradaId() != ultimaTiradaId;
        if (esNuevaTirada) {
            ultimaTiradaId = estado.getTiradaId();
            String tirador = estado.getIdJugadorTirador() != null ? estado.getIdJugadorTirador() : estado.getIdJugadorEnTurno();
            boolean tiroJugadorLocal = idJugadorLocal.equals(tirador);
            if (tiroJugadorLocal) {
                controlDado.setOcultar(false);
                controlDado.setMostrarFlecha(false);
                controlDadoOponente.setOcultar(true);
                controlDado.animarTirada(estado.getValorDado(), () -> {
                    controlDado.mostrarValor(estado.getValorDado());
                    finalizarAnimacionTirada(estado);
                });
            } else {
                controlDado.setOcultar(true);
                controlDadoOponente.setOcultar(false);
                controlDadoOponente.setMostrarFlecha(false);
                controlDadoOponente.animarTirada(estado.getValorDado(), () -> {
                    controlDadoOponente.mostrarValor(estado.getValorDado());
                    finalizarAnimacionTirada(estado);
                });
            }
        } else {
            actualizarEstadoDados(estado);
        }
    }

    private void finalizarAnimacionTirada(EstadoPartidaDTO estado) {
        if (!estado.isEsperaFicha()) {
            javax.swing.Timer pausa = new javax.swing.Timer(1200, e -> actualizarEstadoDados(estado));
            pausa.setRepeats(false);
            pausa.start();
        } else {
            actualizarEstadoDados(estado);
        }
    }

    private void actualizarEstadoDados(EstadoPartidaDTO estado) {
        boolean esMiTurno = idJugadorLocal.equals(estado.getIdJugadorEnTurno()) && estado.getIdGanador() == null;

        if (esMiTurno) {
            controlDado.setOcultar(false);
            controlDadoOponente.setOcultar(true);

            if (estado.isEsperaFicha()) {
                controlDado.mostrarValor(estado.getValorDado());
                controlDado.setMostrarFlecha(false);
                controlDado.habilitar(false);
            } else {
                controlDado.mostrarValor(0);
                controlDado.setMostrarFlecha(true);
                controlDado.habilitar(true);
            }
        } else {
            controlDado.setOcultar(true);
            controlDadoOponente.setOcultar(estado.getValorDado() == 0);
            controlDadoOponente.setMostrarFlecha(false);

            controlDadoOponente.mostrarValor(estado.getValorDado());
            controlDadoOponente.habilitar(false);
        }
    }

    public void alClicDado() {
        if (controlador != null) {
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelFondo = new javax.swing.JPanel();
        panelSuperior = new javax.swing.JPanel();
        panelTopDerecha = new javax.swing.JPanel();
        controlDadoOponente = new presentacion.ControlDado();
        panelJugadorVistaSuperior = new presentacion.PanelJugador();
        panelCentro = new javax.swing.JPanel();
        panelTablero = new presentacion.PanelTablero();
        panelInferior = new javax.swing.JPanel();
        panelBottomIzquierda = new javax.swing.JPanel();
        panelJugadorVistaInferior = new presentacion.PanelJugador();
        controlDado = new presentacion.ControlDado();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parchís");
        setMinimumSize(new java.awt.Dimension(500, 720));

        panelFondo.setBackground(new java.awt.Color(5, 20, 51));
        panelFondo.setPreferredSize(new java.awt.Dimension(500, 720));
        panelFondo.setLayout(new java.awt.BorderLayout());

        panelSuperior.setOpaque(false);
        panelSuperior.setPreferredSize(new java.awt.Dimension(500, 100));
        panelSuperior.setLayout(new java.awt.BorderLayout());

        panelTopDerecha.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 16));
        panelTopDerecha.setOpaque(false);

        controlDadoOponente.setPreferredSize(new java.awt.Dimension(84, 92));
        panelTopDerecha.add(controlDadoOponente);

        panelJugadorVistaSuperior.setIdJugador("J2");
        panelJugadorVistaSuperior.setPreferredSize(new java.awt.Dimension(68, 76));
        panelTopDerecha.add(panelJugadorVistaSuperior);

        panelSuperior.add(panelTopDerecha, java.awt.BorderLayout.EAST);

        panelFondo.add(panelSuperior, java.awt.BorderLayout.NORTH);

        panelCentro.setOpaque(false);
        panelCentro.setLayout(new java.awt.BorderLayout());
        panelCentro.add(panelTablero, java.awt.BorderLayout.CENTER);

        panelFondo.add(panelCentro, java.awt.BorderLayout.CENTER);

        panelInferior.setOpaque(false);
        panelInferior.setPreferredSize(new java.awt.Dimension(500, 105));
        panelInferior.setLayout(new java.awt.BorderLayout());

        panelBottomIzquierda.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 6, 0));
        panelBottomIzquierda.setOpaque(false);

        panelJugadorVistaInferior.setPreferredSize(new java.awt.Dimension(68, 76));
        panelBottomIzquierda.add(panelJugadorVistaInferior);

        controlDado.setPreferredSize(new java.awt.Dimension(84, 92));
        panelBottomIzquierda.add(controlDado);

        panelInferior.add(panelBottomIzquierda, java.awt.BorderLayout.WEST);

        panelFondo.add(panelInferior, java.awt.BorderLayout.SOUTH);

        getContentPane().add(panelFondo, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new VistaPartida().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private presentacion.ControlDado controlDado;
    private presentacion.ControlDado controlDadoOponente;
    private javax.swing.JPanel panelBottomIzquierda;
    private javax.swing.JPanel panelCentro;
    private javax.swing.JPanel panelFondo;
    private javax.swing.JPanel panelInferior;
    private presentacion.PanelJugador panelJugadorVistaInferior;
    private presentacion.PanelJugador panelJugadorVistaSuperior;
    private javax.swing.JPanel panelSuperior;
    private presentacion.PanelTablero panelTablero;
    private javax.swing.JPanel panelTopDerecha;
    // End of variables declaration//GEN-END:variables
}
