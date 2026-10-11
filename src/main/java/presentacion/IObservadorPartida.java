package presentacion;

import dto.EstadoPartidaDTO;

public interface IObservadorPartida {

    void actualizar(EstadoPartidaDTO estado);
}
