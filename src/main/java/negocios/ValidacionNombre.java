package negocios;

import dto.JugadorDTO;
import java.util.List;

public class ValidacionNombre implements ValidacionRegistro {

    @Override
    public void validar(JugadorDTO nuevo, List<JugadorDTO> actuales, int max) throws SalaException {
        if (nuevo == null || nuevo.getNombreUsuario() == null) {
            throw new SalaException("El nombre del jugador es obligatorio.");
        }

        String nombre = nuevo.getNombreUsuario().trim();
        if (nombre.isEmpty()) {
            throw new SalaException("El nombre no puede estar vacío.");
        }
        if (nombre.length() < 2 || nombre.length() > 15) {
            throw new SalaException("El nombre debe tener entre 2 y 15 caracteres.");
        }

        for (JugadorDTO j : actuales) {
            if (j.getNombreUsuario() != null && j.getNombreUsuario().trim().equalsIgnoreCase(nombre)) {
                throw new SalaException("El nombre '" + nombre + "' ya está en uso en la sala.");
            }
        }
    }
}
