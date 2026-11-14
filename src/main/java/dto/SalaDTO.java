package dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<JugadorDTO> jugadores;
    private final Set<String> coloresLibres;
    private final int requeridos;
    private final boolean llena;

    public SalaDTO(List<JugadorDTO> jugadores, Set<String> coloresLibres, int requeridos) {
        this.jugadores = (jugadores != null) ? new ArrayList<>(jugadores) : new ArrayList<>();
        this.coloresLibres = (coloresLibres != null) ? new HashSet<>(coloresLibres) : new HashSet<>();
        this.requeridos = requeridos;
        this.llena = this.jugadores.size() >= requeridos;
    }

    public List<JugadorDTO> getJugadores() {
        return Collections.unmodifiableList(jugadores);
    }

    public Set<String> getColoresLibres() {
        return Collections.unmodifiableSet(coloresLibres);
    }

    public int getRequeridos() {
        return requeridos;
    }

    public int getConectados() {
        return jugadores.size();
    }

    public boolean isLlena() {
        return llena;
    }
}

