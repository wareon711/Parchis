package negocios;

import dto.EstadoPartidaDTO;
import dto.FichaDTO;
import dto.JugadorDTO;
import dto.MensajeDTO;
import dto.SalaDTO;
import dto.TipoMensaje;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import presentacion.ModeloPartida;
import presentacion.SesionUsuario;

public class MatchmakingLocal {

    public interface Listener {
        void onJugadorUnido(String id, String nombre, String color, boolean listo, int conectados, int requeridos);
        void onConteoRegresivo(int segundos);
        void onIniciarPartida(ModeloPartida modelo, String idLocal);
        void onError(String mensaje);
    }

    private final int numJugadores;
    private final String usuarioLocal;
    private final String colorLocal;
    private final String codigoSala;
    private final boolean modoHost;
    private final Listener listener;
    private final String clienteId = UUID.randomUUID().toString().substring(0, 8) + "|" + SesionUsuario.getAvatarElegido();

    private ParchisMessageBroker broker;
    private IComunicacion comunicacion;
    private IComunicacion comunicacionLocal;
    private boolean esHost = false;
    private boolean miListo = false;
    private volatile boolean cancelado = false;
    private volatile boolean unidoConfirmado = false;
    private volatile boolean partidaIniciada = false;
    private Timer timerReintento;
    private Timer timerConteo;

    private String miIdLocal = "J1";
    private String miColor;
    private final List<JugadorDTO> jugadoresSala = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> clientesAceptados = Collections.synchronizedSet(new HashSet<>());
    private ModeloPartida modeloHost;

    public MatchmakingLocal(int numJugadores, String usuarioLocal, String colorLocal, Listener listener) {
        this(numJugadores, usuarioLocal, colorLocal, RedUtil.obtenerCodigo(), true, listener);
    }

    public MatchmakingLocal(int numJugadores, String usuarioLocal, String colorLocal, String codigoOIp, Listener listener) {
        this(numJugadores, usuarioLocal, colorLocal, codigoOIp, false, listener);
    }

    public MatchmakingLocal(int numJugadores, String usuarioLocal, String colorLocal, String codigoSala, boolean modoHost, Listener listener) {
        this.numJugadores = (numJugadores == 4) ? 4 : 2;
        this.usuarioLocal = (usuarioLocal != null && !usuarioLocal.trim().isEmpty()) ? usuarioLocal.trim() : "Jugador";
        this.colorLocal = (colorLocal != null && !colorLocal.trim().isEmpty()) ? colorLocal.trim().toUpperCase() : "AZUL";
        this.codigoSala = (codigoSala != null && !codigoSala.trim().isEmpty()) ? codigoSala.trim().toUpperCase() : RedUtil.obtenerCodigo();
        this.modoHost = modoHost;
        this.listener = listener;
        this.miColor = this.colorLocal;
        this.esHost = modoHost;

        if (modoHost) {
            JugadorDTO j1 = new JugadorDTO("J1", this.usuarioLocal, this.miColor, clienteId, 0, false);
            jugadoresSala.add(j1);
            clientesAceptados.add(clienteId);
        }
    }

    public String getMiIdLocal() {
        return miIdLocal;
    }

    public boolean isEsHost() {
        return esHost;
    }

    public boolean isMiListo() {
        return miListo;
    }

    public boolean todosListos() {
        synchronized (jugadoresSala) {
            if (jugadoresSala.isEmpty()) return false;
            for (JugadorDTO j : jugadoresSala) {
                if (!j.isActivo()) {
                    return false;
                }
            }
            return true;
        }
    }

    public void alternarListo() {
        miListo = !miListo;
        if (esHost) {
            boolean autoIniciar = false;
            synchronized (jugadoresSala) {
                for (JugadorDTO j : jugadoresSala) {
                    if ("J1".equals(j.getIdJugador())) {
                        j.setActivo(miListo);
                    }
                }
                notificarListaLocal();
                autoIniciar = (jugadoresSala.size() >= numJugadores && todosListos());
            }
            difundirSala();
            if (autoIniciar) {
                SwingUtilities.invokeLater(this::arrancarHost);
            }
        } else if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(TipoMensaje.MARCAR_LISTO, clienteId, "HOST", miListo));
            } catch (IOException ignored) {
            }
        }
    }

    private void notificarListaLocal() {
        final List<JugadorDTO> copia = new ArrayList<>(jugadoresSala);
        SwingUtilities.invokeLater(() -> {
            for (JugadorDTO jug : copia) {
                listener.onJugadorUnido(jug.getIdJugador(), jug.getNombreUsuario(), jug.getColor(), jug.isActivo(), copia.size(), numJugadores);
            }
        });
    }

    public void iniciar() {
        new Thread(() -> {
            if (modoHost) {
                iniciarComoHost();
            } else {
                iniciarComoCliente(codigoSala);
            }
        }, "Matchmaking-Init").start();
    }

    private void iniciarComoHost() {
        esHost = true;
        miIdLocal = "J1";
        miColor = colorLocal;

        asegurarJugadorAnfitrion();
        notificarListaLocal();

        try {
            broker = new ParchisMessageBroker(RedUtil.PUERTO_BROKER);
            broker.iniciar();
            comunicacionLocal = new ComunicacionBroker();
            comunicacionLocal.conectar("127.0.0.1", RedUtil.PUERTO_BROKER);
            eventosHostEn(comunicacionLocal);
        } catch (IOException ignored) {
        }

        comunicacion = new ComunicacionBroker();
        try {
            comunicacion.conectar("SALA:" + codigoSala, RedUtil.PUERTO_BROKER);
            if (!cancelado && comunicacion.estaConectado()) {
                eventosHostEn(comunicacion);
            }
        } catch (IOException ignored) {
        }
    }

    private void asegurarJugadorAnfitrion() {
        synchronized (jugadoresSala) {
            boolean existeJ1 = false;
            for (JugadorDTO j : jugadoresSala) {
                if ("J1".equals(j.getIdJugador())) {
                    existeJ1 = true;
                    break;
                }
            }
            if (!existeJ1) {
                JugadorDTO j1 = new JugadorDTO("J1", usuarioLocal, miColor, clienteId, 0, miListo);
                jugadoresSala.add(0, j1);
                clientesAceptados.add(clienteId);
            }
        }
    }

    private void iniciarComoCliente(String codigoDestino) {
        esHost = false;
        unidoConfirmado = false;
        comunicacion = new ComunicacionBroker();

        String destino = codigoDestino.contains(".") ? codigoDestino : ("SALA:" + codigoDestino);
        try {
            comunicacion.conectar(destino, RedUtil.PUERTO_BROKER);
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> listener.onError("No se pudo conectar al código " + codigoDestino));
            return;
        }

        eventosCliente();
        enviarSolicitudRegistro();

        timerReintento = new Timer(1200, e -> {
            if (cancelado || unidoConfirmado) {
                ((Timer) e.getSource()).stop();
                return;
            }
            enviarSolicitudRegistro();
        });
        timerReintento.start();
    }

    private void enviarSolicitudRegistro() {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                JugadorDTO solicitud = new JugadorDTO("PENDIENTE", usuarioLocal, colorLocal, clienteId, 0, miListo);
                comunicacion.enviar(new MensajeDTO(TipoMensaje.SOLICITUD_REGISTRO, clienteId, "HOST", solicitud));
            } catch (Exception ignored) {
            }
        }
    }

    private void eventosHostEn(IComunicacion canal) {
        if (canal == null) return;

        canal.suscribir(TipoMensaje.SOLICITUD_REGISTRO, msg -> {
            if (cancelado) return;
            if (msg.getPayload() instanceof JugadorDTO nuevo) {
                String idRemoto = nuevo.getAvatar() != null ? nuevo.getAvatar() : msg.getRemitente();
                if (clienteId.equals(idRemoto)) {
                    return;
                }

                if (modeloHost != null) {
                    EstadoPartidaDTO estCurso = modeloHost.getEstadoActual();
                    if (estCurso != null && estCurso.getIdGanador() == null) {
                        for (JugadorDTO j : estCurso.getJugadores()) {
                            if (!"J1".equals(j.getIdJugador()) && (j.getNombreUsuario().equalsIgnoreCase(nuevo.getNombreUsuario()) || !j.isActivo())) {
                                j.setAvatar(idRemoto);
                                j.setActivo(true);
                                enviarATodos(new MensajeDTO(TipoMensaje.PARTIDA_INICIADA, "HOST", "TODOS", estCurso));
                                return;
                            }
                        }
                    }
                    return;
                }

                synchronized (jugadoresSala) {
                    asegurarJugadorAnfitrion();
                    if (clientesAceptados.contains(idRemoto)) {
                        difundirSala();
                        return;
                    }
                    if (jugadoresSala.size() >= numJugadores) {
                        return;
                    }

                    clientesAceptados.add(idRemoto);
                    int sig = jugadoresSala.size() + 1;
                    String id = "J" + sig;
                    String colorAsignado = nuevo.getColor();

                    for (JugadorDTO j : jugadoresSala) {
                        if (j.getColor().equalsIgnoreCase(colorAsignado)) {
                            colorAsignado = (numJugadores == 2)
                                    ? colorOpuesto(jugadoresSala.get(0).getColor())
                                    : colorLibre();
                            break;
                        }
                    }

                    String nombreFinal = nuevo.getNombreUsuario();
                    for (JugadorDTO j : jugadoresSala) {
                        if (j.getNombreUsuario().equalsIgnoreCase(nombreFinal)) {
                            nombreFinal = nombreFinal + " " + sig;
                            break;
                        }
                    }

                    JugadorDTO aceptado = new JugadorDTO(id, nombreFinal, colorAsignado, idRemoto, 0, false);
                    jugadoresSala.add(aceptado);

                    try {
                        FabricaSubsistemas.crearPerfilBO().obtenerOCrear(aceptado);
                    } catch (Exception ignored) {
                    }

                    notificarListaLocal();
                    difundirSala();
                }
            }
        });

        canal.suscribir(TipoMensaje.MARCAR_LISTO, msg -> {
            if (cancelado || modeloHost != null) return;
            String idRemoto = msg.getRemitente();
            boolean nuevoEstadoListo = (msg.getPayload() instanceof Boolean b) ? b : true;
            boolean autoIniciar = false;
            synchronized (jugadoresSala) {
                for (JugadorDTO j : jugadoresSala) {
                    if (idRemoto != null && idRemoto.equals(j.getAvatar())) {
                        j.setActivo(nuevoEstadoListo);
                        break;
                    }
                }
                notificarListaLocal();
                autoIniciar = (jugadoresSala.size() >= numJugadores && todosListos());
            }
            difundirSala();
            if (autoIniciar) {
                SwingUtilities.invokeLater(this::arrancarHost);
            }
        });

        canal.suscribir(TipoMensaje.INICIAR_PARTIDA, msg -> {
            if (!cancelado && modeloHost == null) {
                SwingUtilities.invokeLater(this::arrancarHost);
            }
        });

        canal.suscribir(TipoMensaje.TIRADA_DADO, msg -> {
            if (modeloHost != null && msg.getPayload() instanceof String idJugador) {
                SwingUtilities.invokeLater(() -> modeloHost.lanzarDado(idJugador));
            }
        });

        canal.suscribir(TipoMensaje.MOVIMIENTO_FICHA, msg -> {
            if (modeloHost != null && msg.getPayload() instanceof String[] args && args.length >= 2) {
                SwingUtilities.invokeLater(() -> modeloHost.moverFicha(args[0], args[1]));
            }
        });

        canal.suscribir(TipoMensaje.PASAR_TURNO, msg -> {
            if (modeloHost != null && msg.getPayload() instanceof String idJugador) {
                SwingUtilities.invokeLater(() -> modeloHost.pasarTurno(idJugador));
            }
        });

        canal.suscribir(TipoMensaje.DESCONEXION, msg -> {
            if (modeloHost != null && msg.getPayload() instanceof String idJugador) {
                SwingUtilities.invokeLater(() -> modeloHost.abandonarPartida(idJugador));
            }
        });
    }

    private void enviarATodos(MensajeDTO msg) {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(msg);
            } catch (IOException ignored) {
            }
        }
        if (comunicacionLocal != null && comunicacionLocal.estaConectado()) {
            try {
                comunicacionLocal.enviar(msg);
            } catch (IOException ignored) {
            }
        }
    }

    private void eventosCliente() {
        comunicacion.suscribir(TipoMensaje.SALA_ACTUALIZADA, msg -> {
            if (msg.getPayload() instanceof SalaDTO sala) {
                List<JugadorDTO> lista = sala.getJugadores();
                boolean estoy = false;
                for (JugadorDTO j : lista) {
                    if (clienteId.equals(j.getAvatar())) {
                        miIdLocal = j.getIdJugador();
                        miColor = j.getColor();
                        miListo = j.isActivo();
                        estoy = true;
                        break;
                    }
                }
                if (estoy) {
                    unidoConfirmado = true;
                    if (timerReintento != null) {
                        timerReintento.stop();
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    for (JugadorDTO j : lista) {
                        listener.onJugadorUnido(j.getIdJugador(), j.getNombreUsuario(), j.getColor(), j.isActivo(), lista.size(), sala.getRequeridos());
                    }
                });
            }
        });

        comunicacion.suscribir(TipoMensaje.CONTEO_REGRESIVO, msg -> {
            if (msg.getPayload() instanceof Integer seg) {
                SwingUtilities.invokeLater(() -> listener.onConteoRegresivo(seg));
            }
        });

        comunicacion.suscribir(TipoMensaje.PARTIDA_INICIADA, msg -> {
            if (partidaIniciada) return;
            if (msg.getPayload() instanceof EstadoPartidaDTO estadoInicial) {
                partidaIniciada = true;
                if (timerReintento != null) {
                    timerReintento.stop();
                }
                for (JugadorDTO j : estadoInicial.getJugadores()) {
                    if (clienteId.equals(j.getAvatar()) || j.getNombreUsuario().equalsIgnoreCase(usuarioLocal)) {
                        miIdLocal = j.getIdJugador();
                        miColor = j.getColor();
                        break;
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    FachadaReglasRemota fachadaRemota = new FachadaReglasRemota(comunicacion, estadoInicial);
                    ModeloPartida modeloCliente = new ModeloPartida(fachadaRemota);

                    comunicacion.suscribir(TipoMensaje.ESTADO_ACTUALIZADO, m -> {
                        if (m.getPayload() instanceof EstadoPartidaDTO nuevoEstado) {
                            fachadaRemota.setEstadoActual(nuevoEstado);
                            SwingUtilities.invokeLater(() -> modeloCliente.actualizarEstado(nuevoEstado));
                        }
                    });

                    listener.onIniciarPartida(modeloCliente, miIdLocal);
                });
            }
        });
    }

    private void difundirSala() {
        List<JugadorDTO> copia;
        synchronized (jugadoresSala) {
            copia = new ArrayList<>(jugadoresSala);
        }
        Set<String> libres = new HashSet<>(Arrays.asList("AZUL", "ROJO", "VERDE", "AMARILLO"));
        for (JugadorDTO j : copia) {
            libres.remove(j.getColor().toUpperCase());
        }
        SalaDTO sala = new SalaDTO(copia, libres, numJugadores);
        enviarATodos(new MensajeDTO(TipoMensaje.SALA_ACTUALIZADA, "HOST", "TODOS", sala));
    }

    public void iniciarAhora() {
        if (cancelado) return;
        if (timerReintento != null) timerReintento.stop();
        if (timerConteo != null) timerConteo.stop();

        if (esHost) {
            arrancarHost();
        } else if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(TipoMensaje.INICIAR_PARTIDA, usuarioLocal, "HOST", "INICIAR"));
            } catch (IOException ignored) {
            }
        }
    }

    private synchronized void arrancarHost() {
        if (cancelado || modeloHost != null) return;

        EstadoPartidaDTO estado = new EstadoPartidaDTO();
        synchronized (jugadoresSala) {
            asegurarJugadorAnfitrion();

            for (JugadorDTO j : jugadoresSala) {
                j.setActivo(true);
            }

            // Si hay menos jugadores reales que la capacidad de la sala (e.g. 2 o 3 en sala de 4),
            // completar los cupos restantes automáticamente con Bots inteligentes
            while (jugadoresSala.size() < numJugadores) {
                int sig = jugadoresSala.size() + 1;
                String idBot = "J" + sig;
                String nomBot = nombreBot();
                String colBot = colorLibre();
                jugadoresSala.add(new JugadorDTO(idBot, nomBot, colBot, "BOT", 0, true));
            }

            List<JugadorDTO> ordenados = new ArrayList<>(jugadoresSala);
            ordenados.sort((a, b) -> Integer.compare(ordenHorario(a.getColor()), ordenHorario(b.getColor())));

            for (JugadorDTO j : ordenados) {
                estado.getJugadores().add(j);
                for (int f = 1; f <= 4; f++) {
                    estado.getFichas().add(new FichaDTO(j.getIdJugador() + "-F" + f, j.getIdJugador(), j.getColor(), -1, true, false, false));
                }
            }
        }

        Random rnd = new Random();
        JugadorDTO inicial = estado.getJugadores().get(rnd.nextInt(estado.getJugadores().size()));
        estado.setIdJugadorEnTurno(inicial.getIdJugador());
        estado.setValorDado(0);
        estado.setEsperaFicha(false);
        estado.setMensaje("Comienza " + inicial.getNombreUsuario() + ".");

        IFachadaReglas fachada = new FachadaReglasMock(estado, rnd);
        modeloHost = new ModeloPartida(fachada);

        modeloHost.setListenerCambio(nuevoEstado -> {
            enviarATodos(new MensajeDTO(TipoMensaje.ESTADO_ACTUALIZADO, "HOST", "TODOS", nuevoEstado));
        });

        for (JugadorDTO j : jugadoresSala) {
            boolean esBot = "BOT".equalsIgnoreCase(j.getAvatar()) || (j.getNombreUsuario() != null && j.getNombreUsuario().toLowerCase().contains("bot"));
            if (esBot && !"J1".equals(j.getIdJugador())) {
                new BotJugador(j.getIdJugador(), j.getNombreUsuario(), modeloHost);
            }
        }

        enviarATodos(new MensajeDTO(TipoMensaje.PARTIDA_INICIADA, "HOST", "TODOS", estado));
        Timer reenvio = new Timer(400, e -> {
            enviarATodos(new MensajeDTO(TipoMensaje.PARTIDA_INICIADA, "HOST", "TODOS", estado));
        });
        reenvio.setRepeats(false);
        reenvio.start();

        SwingUtilities.invokeLater(() -> listener.onIniciarPartida(modeloHost, "J1"));
    }

    private static int ordenHorario(String color) {
        if ("ROJO".equalsIgnoreCase(color)) return 0;
        if ("VERDE".equalsIgnoreCase(color)) return 1;
        if ("AMARILLO".equalsIgnoreCase(color)) return 2;
        return 3;
    }

    private String nombreBot() {
        String[] pool = {"SofiaBot", "MateoBot", "LucasBot", "ElenaBot", "CarlosBot", "ValeriaBot"};
        List<String> ocupados = new ArrayList<>();
        synchronized (jugadoresSala) {
            for (JugadorDTO j : jugadoresSala) {
                ocupados.add(j.getNombreUsuario().toLowerCase());
            }
        }
        for (String n : pool) {
            if (!ocupados.contains(n.toLowerCase())) {
                return n;
            }
        }
        return "Bot" + (jugadoresSala.size() + 1);
    }

    public String colorLibre() {
        if (numJugadores == 2 && !jugadoresSala.isEmpty()) {
            return colorOpuesto(jugadoresSala.get(0).getColor());
        }
        String[] todos = {"AZUL", "ROJO", "VERDE", "AMARILLO"};
        for (String c : todos) {
            boolean libre = true;
            synchronized (jugadoresSala) {
                for (JugadorDTO j : jugadoresSala) {
                    if (c.equalsIgnoreCase(j.getColor())) {
                        libre = false;
                        break;
                    }
                }
            }
            if (libre) return c;
        }
        return "ROJO";
    }

    public static String colorOpuesto(String color) {
        if ("VERDE".equalsIgnoreCase(color)) return "AZUL";
        if ("AZUL".equalsIgnoreCase(color)) return "VERDE";
        if ("ROJO".equalsIgnoreCase(color)) return "AMARILLO";
        return "ROJO";
    }

    public void cancelar() {
        cancelado = true;
        if (timerReintento != null) timerReintento.stop();
        if (timerConteo != null) timerConteo.stop();
        if (comunicacion != null) comunicacion.desconectar();
        if (comunicacionLocal != null) comunicacionLocal.desconectar();
        if (broker != null) broker.detener();
    }
}
