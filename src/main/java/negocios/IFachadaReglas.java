package negocios;

import dto.EstadoPartidaDTO;

public interface IFachadaReglas {

    EstadoPartidaDTO lanzarDado(String idJugador);

    EstadoPartidaDTO moverFicha(String idJugador, String idFicha);

    EstadoPartidaDTO pasarTurno(String idJugador);

    default EstadoPartidaDTO abandonarPartida(String idJugador) {
        return obtenerEstadoPartida();
    }

    EstadoPartidaDTO obtenerEstadoPartida();
}
