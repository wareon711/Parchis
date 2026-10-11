package negocios;

import dto.EstadoPartidaDTO;

public interface IFachadaReglas {

    EstadoPartidaDTO lanzarDado(String idJugador);

    EstadoPartidaDTO moverFicha(String idJugador, String idFicha);

    EstadoPartidaDTO obtenerEstadoPartida();
}
