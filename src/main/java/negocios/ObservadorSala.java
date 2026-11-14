package negocios;

import dto.SalaDTO;
import presentacion.ModeloPartida;

public interface ObservadorSala {

    void salaActualizada(SalaDTO sala);

    void conteoRegresivo(int segundos);

    void partidaIniciada(ModeloPartida modelo, String idLocal);

    void errorRecibido(String mensaje);
}

