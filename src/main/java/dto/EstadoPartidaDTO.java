package dto;

import java.util.ArrayList;
import java.util.List;

public class EstadoPartidaDTO {

    private final List<JugadorDTO> jugadores = new ArrayList<>();
    private final List<FichaDTO> fichas = new ArrayList<>();
    private String idJugadorEnTurno;
    private String idJugadorTirador;
    private int valorDado;
    private int tiradaId;
    private boolean esperaFicha;
    private String mensaje;
    private String idGanador;

    public List<JugadorDTO> getJugadores() {
        return jugadores;
    }

    public List<FichaDTO> getFichas() {
        return fichas;
    }

    public String getIdJugadorEnTurno() {
        return idJugadorEnTurno;
    }

    public void setIdJugadorEnTurno(String idJugadorEnTurno) {
        this.idJugadorEnTurno = idJugadorEnTurno;
    }

    public String getIdJugadorTirador() {
        return idJugadorTirador;
    }

    public void setIdJugadorTirador(String idJugadorTirador) {
        this.idJugadorTirador = idJugadorTirador;
    }

    public int getValorDado() {
        return valorDado;
    }

    public void setValorDado(int valorDado) {
        this.valorDado = valorDado;
    }

    public int getTiradaId() {
        return tiradaId;
    }

    public void setTiradaId(int tiradaId) {
        this.tiradaId = tiradaId;
    }

    public boolean isEsperaFicha() {
        return esperaFicha;
    }

    public void setEsperaFicha(boolean esperaFicha) {
        this.esperaFicha = esperaFicha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getIdGanador() {
        return idGanador;
    }

    public void setIdGanador(String idGanador) {
        this.idGanador = idGanador;
    }
}
