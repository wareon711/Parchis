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

    public void lanzarDado(String idJugador) {
        estadoActual = fachada.lanzarDado(idJugador);
        notificarObservadores();
    }

    public void moverFicha(String idJugador, String idFicha) {
        estadoActual = fachada.moverFicha(idJugador, idFicha);
        notificarObservadores();
    }

    public void notificarObservadores() {
        for (IObservadorPartida observador : new ArrayList<>(observadores)) {
            observador.actualizar(estadoActual);
        }
    }
}
