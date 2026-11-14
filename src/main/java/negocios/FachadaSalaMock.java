package negocios;

import dto.EstadoPartidaDTO;
import dto.FichaDTO;
import dto.JugadorDTO;
import dto.SalaDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import presentacion.ModeloPartida;

public class FachadaSalaMock implements IFachadaSala {

    private final int capacidadMax;
    private final List<JugadorDTO> jugadores = new ArrayList<>();
    private final List<ValidacionRegistro> validaciones = new ArrayList<>();
    private final List<ObservadorSala> observadores = new CopyOnWriteArrayList<>();
    private Timer timerBots;
    private Timer timerConteo;
    private int segundos = 3;
    private boolean partidaIniciada = false;

    public FachadaSalaMock() {
        this(2);
    }

    public FachadaSalaMock(int capacidadMax) {
        this.capacidadMax = (capacidadMax == 4) ? 4 : 2;
        this.validaciones.add(new ValidacionNombre());
        this.validaciones.add(new ValidacionSalaLugar());
        this.validaciones.add(new ValidacionColorLibre());
    }

    @Override
    public synchronized SalaDTO registrarJugador(JugadorDTO jugador) throws SalaException {
        for (ValidacionRegistro v : validaciones) {
            v.validar(jugador, jugadores, capacidadMax);
        }
        jugadores.add(jugador);
        SalaDTO sala = consultarSala();
        avisarSala(sala);

        iniciarBots();
        return sala;
    }

    private void iniciarBots() {
        if (timerBots != null && timerBots.isRunning()) return;
        timerBots = new Timer(1200, ev -> {
            synchronized (this) {
                if (partidaIniciada || jugadores.size() >= capacidadMax) {
                    ((Timer) ev.getSource()).stop();
                    return;
                }
                agregarBot();
                if (jugadores.size() >= capacidadMax) {
                    ((Timer) ev.getSource()).stop();
                    iniciarConteo();
                }
            }
        });
        timerBots.start();
    }

    private void agregarBot() {
        String[] poolNombres = {"SofiaBot", "MateoBot", "LucasBot", "ElenaBot", "CarlosBot", "ValeriaBot"};
        String nombre = "Bot" + (jugadores.size() + 1);
        for (String p : poolNombres) {
            boolean ocupado = false;
            for (JugadorDTO j : jugadores) {
                if (j.getNombreUsuario().equalsIgnoreCase(p)) {
                    ocupado = true;
                    break;
                }
            }
            if (!ocupado) {
                nombre = p;
                break;
            }
        }

        String color = obtenerColorLibre();
        String idBot = "J" + (jugadores.size() + 1);
        jugadores.add(new JugadorDTO(idBot, nombre, color, "avatar", 0, true));
        avisarSala(consultarSala());
    }

    private String obtenerColorLibre() {
        List<String> todos = Arrays.asList("AZUL", "ROJO", "VERDE", "AMARILLO");
        for (String c : todos) {
            boolean libre = true;
            for (JugadorDTO j : jugadores) {
                if (c.equalsIgnoreCase(j.getColor())) {
                    libre = false;
                    break;
                }
            }
            if (libre) return c;
        }
        return "ROJO";
    }

    private void iniciarConteo() {
        if (timerConteo != null && timerConteo.isRunning()) return;
        segundos = 3;
        avisarConteo(segundos);

        timerConteo = new Timer(1000, e -> {
            segundos--;
            if (segundos > 0) {
                avisarConteo(segundos);
            } else {
                ((Timer) e.getSource()).stop();
                iniciarPartida();
            }
        });
        timerConteo.start();
    }

    private void iniciarPartida() {
        partidaIniciada = true;
        EstadoPartidaDTO estado = new EstadoPartidaDTO();
        for (JugadorDTO j : jugadores) {
            estado.getJugadores().add(j);
            for (int f = 1; f <= 4; f++) {
                estado.getFichas().add(new FichaDTO(j.getIdJugador() + "-F" + f, j.getIdJugador(), j.getColor(), -1, true, false, false));
            }
        }
        estado.setIdJugadorEnTurno(jugadores.get(0).getIdJugador());
        estado.setValorDado(0);
        estado.setEsperaFicha(false);
        estado.setMensaje("Comienza " + jugadores.get(0).getNombreUsuario() + ".");

        IFachadaReglas fachada = new FachadaReglasMock(estado, new Random());
        ModeloPartida modelo = new ModeloPartida(fachada);

        for (JugadorDTO j : jugadores) {
            if (!"J1".equals(j.getIdJugador()) && j.getNombreUsuario().toLowerCase().contains("bot")) {
                new BotJugador(j.getIdJugador(), j.getNombreUsuario(), modelo);
            }
        }

        String idLocal = jugadores.get(0).getIdJugador();
        avisarInicio(modelo, idLocal);
    }

    @Override
    public synchronized void marcarListo(String nombreJugador) throws SalaException {
        boolean encontrado = false;
        for (int i = 0; i < jugadores.size(); i++) {
            JugadorDTO j = jugadores.get(i);
            if (j.getNombreUsuario().equalsIgnoreCase(nombreJugador)) {
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            throw new SalaException("El jugador no está en la sala.");
        }
        avisarSala(consultarSala());
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
    public synchronized void desconectar() {
        if (timerBots != null) timerBots.stop();
        if (timerConteo != null) timerConteo.stop();
        jugadores.clear();
        partidaIniciada = false;
    }

    private void avisarSala(SalaDTO sala) {
        for (ObservadorSala obs : observadores) {
            SwingUtilities.invokeLater(() -> obs.salaActualizada(sala));
        }
    }

    private void avisarConteo(int seg) {
        for (ObservadorSala obs : observadores) {
            SwingUtilities.invokeLater(() -> obs.conteoRegresivo(seg));
        }
    }

    private void avisarInicio(ModeloPartida modelo, String idLocal) {
        for (ObservadorSala obs : observadores) {
            SwingUtilities.invokeLater(() -> obs.partidaIniciada(modelo, idLocal));
        }
    }
}

