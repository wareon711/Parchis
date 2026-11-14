package negocios;

import dto.EstadoPartidaDTO;
import dto.FichaDTO;
import dto.JugadorDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FachadaReglasMock implements IFachadaReglas {

    private static final String[] COLORES = {"AZUL", "VERDE", "AMARILLO", "ROJO"};
    private static final int META = 56;
    private static final int FICHAS_POR_JUGADOR = 4;
    private static final int MAX_TURNOS_INACTIVOS = 6;

    private final Random generador;
    private final EstadoPartidaDTO estado;
    private final Map<String, Integer> turnosInactivos = new HashMap<>();
    private int pendiente;
    private int seisSeguidos;
    private boolean ultimoTiroFueSeis;
    private String ultimaFichaTurno;
    private int tiradasTotal = 0;

    public FachadaReglasMock() {
        this(2);
    }

    public FachadaReglasMock(int numJugadores) {
        this(numJugadores, new Random());
    }

    public FachadaReglasMock(EstadoPartidaDTO estadoInicial) {
        this(estadoInicial, new Random());
    }

    public FachadaReglasMock(EstadoPartidaDTO estadoInicial, Random generador) {
        this.estado = estadoInicial;
        this.generador = generador;
        if (this.estado != null && this.estado.getIdJugadorEnTurno() == null && !this.estado.getJugadores().isEmpty()) {
            JugadorDTO inicial = this.estado.getJugadores().get(0);
            this.estado.setIdJugadorEnTurno(inicial.getIdJugador());
            this.estado.setValorDado(0);
            this.estado.setEsperaFicha(false);
            this.estado.setMensaje("Comienza " + inicial.getNombreUsuario() + ".");
        }
    }

    public FachadaReglasMock(int numJugadores, Random generador) {
        if (numJugadores < 2 || numJugadores > 4) {
            throw new IllegalArgumentException("La partida admite de 2 a 4 jugadores");
        }
        this.generador = generador;
        this.estado = new EstadoPartidaDTO();

        for (int i = 0; i < numJugadores; i++) {
            String id = "J" + (i + 1);
            estado.getJugadores().add(new JugadorDTO(id, "Jugador " + (i + 1), COLORES[i], "avatar", 0, true));
            for (int f = 1; f <= FICHAS_POR_JUGADOR; f++) {
                estado.getFichas().add(new FichaDTO(id + "-F" + f, id, COLORES[i], -1, true, false, false));
            }
        }

        JugadorDTO inicial = estado.getJugadores().get(generador.nextInt(numJugadores));
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
        if (pendiente > 0) {
            return rechazar("Primero elige una ficha para mover.");
        }

        turnosInactivos.put(idJugador, 0);
        JugadorDTO jugActual = buscarJugador(idJugador);
        if (jugActual != null) {
            jugActual.setTurnosInactivos(0);
        }
        tiradasTotal++;
        estado.setTiradaId(tiradasTotal);
        estado.setIdJugadorTirador(idJugador);

        int valor = generarValor();
        estado.setValorDado(valor);
        ultimoTiroFueSeis = (valor == 6);
        seisSeguidos = (valor == 6) ? seisSeguidos + 1 : 0;

        String nombre = jugActual != null ? jugActual.getNombreUsuario() : idJugador;
        if (seisSeguidos >= 3) {
            if (ultimaFichaTurno != null) {
                FichaDTO castigada = buscarFicha(ultimaFichaTurno);
                if (castigada != null && !castigada.isEnBase() && !castigada.isEnMeta() && castigada.getPosicion() <= 50) {
                    castigada.setEnBase(true);
                    castigada.setPosicion(-1);
                }
            }
            estado.setEsperaFicha(false);
            estado.setMensaje(nombre + ": ¡Tres 6 seguidos! Pierde turno.");
            pasarTurno();
            return copiarEstado();
        }

        boolean obligaRomperBloqueo = marcarMovibles(idJugador, valor);

        boolean hayMovibles = false;
        for (FichaDTO f : estado.getFichas()) {
            if (f.isMovible()) {
                hayMovibles = true;
                break;
            }
        }

        if (hayMovibles) {
            pendiente = valor;
            estado.setEsperaFicha(true);
            if (obligaRomperBloqueo) {
                estado.setMensaje(nombre + ": ¡Sacó 6! Debe romper su bloqueo.");
            } else {
                estado.setMensaje(valor == 6
                        ? nombre + ": ¡Sacó seis! Elige una ficha."
                        : nombre + ": Elige una ficha para mover.");
            }
        } else {
            estado.setEsperaFicha(false);
            estado.setMensaje(nombre + ": Sacó " + valor + ", sin movimientos.");
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
        if (pendiente == 0) {
            return rechazar("Primero lanza el dado.");
        }

        FichaDTO ficha = buscarFicha(idFicha);
        if (ficha == null || !ficha.getIdJugador().equals(idJugador) || !ficha.isMovible()) {
            return rechazar("Esa ficha no se puede mover.");
        }

        turnosInactivos.put(idJugador, 0);
        JugadorDTO jugador = buscarJugador(idJugador);
        if (jugador != null) {
            jugador.setTurnosInactivos(0);
        }

        ultimaFichaTurno = idFicha;
        int premio = mover(idFicha);
        pendiente = 0;
        estado.setEsperaFicha(false);

        if (jugador.getFichasEnMeta() == FICHAS_POR_JUGADOR) {
            estado.setIdGanador(idJugador);
            estado.setMensaje("¡" + jugador.getNombreUsuario() + " ganó la partida!");
            return copiarEstado();
        }

        if (premio > 0) {
            marcarMovibles(idJugador, premio);
            boolean puedeUsarPremio = false;
            for (FichaDTO f : estado.getFichas()) {
                if (f.isMovible()) {
                    puedeUsarPremio = true;
                    break;
                }
            }
            if (puedeUsarPremio) {
                pendiente = premio;
                estado.setEsperaFicha(true);
                estado.setMensaje(estado.getMensaje() + " ¡Premio +" + premio + "! Elige ficha.");
                return copiarEstado();
            }
        }

        if (!ultimoTiroFueSeis || seisSeguidos >= 3) {
            estado.setValorDado(0);
            pasarTurno();
        } else {
            estado.setValorDado(0);
            estado.setMensaje(estado.getMensaje() + " Vuelve a tirar.");
        }
        return copiarEstado();
    }

    @Override
    public EstadoPartidaDTO pasarTurno(String idJugador) {
        if (estado.getIdGanador() != null) {
            return rechazar("La partida ya terminó.");
        }
        if (!idJugador.equals(estado.getIdJugadorEnTurno())) {
            return rechazar("No es tu turno.");
        }
        JugadorDTO jugador = buscarJugador(idJugador);
        String nombre = jugador != null ? jugador.getNombreUsuario() : idJugador;

        int acumulados = turnosInactivos.getOrDefault(idJugador, 0) + 1;
        turnosInactivos.put(idJugador, acumulados);
        if (jugador != null) {
            jugador.setTurnosInactivos(acumulados);
        }

        if (acumulados >= MAX_TURNOS_INACTIVOS) {
            return abandonarPartida(idJugador);
        }

        estado.setMensaje("Tiempo agotado para " + nombre + " (AFK " + acumulados + "/6).");
        pasarTurno();
        return copiarEstado();
    }

    @Override
    public EstadoPartidaDTO abandonarPartida(String idJugador) {
        if (estado.getIdGanador() != null) {
            return copiarEstado();
        }
        JugadorDTO jSaliente = buscarJugador(idJugador);
        if (jSaliente != null) {
            jSaliente.setActivo(false);
        }
        estado.getFichas().removeIf(f -> idJugador != null && idJugador.equals(f.getIdJugador()));

        JugadorDTO unicoActivo = null;
        int activos = 0;
        for (JugadorDTO j : estado.getJugadores()) {
            if (j.isActivo()) {
                activos++;
                unicoActivo = j;
            }
        }

        String nomSaliente = jSaliente != null ? jSaliente.getNombreUsuario() : "Rival";
        if (activos == 1 && unicoActivo != null) {
            estado.setIdGanador(unicoActivo.getIdJugador());
            estado.setEsperaFicha(false);
            estado.setMensaje("¡" + nomSaliente + " abandonó! Ganó " + unicoActivo.getNombreUsuario() + ".");
            return copiarEstado();
        }

        estado.setMensaje(nomSaliente + " abandonó la partida.");
        if (idJugador != null && idJugador.equals(estado.getIdJugadorEnTurno())) {
            pasarTurno();
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

    private boolean marcarMovibles(String idJugador, int valor) {
        for (FichaDTO f : estado.getFichas()) {
            boolean movible = false;
            if (f.getIdJugador().equals(idJugador) && !f.isEnMeta()) {
                if (f.isEnBase()) {
                    if (valor == 5) {
                        int casillaSalida = casillaInicio(f.getColor());
                        movible = (contarFichas(casillaSalida) < 2);
                    }
                } else if (f.getPosicion() + valor <= META) {
                    movible = !hayBarrera(f, valor);
                }
            }
            f.setMovible(movible);
        }

        if (valor == 6) {
            boolean hayBloqueoPropioMovible = false;
            for (FichaDTO f : estado.getFichas()) {
                if (f.getIdJugador().equals(idJugador) && f.isMovible() && !f.isEnBase() && !f.isEnMeta()) {
                    if (contarPropiasEnPosicion(idJugador, f.getPosicion()) >= 2) {
                        hayBloqueoPropioMovible = true;
                        break;
                    }
                }
            }
            if (hayBloqueoPropioMovible) {
                for (FichaDTO f : estado.getFichas()) {
                    if (f.getIdJugador().equals(idJugador) && f.isMovible()) {
                        if (f.isEnBase() || contarPropiasEnPosicion(idJugador, f.getPosicion()) < 2) {
                            f.setMovible(false);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private int contarPropiasEnPosicion(String idJugador, int posicion) {
        int cuenta = 0;
        for (FichaDTO f : estado.getFichas()) {
            if (f.getIdJugador().equals(idJugador) && !f.isEnBase() && !f.isEnMeta() && f.getPosicion() == posicion) {
                cuenta++;
            }
        }
        return cuenta;
    }

    private int contarFichas(int casilla) {
        int cuenta = 0;
        for (FichaDTO f : estado.getFichas()) {
            if (!f.isEnBase() && !f.isEnMeta() && f.getPosicion() <= 50) {
                if (posReal(f) == casilla) {
                    cuenta++;
                }
            }
        }
        return cuenta;
    }

    private boolean hayBarrera(FichaDTO ficha, int pasos) {
        int posActual = ficha.getPosicion();
        for (int p = 1; p <= pasos; p++) {
            int posInter = posActual + p;
            if (posInter <= 50) {
                int casilla = (casillaInicio(ficha.getColor()) + posInter) % 52;
                if (contarFichas(casilla) >= 2) {
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

    private int mover(String idFicha) {
        FichaDTO ficha = buscarFicha(idFicha);
        JugadorDTO jugador = buscarJugador(ficha.getIdJugador());
        String mensaje;
        int premio = 0;

        if (ficha.isEnBase()) {
            ficha.setEnBase(false);
            ficha.setPosicion(0);
            mensaje = jugador.getNombreUsuario() + " sacó una ficha.";
        } else {
            ficha.setPosicion(ficha.getPosicion() + pendiente);
            mensaje = jugador.getNombreUsuario() + " movió " + pendiente + " casillas.";
        }

        if (ficha.getPosicion() == META) {
            ficha.setEnMeta(true);
            jugador.setFichasEnMeta(jugador.getFichasEnMeta() + 1);
            mensaje += " ¡Una ficha llegó a la meta!";
            premio = 10;
        } else if (ficha.getPosicion() <= 50) {
            int casilla = posReal(ficha);
            if (!esSegura(casilla)) {
                for (FichaDTO rival : estado.getFichas()) {
                    if (!rival.getIdJugador().equals(ficha.getIdJugador()) && !rival.isEnBase()
                            && !rival.isEnMeta() && rival.getPosicion() <= 50
                            && posReal(rival) == casilla) {
                        rival.setEnBase(true);
                        rival.setPosicion(-1);
                        mensaje += " ¡Capturó una ficha de "
                                + buscarJugador(rival.getIdJugador()).getNombreUsuario() + "!";
                        premio = 20;
                    }
                }
            }
        }

        for (FichaDTO f : estado.getFichas()) {
            f.setMovible(false);
        }
        estado.setMensaje(mensaje);
        return premio;
    }

    private void pasarTurno() {
        for (FichaDTO f : estado.getFichas()) {
            f.setMovible(false);
        }
        pendiente = 0;
        seisSeguidos = 0;
        ultimoTiroFueSeis = false;
        ultimaFichaTurno = null;
        estado.setEsperaFicha(false);
        int n = estado.getJugadores().size();
        int actual = 0;
        for (int i = 0; i < n; i++) {
            if (estado.getJugadores().get(i).getIdJugador().equals(estado.getIdJugadorEnTurno())) {
                actual = i;
            }
        }
        for (int paso = 1; paso <= n; paso++) {
            JugadorDTO candidato = estado.getJugadores().get((actual + paso) % n);
            if (candidato.isActivo()) {
                estado.setIdJugadorEnTurno(candidato.getIdJugador());
                estado.setMensaje(estado.getMensaje() + " Turno de " + candidato.getNombreUsuario() + ".");
                return;
            }
        }
    }

    private int casillaInicio(String color) {
        if ("VERDE".equalsIgnoreCase(color)) {
            return 13;
        }
        if ("AMARILLO".equalsIgnoreCase(color)) {
            return 26;
        }
        if ("AZUL".equalsIgnoreCase(color)) {
            return 39;
        }
        return 0;
    }

    private boolean esSegura(int casilla) {
        int[] seguras = {0, 8, 13, 21, 26, 34, 39, 47};
        for (int s : seguras) {
            if (s == casilla) {
                return true;
            }
        }
        return false;
    }

    private int posReal(FichaDTO ficha) {
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
            JugadorDTO cj = new JugadorDTO(j.getIdJugador(), j.getNombreUsuario(),
                    j.getColor(), j.getAvatar(), j.getFichasEnMeta(), j.isActivo());
            cj.setTurnosInactivos(j.getTurnosInactivos());
            copia.getJugadores().add(cj);
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
