package presentacion;

public class ControladorPartida {

    private final String idJugadorLocal;
    private final ModeloPartida modelo;

    public ControladorPartida(String idJugadorLocal, ModeloPartida modelo) {
        this.idJugadorLocal = idJugadorLocal;
        this.modelo = modelo;
    }

    public void lanzarDado() {
        modelo.lanzarDado(idJugadorLocal);
    }

    public void moverFicha(String idFicha) {
        modelo.moverFicha(idJugadorLocal, idFicha);
    }
}
