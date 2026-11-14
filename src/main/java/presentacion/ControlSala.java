package presentacion;

import dto.JugadorDTO;
import dto.SalaDTO;
import negocios.FabricaSubsistemas;
import negocios.IFachadaSala;
import negocios.ObservadorSala;
import negocios.SalaException;

public class ControlSala {

    private final IFachadaSala fachadaSala;

    public ControlSala(int capacidadMax) {
        this.fachadaSala = FabricaSubsistemas.crearFachadaSala(capacidadMax);
    }

    public ControlSala(IFachadaSala fachadaSala) {
        this.fachadaSala = (fachadaSala != null) ? fachadaSala : FabricaSubsistemas.crearFachadaSala(2);
    }

    public IFachadaSala getFachadaSala() {
        return fachadaSala;
    }

    public SalaDTO registrar(String nombre, String color, String avatar) throws SalaException {
        JugadorDTO jugador = new JugadorDTO("J1", nombre, color, avatar, 0, true);
        return fachadaSala.registrarJugador(jugador);
    }

    public void marcarListo(String nombre) throws SalaException {
        fachadaSala.marcarListo(nombre);
    }

    public SalaDTO consultarSala() {
        return fachadaSala.consultarSala();
    }

    public void suscribirObservador(ObservadorSala observador) {
        fachadaSala.agregarObservador(observador);
    }

    public void desuscribirObservador(ObservadorSala observador) {
        fachadaSala.quitarObservador(observador);
    }

    public void salir() {
        fachadaSala.desconectar();
    }
}

