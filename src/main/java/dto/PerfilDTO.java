package dto;

import java.io.Serializable;

public record PerfilDTO(
    String nombre,
    String colorPreferido,
    String avatar,
    int partidasJugadas,
    int partidasGanadas
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

