package negocios;

public class SalaException extends Exception {

    public SalaException(String mensaje) {
        super(mensaje);
    }

    public SalaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

