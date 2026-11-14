package negocios;

import dto.EstadoPartidaDTO;
import dto.JugadorDTO;
import dto.MensajeDTO;
import dto.SalaDTO;
import dto.TipoMensaje;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import presentacion.ModeloPartida;

public class FachadaSala implements IFachadaSala {

    private final IComunicacion comunicacion;
    private final PerfilBO perfilBO;
    private final int capacidadMax;
    private final List<JugadorDTO> jugadores = new ArrayList<>();
    private final List<ObservadorSala> observadores = new CopyOnWriteArrayList<>();
    private final List<ValidacionRegistro> validaciones = new ArrayList<>();

    public FachadaSala(IComunicacion comunicacion, PerfilBO perfilBO, int capacidadMax) {
        this.comunicacion = comunicacion;
        this.perfilBO = perfilBO;
        this.capacidadMax = (capacidadMax == 4) ? 4 : 2;

        this.validaciones.add(new ValidacionNombre());
        this.validaciones.add(new ValidacionSalaLugar());
        this.validaciones.add(new ValidacionColorLibre());

        suscribirEventos();
    }

    private void suscribirEventos() {
        if (comunicacion == null) {
            return;
        }

        comunicacion.suscribir(TipoMensaje.SALA_ACTUALIZADA, msg -> {
            if (msg.getPayload() instanceof SalaDTO sala) {
                synchronized (jugadores) {
                    jugadores.clear();
                    jugadores.addAll(sala.getJugadores());
                }
                avisarSala(sala);
            }
        });

        comunicacion.suscribir(TipoMensaje.ERROR_SALA, msg -> {
            String error = msg.getPayload() != null ? msg.getPayload().toString() : "Error en sala";
            avisarError(error);
        });

        comunicacion.suscribir(TipoMensaje.PARTIDA_INICIADA, msg -> {
            if (msg.getPayload() instanceof EstadoPartidaDTO estado) {
                IFachadaReglas fachada = new FachadaReglasMock(estado, new Random());
                ModeloPartida modelo = new ModeloPartida(fachada);
                avisarInicio(modelo, "J1");
            }
        });
    }

    @Override
    public synchronized SalaDTO registrarJugador(JugadorDTO jugador) throws SalaException {
        for (ValidacionRegistro v : validaciones) {
            v.validar(jugador, jugadores, capacidadMax);
        }

        guardarPerfil(jugador);

        jugadores.add(jugador);
        SalaDTO sala = consultarSala();

        enviarAlBroker(jugador, sala);
        return sala;
    }

    private void guardarPerfil(JugadorDTO jugador) {
        if (perfilBO != null) {
            try {
                perfilBO.obtenerOCrear(jugador);
            } catch (Exception ignored) {
            }
        }
    }

    private void enviarAlBroker(JugadorDTO jugador, SalaDTO sala) throws SalaException {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(
                        TipoMensaje.SOLICITUD_REGISTRO,
                        jugador.getNombreUsuario(),
                        "BROKER",
                        jugador
                ));
            } catch (IOException e) {
                throw new SalaException("Error al enviar al Broker: " + e.getMessage(), e);
            }
        } else {
            avisarSala(sala);
        }
    }

    @Override
    public synchronized void marcarListo(String nombreJugador) throws SalaException {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(
                        TipoMensaje.MARCAR_LISTO,
                        nombreJugador,
                        "BROKER",
                        nombreJugador
                ));
            } catch (IOException e) {
                throw new SalaException("Error al marcar listo: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public synchronized SalaDTO consultarSala() {
        Set<String> disponibles = new HashSet<>(Arrays.asList("AZUL", "ROJO", "VERDE", "AMARILLO"));
        for (JugadorDTO j : jugadores) {
            if (j.getColor() != null) {
                disponibles.remove(j.getColor().toUpperCase());
            }
        }
        return new SalaDTO(jugadores, disponibles, capacidadMax);
    }

    @Override
    public void agregarObservador(ObservadorSala observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void quitarObservador(ObservadorSala observador) {
        observadores.remove(observador);
    }

    @Override
    public void desconectar() {
        if (comunicacion != null) {
            comunicacion.desconectar();
        }
        jugadores.clear();
    }

    private void avisarSala(SalaDTO sala) {
        for (ObservadorSala obs : observadores) {
            SwingUtilities.invokeLater(() -> obs.salaActualizada(sala));
        }
    }

    private void avisarError(String err) {
        for (ObservadorSala obs : observadores) {
            SwingUtilities.invokeLater(() -> obs.errorRecibido(err));
        }
    }

    private void avisarInicio(ModeloPartida modelo, String idLocal) {
        for (ObservadorSala obs : observadores) {
            SwingUtilities.invokeLater(() -> obs.partidaIniciada(modelo, idLocal));
        }
    }
}
