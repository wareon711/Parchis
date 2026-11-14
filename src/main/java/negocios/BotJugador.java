package negocios;

import dto.EstadoPartidaDTO;
import dto.FichaDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;
import presentacion.IObservadorPartida;
import presentacion.ModeloPartida;

public class BotJugador implements IObservadorPartida {

    private final String idBot;
    private final String nombreBot;
    private final ModeloPartida modelo;
    private final Random random = new Random();
    private boolean accionEnCurso = false;

    public BotJugador(String idBot, ModeloPartida modelo) {
        this(idBot, "Bot Simulador", modelo);
    }

    public BotJugador(String idBot, String nombreBot, ModeloPartida modelo) {
        this.idBot = idBot != null ? idBot : "J2";
        this.nombreBot = nombreBot != null ? nombreBot : "Bot Simulador";
        this.modelo = modelo;
        if (modelo != null) {
            modelo.agregarObservador(this);
        }
    }

    public String getIdBot() {
        return idBot;
    }

    public String getNombreBot() {
        return nombreBot;
    }

    @Override
    public void actualizar(EstadoPartidaDTO estado) {
        if (estado == null || estado.getIdGanador() != null || modelo == null) {
            return;
        }

        boolean esMiTurno = idBot.equals(estado.getIdJugadorEnTurno());
        if (!esMiTurno || accionEnCurso) {
            return;
        }

        if (!estado.isEsperaFicha()) {
            accionEnCurso = true;
            Timer timerTiro = new Timer(600 + random.nextInt(150), e -> {
                accionEnCurso = false;
                modelo.lanzarDado(idBot);
            });
            timerTiro.setRepeats(false);
            timerTiro.start();
        } else {
            accionEnCurso = true;
            Timer timerMover = new Timer(550 + random.nextInt(150), e -> {
                accionEnCurso = false;
                List<FichaDTO> movibles = new ArrayList<>();
                for (FichaDTO f : estado.getFichas()) {
                    if (idBot.equals(f.getIdJugador()) && f.isMovible()) {
                        movibles.add(f);
                    }
                }

                if (!movibles.isEmpty()) {
                    FichaDTO elegida = elegirFicha(movibles, estado);
                    modelo.moverFicha(idBot, elegida.getIdFicha());
                } else {
                    modelo.lanzarDado(idBot);
                }
            });
            timerMover.setRepeats(false);
            timerMover.start();
        }
    }

    private FichaDTO elegirFicha(List<FichaDTO> movibles, EstadoPartidaDTO estado) {
        int valor = estado.getValorDado();

        for (FichaDTO f : movibles) {
            if (!f.isEnBase()) {
                int posDestino = f.getPosicion() + valor;
                if (posDestino <= 50) {
                    int casillaDest = posReal(f.getColor(), posDestino);
                    if (!esSegura(casillaDest)) {
                        for (FichaDTO rival : estado.getFichas()) {
                            if (!rival.getIdJugador().equals(idBot) && !rival.isEnBase() && !rival.isEnMeta() && rival.getPosicion() <= 50) {
                                if (posReal(rival.getColor(), rival.getPosicion()) == casillaDest) {
                                    return f;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (FichaDTO f : movibles) {
            if (f.isEnBase()) {
                return f;
            }
        }

        for (FichaDTO f : movibles) {
            if (f.getPosicion() + valor == 56) {
                return f;
            }
        }

        FichaDTO mejor = movibles.get(0);
        for (FichaDTO f : movibles) {
            if (f.getPosicion() > mejor.getPosicion()) {
                mejor = f;
            }
        }
        return mejor;
    }

    private static int casillaInicio(String color) {
        if ("VERDE".equalsIgnoreCase(color)) return 13;
        if ("AMARILLO".equalsIgnoreCase(color)) return 26;
        if ("AZUL".equalsIgnoreCase(color)) return 39;
        return 0;
    }

    private static int posReal(String color, int posicion) {
        return (casillaInicio(color) + posicion) % 52;
    }

    private static boolean esSegura(int casilla) {
        int[] seguras = {0, 8, 13, 21, 26, 34, 39, 47};
        for (int s : seguras) {
            if (s == casilla) return true;
        }
        return false;
    }
}
