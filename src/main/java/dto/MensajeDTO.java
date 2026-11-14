package dto;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class MensajeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicLong SECUENCIA = new AtomicLong(0);

    private final TipoMensaje tipo;
    private final String remitente;
    private final String destinatario;
    private final Object payload;
    private final long timestamp;

    public MensajeDTO(TipoMensaje tipo, String remitente, String destinatario, Object payload) {
        this.tipo = tipo;
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis() * 1000L + (SECUENCIA.incrementAndGet() % 1000L);
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    public String getRemitente() {
        return remitente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public Object getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "MensajeDTO{" + "tipo=" + tipo + ", remitente=" + remitente + ", destinatario=" + destinatario + '}';
    }
}
