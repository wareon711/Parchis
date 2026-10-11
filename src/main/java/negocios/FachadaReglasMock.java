package negocios;

import dto.EstadoPartidaDTO;
import dto.FichaDTO;
import dto.JugadorDTO;
import java.util.Random;

public class FachadaReglasMock implements IFachadaReglas {

    private static final String[] COLORES = {"AZUL", "VERDE", "AMARILLO", "ROJO"};
    private static final int META = 56;
    private static final int FICHAS_POR_JUGADOR = 4;

    private final Random generador;
    private final EstadoPartidaDTO estado;
    private int valorPendiente;
    private int seisConsecutivos;
    private int tiradasTotal = 0;

    public FachadaReglasMock() {
        this(2);
    }

    public FachadaReglasMock(int numeroJugadores) {
        this(numeroJugadores, new Random());
    }

    public FachadaReglasMock(EstadoPartidaDTO estadoInicial) {
        this(estadoInicial, new Random());
    }

    public FachadaReglasMock(EstadoPartidaDTO estadoInicial, Random generador) {
        this.estado = estadoInicial;
        this.generador = generador;
    }

    public FachadaReglasMock(int numeroJugadores, Random generador) {
        if (numeroJugadores < 2 || numeroJugadores > 4) {
            throw new IllegalArgumentException("La partida admite de 2 a 4 jugadores");
        }
        this.generador = generador;
        this.estado = new EstadoPartidaDTO();

        for (int i = 0; i < numeroJugadores; i++) {
            String id = "J" + (i + 1);
            estado.getJugadores().add(new JugadorDTO(id, "Jugador " + (i + 1), COLORES[i], "avatar", 0, true));
            for (int f = 1; f <= FICHAS_POR_JUGADOR; f++) {
                estado.getFichas().add(new FichaDTO(id + "-F" + f, id, COLORES[i], -1, true, false, false));
            }
        }

        JugadorDTO inicial = estado.getJugadores().get(generador.nextInt(numeroJugadores));
        estado.setIdJugadorEnTurno(inicial.getIdJugador());
        estado.setValorDado(0);
        estado.setMensaje("Comienza " + inicial.getNombreUsuario() + ".");
    }

    @Override
    public EstadoPartidaDTO lanzarDado(String idJugador) {
        if (estado.getIdGanador() != null) {
            return rechazar("La partida ya terminó.");
        }
        if (!idJugador.equals(estado.getIdJugadorEnTurno())) {
            return rechazar("No es tu turno.");
        }
        if (valorPendiente > 0) {
            return rechazar("Primero elige una ficha para mover.");
        }

        tiradasTotal++;
        estado.setTiradaId(tiradasTotal);
        estado.setIdJugadorTirador(idJugador);

        int valor = generarValor();
        estado.setValorDado(valor);
        seisConsecutivos = (valor == 6) ? seisConsecutivos + 1 : 0;
        marcarFichasMovibles(idJugador, valor);

        boolean hayMovibles = false;
        for (FichaDTO f : estado.getFichas()) {
            if (f.isMovible()) {
                hayMovibles = true;
                break;
            }
        }

        String nombre = buscarJugador(idJugador).getNombreUsuario();
        if (hayMovibles) {
            valorPendiente = valor;
            estado.setEsperaFicha(true);
            estado.setMensaje(valor == 6
                    ? nombre + ": ¡Sacó seis! Elige una ficha."
                    : nombre + ": Elige una ficha para mover.");
        } else {
            estado.setEsperaFicha(false);
            estado.setMensaje(nombre + ": Sin movimientos posibles.");
            pasarTurno();
        }
        return copiarEstado();
    }

    @Override
    public EstadoPartidaDTO moverFicha(String idJugador, String idFicha) {
        if (estado.getIdGanador() != null) {
            return rechazar("La partida ya terminó.");
        }
        if (!idJugador.equals(estado.getIdJugadorEnTurno())) {
            return rechazar("No es tu turno.");
        }
        if (valorPendiente == 0) {
            return rechazar("Primero lanza el dado.");
        }

        FichaDTO ficha = buscarFicha(idFicha);
        if (ficha == null || !ficha.getIdJugador().equals(idJugador) || !ficha.isMovible()) {
            return rechazar("Esa ficha no se puede mover.");
        }

        int valor = valorPendiente;
        aplicarMovimiento(idFicha);
        valorPendiente = 0;
        estado.setEsperaFicha(false);

        JugadorDTO jugador = buscarJugador(idJugador);
        if (jugador.getFichasEnMeta() == FICHAS_POR_JUGADOR) {
            estado.setIdGanador(idJugador);
            estado.setMensaje("¡" + jugador.getNombreUsuario() + " ganó la partida!");
        } else if (valor != 6 || seisConsecutivos >= 3) {
            estado.setValorDado(0);
            pasarTurno();
        } else {
            estado.setValorDado(0);
            estado.setMensaje(estado.getMensaje() + " Vuelve a tirar.");
        }
        return copiarEstado();
    }

    @Override
    public EstadoPartidaDTO obtenerEstadoPartida() {
        return copiarEstado();
    }

    protected int generarValor() {
        return generador.nextInt(6) + 1;
    }

    protected EstadoPartidaDTO getEstadoInterno() {
        return estado;
    }

    private void marcarFichasMovibles(String idJugador, int valor) {
        for (FichaDTO f : estado.getFichas()) {
            boolean movible = false;
            if (f.getIdJugador().equals(idJugador) && !f.isEnMeta()) {
                if (f.isEnBase()) {
                    if (valor == 5) {
                        int casillaSalida = casillaInicio(f.getColor());
                        movible = (contarFichasEnCasilla(casillaSalida) < 2);
                    }
                } else if (f.getPosicion() + valor <= META) {
                    movible = !hayBarreraEnCamino(f, valor);
                }
            }
            f.setMovible(movible);
        }
    }

    private int contarFichasEnCasilla(int casilla) {
        int cuenta = 0;
        for (FichaDTO f : estado.getFichas()) {
            if (!f.isEnBase() && !f.isEnMeta() && f.getPosicion() <= 50) {
                if (casillaAbsoluta(f) == casilla) {
                    cuenta++;
                }
            }
        }
        return cuenta;
    }

    private boolean hayBarreraEnCamino(FichaDTO ficha, int pasos) {
        int posActual = ficha.getPosicion();
        for (int p = 1; p <= pasos; p++) {
            int posInter = posActual + p;
            if (posInter <= 50) {
                int casilla = (casillaInicio(ficha.getColor()) + posInter) % 52;
                if (contarFichasEnCasilla(casilla) >= 2) {
                    return true;
                }
            } else if (posInter < META) {
                for (FichaDTO otra : estado.getFichas()) {
                    if (otra.getIdJugador().equals(ficha.getIdJugador())
                            && !otra.isEnBase() && !otra.isEnMeta()
                            && !otra.getIdFicha().equals(ficha.getIdFicha())
                            && otra.getPosicion() == posInter) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void aplicarMovimiento(String idFicha) {
        FichaDTO ficha = buscarFicha(idFicha);
        JugadorDTO jugador = buscarJugador(ficha.getIdJugador());
        String mensaje;

        if (ficha.isEnBase()) {
            ficha.setEnBase(false);
            ficha.setPosicion(0);
            mensaje = jugador.getNombreUsuario() + " sacó una ficha.";
        } else {
            ficha.setPosicion(ficha.getPosicion() + valorPendiente);
            mensaje = jugador.getNombreUsuario() + " movió " + valorPendiente + " casillas.";
        }

        if (ficha.getPosicion() == META) {
            ficha.setEnMeta(true);
            jugador.setFichasEnMeta(jugador.getFichasEnMeta() + 1);
            mensaje += " ¡Una ficha llegó a la meta!";
        } else if (ficha.getPosicion() <= 50) {
            int casilla = casillaAbsoluta(ficha);
            if (!esCasillaSegura(casilla)) {
                for (FichaDTO rival : estado.getFichas()) {
                    if (!rival.getIdJugador().equals(ficha.getIdJugador()) && !rival.isEnBase()
                            && !rival.isEnMeta() && rival.getPosicion() <= 50
                            && casillaAbsoluta(rival) == casilla) {
                        rival.setEnBase(true);
                        rival.setPosicion(-1);
                        mensaje += " ¡Capturó una ficha de "
                                + buscarJugador(rival.getIdJugador()).getNombreUsuario() + "!";
                    }
                }
            }
        }

        for (FichaDTO f : estado.getFichas()) {
            f.setMovible(false);
        }
        estado.setMensaje(mensaje);
    }

    private void pasarTurno() {
        for (FichaDTO f : estado.getFichas()) {
            f.setMovible(false);
        }
        valorPendiente = 0;
        seisConsecutivos = 0;
        estado.setEsperaFicha(false);
        int n = estado.getJugadores().size();
        int actual = 0;
        for (int i = 0; i < n; i++) {
            if (estado.getJugadores().get(i).getIdJugador().equals(estado.getIdJugadorEnTurno())) {
                actual = i;
            }
        }
        JugadorDTO siguiente = estado.getJugadores().get((actual + 1) % n);
        estado.setIdJugadorEnTurno(siguiente.getIdJugador());
        estado.setMensaje(estado.getMensaje() + " Turno de " + siguiente.getNombreUsuario() + ".");
    }

    private int casillaInicio(String color) {
        if ("VERDE".equals(color)) {
            return 13;
        }
        if ("AMARILLO".equals(color)) {
            return 26;
        }
        if ("AZUL".equals(color)) {
            return 39;
        }
        return 0;
    }

    private boolean esCasillaSegura(int casilla) {
        int[] seguras = {0, 8, 13, 21, 26, 34, 39, 47};
        for (int s : seguras) {
            if (s == casilla) {
                return true;
            }
        }
        return false;
    }

    private int casillaAbsoluta(FichaDTO ficha) {
        return (casillaInicio(ficha.getColor()) + ficha.getPosicion()) % 52;
    }

    private EstadoPartidaDTO rechazar(String motivo) {
        EstadoPartidaDTO copia = copiarEstado();
        copia.setMensaje(motivo);
        return copia;
    }

    private JugadorDTO buscarJugador(String idJugador) {
        for (JugadorDTO j : estado.getJugadores()) {
            if (j.getIdJugador().equals(idJugador)) {
                return j;
            }
        }
        return null;
    }

    private FichaDTO buscarFicha(String idFicha) {
        for (FichaDTO f : estado.getFichas()) {
            if (f.getIdFicha().equals(idFicha)) {
                return f;
            }
        }
        return null;
    }

    private EstadoPartidaDTO copiarEstado() {
        EstadoPartidaDTO copia = new EstadoPartidaDTO();
        for (JugadorDTO j : estado.getJugadores()) {
            copia.getJugadores().add(new JugadorDTO(j.getIdJugador(), j.getNombreUsuario(),
                    j.getColor(), j.getAvatar(), j.getFichasEnMeta(), j.isActivo()));
        }
        for (FichaDTO f : estado.getFichas()) {
            copia.getFichas().add(new FichaDTO(f.getIdFicha(), f.getIdJugador(), f.getColor(),
                    f.getPosicion(), f.isEnBase(), f.isEnMeta(), f.isMovible()));
        }
        copia.setIdJugadorEnTurno(estado.getIdJugadorEnTurno());
        copia.setIdJugadorTirador(estado.getIdJugadorTirador());
        copia.setValorDado(estado.getValorDado());
        copia.setTiradaId(estado.getTiradaId());
        copia.setEsperaFicha(estado.isEsperaFicha());
        copia.setMensaje(estado.getMensaje());
        copia.setIdGanador(estado.getIdGanador());
        return copia;
    }
}
