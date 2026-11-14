package negocios;

import dto.MensajeDTO;
import dto.TipoMensaje;
import java.io.IOException;

public interface IComunicacion {

    void conectar(String host, int puerto) throws IOException;

    void enviar(MensajeDTO mensaje) throws IOException;

    void suscribir(TipoMensaje tipo, ReceptorMensajes r);

    void desconectar();

    boolean estaConectado();
}

