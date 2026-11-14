package negocios;

import dto.JugadorDTO;
import java.util.List;

public class ValidacionSalaLugar implements ValidacionRegistro {

    @Override
    public void validar(JugadorDTO nuevo, List<JugadorDTO> actuales, int max) throws SalaException {
        if (actuales.size() >= max) {
            throw new SalaException("La sala está llena (máximo " + max + " jugadores).");
        }
    }
}
