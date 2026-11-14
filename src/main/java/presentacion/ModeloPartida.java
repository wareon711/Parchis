package presentacion;

import dto.EstadoPartidaDTO;
import java.util.ArrayList;
import java.util.List;
import negocios.IFachadaReglas;

public class ModeloPartida {

    private EstadoPartidaDTO estadoActual;
    private final List<IObservadorPartida> observadores = new ArrayList<>();
    private final IFachadaReglas fachada;

    public ModeloPartida(IFachadaReglas fachada) {
        this.fachada = fachada;
    }

    public void agregarObservador(IObservadorPartida observador) {
        observadores.add(observador);
        estadoActual = fachada.obtenerEstadoPartida();
        observador.actualizar(estadoActual);
    }

    public void quitarObservador(IObservadorPartida observador) {
        observadores.remove(observador);
    }

    public interface ListenerCambioEstado {
        void onEstadoCambiado(EstadoPartidaDTO estado);
    }

    private ListenerCambioEstado listenerCambio;

    public void setListenerCambio(ListenerCambioEstado listenerCambio) {
        this.listenerCambio = listenerCambio;
    }

    public void lanzarDado(String idJugador) {
        estadoActual = fachada.lanzarDado(idJugador);
        if (!(fachada instanceof negocios.FachadaReglasRemota)) {
            avisarObservadores();
            avisarCambio();
        }
    }

    public void moverFicha(String idJugador, String idFicha) {
        estadoActual = fachada.moverFicha(idJugador, idFicha);
        if (!(fachada instanceof negocios.FachadaReglasRemota)) {
            avisarObservadores();
            avisarCambio();
        }
    }

    public void pasarTurno(String idJugador) {
        estadoActual = fachada.pasarTurno(idJugador);
        if (!(fachada instanceof negocios.FachadaReglasRemota)) {
            avisarObservadores();
            avisarCambio();
        }
    }

    public void abandonarPartida(String idJugador) {
        estadoActual = fachada.abandonarPartida(idJugador);
        if (!(fachada instanceof negocios.FachadaReglasRemota)) {
            avisarObservadores();
            avisarCambio();
        }
    }

    private void avisarCambio() {
        if (listenerCambio != null && estadoActual != null) {
            listenerCambio.onEstadoCambiado(estadoActual);
        }
    }

    public void actualizarEstado(EstadoPartidaDTO nuevoEstado) {
        if (nuevoEstado != null) {
            this.estadoActual = nuevoEstado;
            avisarObservadores();
        }
    }

    public EstadoPartidaDTO getEstadoActual() {
        return estadoActual != null ? estadoActual : fachada.obtenerEstadoPartida();
    }

    public void avisarObservadores() {
        for (IObservadorPartida observador : new ArrayList<>(observadores)) {
            observador.actualizar(estadoActual);
        }
    }
}
