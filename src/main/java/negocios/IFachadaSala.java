package negocios;

import dto.JugadorDTO;
import dto.SalaDTO;

public interface IFachadaSala {

    SalaDTO registrarJugador(JugadorDTO jugador) throws SalaException;

    void marcarListo(String nombreJugador) throws SalaException;

    SalaDTO consultarSala();

    void agregarObservador(ObservadorSala observador);

    void quitarObservador(ObservadorSala observador);

    void desconectar();
}

