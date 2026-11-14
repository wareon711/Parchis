package negocios;

import dto.JugadorDTO;
import java.util.List;

public interface ValidacionRegistro {

    void validar(JugadorDTO jugador, List<JugadorDTO> jugadoresActuales, int capacidadMax) throws SalaException;
}

