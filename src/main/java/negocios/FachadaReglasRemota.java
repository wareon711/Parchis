package negocios;

import dto.EstadoPartidaDTO;
import dto.MensajeDTO;
import dto.TipoMensaje;
import java.io.IOException;

public class FachadaReglasRemota implements IFachadaReglas {

    private final IComunicacion comunicacion;
    private volatile EstadoPartidaDTO estadoActual;

    public FachadaReglasRemota(IComunicacion comunicacion, EstadoPartidaDTO estadoInicial) {
        this.comunicacion = comunicacion;
        this.estadoActual = estadoInicial;
    }

    public void setEstadoActual(EstadoPartidaDTO estado) {
        this.estadoActual = estado;
    }

    @Override
    public EstadoPartidaDTO lanzarDado(String idJugador) {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(TipoMensaje.TIRADA_DADO, idJugador, "BROKER", idJugador));
            } catch (IOException ignored) {
            }
        }
        return estadoActual;
    }

    @Override
    public EstadoPartidaDTO moverFicha(String idJugador, String idFicha) {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(TipoMensaje.MOVIMIENTO_FICHA, idJugador, "BROKER", new String[]{idJugador, idFicha}));
            } catch (IOException ignored) {
            }
        }
        return estadoActual;
    }

    @Override
    public EstadoPartidaDTO pasarTurno(String idJugador) {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(TipoMensaje.PASAR_TURNO, idJugador, "BROKER", idJugador));
            } catch (IOException ignored) {
            }
        }
        return estadoActual;
    }

    @Override
    public EstadoPartidaDTO abandonarPartida(String idJugador) {
        if (comunicacion != null && comunicacion.estaConectado()) {
            try {
                comunicacion.enviar(new MensajeDTO(TipoMensaje.DESCONEXION, idJugador, "BROKER", idJugador));
            } catch (IOException ignored) {
            }
        }
        return estadoActual;
    }

    @Override
    public EstadoPartidaDTO obtenerEstadoPartida() {
        return estadoActual;
    }
}
