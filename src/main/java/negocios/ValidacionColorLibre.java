package negocios;

import dto.JugadorDTO;
import java.util.Arrays;
import java.util.List;

public class ValidacionColorLibre implements ValidacionRegistro {

    private static final List<String> COLORES = Arrays.asList("AZUL", "ROJO", "VERDE", "AMARILLO");

    @Override
    public void validar(JugadorDTO nuevo, List<JugadorDTO> actuales, int max) throws SalaException {
        if (nuevo == null || nuevo.getColor() == null || nuevo.getColor().trim().isEmpty()) {
            throw new SalaException("Debes seleccionar un color de ficha.");
        }

        String color = nuevo.getColor().trim().toUpperCase();
        if (!COLORES.contains(color)) {
            throw new SalaException("Color inválido: " + color);
        }

        for (JugadorDTO j : actuales) {
            if (j.getColor() != null && j.getColor().trim().equalsIgnoreCase(color)) {
                throw new SalaException("El color " + color + " ya está ocupado por otro jugador.");
            }
        }
    }
}
