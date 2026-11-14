package negocios;

import dto.MensajeDTO;
import dto.TipoMensaje;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ComunicacionBroker implements IComunicacion {

    private static final String[] BROKERS_NUBE = {"broker.emqx.io", "broker.hivemq.com"};

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final List<CanalMqtt> canalesMqtt = new CopyOnWriteArrayList<>();
    private final Set<String> mensajesVistos = Collections.synchronizedSet(new HashSet<>());
    private boolean modoMqtt = false;
    private String topicoSala;
    private volatile boolean conectado = false;
    private final Map<TipoMensaje, List<ReceptorMensajes>> suscriptores = new ConcurrentHashMap<>();
    private Thread hiloEscucha;

    @Override
    public synchronized void conectar(String host, int puerto) throws IOException {
        if (conectado) {
            return;
        }

        if (host != null && host.startsWith("SALA:")) {
            String codigo = host.substring(5).trim().replace(" ", "").replace("-", "").toUpperCase();
            if (conectarMqttMultiple(codigo)) {
                return;
            }
            String ipFallback = RedUtil.resolverIP(codigo);
            try {
                conectarTcp(ipFallback, puerto, 1200);
                return;
            } catch (IOException e) {
                conectarTcp("127.0.0.1", puerto, 1000);
                return;
            }
        }

        conectarTcp(host, puerto, 2000);
    }

    private void conectarTcp(String host, int puerto, int timeoutMs) throws IOException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, puerto), timeoutMs);
        ObjectOutputStream oOut = new ObjectOutputStream(s.getOutputStream());
        oOut.flush();
        ObjectInputStream oIn = new ObjectInputStream(s.getInputStream());
        this.socket = s;
        this.out = oOut;
        this.in = oIn;
        this.modoMqtt = false;
        this.conectado = true;

        hiloEscucha = new Thread(this::escuchar, "Broker-TCP-Listener");
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }

    private boolean conectarMqttMultiple(String codigo) {
        this.topicoSala = "parchis/sala/" + codigo;
        canalesMqtt.clear();

        Thread[] hilos = new Thread[BROKERS_NUBE.length];
        for (int i = 0; i < BROKERS_NUBE.length; i++) {
            final String servidor = BROKERS_NUBE[i];
            hilos[i] = new Thread(() -> {
                CanalMqtt canal = abrirCanalMqtt(servidor, topicoSala);
                if (canal != null) {
                    canalesMqtt.add(canal);
                }
            });
            hilos[i].setDaemon(true);
            hilos[i].start();
        }

        for (Thread t : hilos) {
            try {
                t.join(2800);
            } catch (InterruptedException ignored) {
            }
        }

        if (!canalesMqtt.isEmpty()) {
            this.modoMqtt = true;
            this.conectado = true;
            for (CanalMqtt c : canalesMqtt) {
                c.iniciarHilos();
            }
            return true;
        }
        return false;
    }

    private CanalMqtt abrirCanalMqtt(String servidor, String topico) {
        String idCliente = "parchis-" + UUID.randomUUID().toString().substring(0, 8);
        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(servidor, 1883), 2400);
            s.setSoTimeout(2400);
            DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
            DataInputStream dIn = new DataInputStream(s.getInputStream());

            enviarConnectMqtt(dOut, idCliente);
            int tipo = dIn.readUnsignedByte();
            int len = leerLongitudMqtt(dIn);
            byte[] ack = new byte[len];
            dIn.readFully(ack);
            if ((tipo & 0xF0) != 0x20 || ack.length < 2 || ack[1] != 0) {
                s.close();
                return null;
            }

            enviarSubscribeMqtt(dOut, topico);
            int subTipo = dIn.readUnsignedByte();
            int subLen = leerLongitudMqtt(dIn);
            byte[] subAck = new byte[subLen];
            dIn.readFully(subAck);
            if ((subTipo & 0xF0) != 0x90) {
                s.close();
                return null;
            }

            s.setSoTimeout(0);
            return new CanalMqtt(s, dOut, dIn);
        } catch (Exception e) {
            try {
                if (s != null) s.close();
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private void enviarConnectMqtt(DataOutputStream dOut, String idCliente) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        DataOutputStream bOut = new DataOutputStream(body);
        escribirTextoMqtt(bOut, "MQTT");
        bOut.writeByte(4);
        bOut.writeByte(2);
        bOut.writeShort(60);
        escribirTextoMqtt(bOut, idCliente);
        byte[] bytes = body.toByteArray();

        dOut.writeByte(0x10);
        escribirLongitudMqtt(dOut, bytes.length);
        dOut.write(bytes);
        dOut.flush();
    }

    private void enviarSubscribeMqtt(DataOutputStream dOut, String topico) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        DataOutputStream bOut = new DataOutputStream(body);
        bOut.writeShort(1);
        escribirTextoMqtt(bOut, topico);
        bOut.writeByte(0);
        byte[] bytes = body.toByteArray();

        dOut.writeByte(0x82);
        escribirLongitudMqtt(dOut, bytes.length);
        dOut.write(bytes);
        dOut.flush();
    }

    private void escribirTextoMqtt(DataOutputStream dOut, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        dOut.writeShort(bytes.length);
        dOut.write(bytes);
    }

    private void escribirLongitudMqtt(DataOutputStream dOut, int valor) throws IOException {
        do {
            int b = valor % 128;
            valor /= 128;
            if (valor > 0) {
                b |= 128;
            }
            dOut.writeByte(b);
        } while (valor > 0);
    }

    private int leerLongitudMqtt(DataInputStream dIn) throws IOException {
        int mult = 1;
        int valor = 0;
        int b;
        do {
            b = dIn.readUnsignedByte();
            valor += (b & 127) * mult;
            mult *= 128;
        } while ((b & 128) != 0);
        return valor;
    }

    private void escuchar() {
        try {
            while (conectado) {
                Object obj = in.readObject();
                if (obj instanceof MensajeDTO msg) {
                    avisarUnico(msg);
                }
            }
        } catch (Exception ignored) {
        } finally {
            desconectar();
        }
    }

    private void avisarUnico(MensajeDTO msg) {
        if (msg == null) return;
        String clave = msg.getTipo() + ":" + msg.getRemitente() + ":" + msg.getTimestamp();
        if (!mensajesVistos.add(clave)) {
            return;
        }
        avisar(msg);
    }

    private void avisar(MensajeDTO msg) {
        List<ReceptorMensajes> lista = suscriptores.get(msg.getTipo());
        if (lista != null) {
            for (ReceptorMensajes receptor : lista) {
                try {
                    receptor.recibir(msg);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public synchronized void enviar(MensajeDTO mensaje) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión con el Message Broker.");
        }
        if (modoMqtt && !canalesMqtt.isEmpty()) {
            ByteArrayOutputStream objOut = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(objOut)) {
                oos.writeObject(mensaje);
            }
            byte[] payload = objOut.toByteArray();

            ByteArrayOutputStream pkt = new ByteArrayOutputStream();
            DataOutputStream pOut = new DataOutputStream(pkt);
            escribirTextoMqtt(pOut, topicoSala);
            pOut.write(payload);
            byte[] bytes = pkt.toByteArray();

            boolean ok = false;
            for (CanalMqtt c : canalesMqtt) {
                if (c.publicar(bytes)) {
                    ok = true;
                }
            }
            if (!ok) {
                throw new IOException("No se pudo publicar en el broker.");
            }
            return;
        }
        if (out == null) {
            throw new IOException("No hay canal de salida.");
        }
        out.writeObject(mensaje);
        out.flush();
    }

    @Override
    public void suscribir(TipoMensaje tipo, ReceptorMensajes r) {
        if (tipo != null && r != null) {
            suscriptores.computeIfAbsent(tipo, k -> new CopyOnWriteArrayList<>()).add(r);
        }
    }

    @Override
    public synchronized void desconectar() {
        conectado = false;
        for (CanalMqtt c : canalesMqtt) {
            c.cerrar();
        }
        canalesMqtt.clear();
        try {
            if (out != null) out.close();
        } catch (Exception ignored) {}
        try {
            if (in != null) in.close();
        } catch (Exception ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}
        if (hiloEscucha != null && hiloEscucha.isAlive()) {
            hiloEscucha.interrupt();
        }
    }

    @Override
    public boolean estaConectado() {
        if (modoMqtt) {
            for (CanalMqtt c : canalesMqtt) {
                if (c.estaActivo()) return true;
            }
            return false;
        }
        return conectado && socket != null && socket.isConnected() && !socket.isClosed();
    }

    private class CanalMqtt {
        private final Socket sock;
        private final DataOutputStream dOut;
        private final DataInputStream dIn;
        private volatile boolean activo = true;
        private Thread lector;
        private Thread ping;

        CanalMqtt(Socket sock, DataOutputStream dOut, DataInputStream dIn) {
            this.sock = sock;
            this.dOut = dOut;
            this.dIn = dIn;
        }

        void iniciarHilos() {
            lector = new Thread(this::leerLoop, "MQTT-Reader");
            lector.setDaemon(true);
            lector.start();

            ping = new Thread(this::pingLoop, "MQTT-Ping");
            ping.setDaemon(true);
            ping.start();
        }

        private void leerLoop() {
            try {
                while (conectado && activo) {
                    int header = dIn.readUnsignedByte();
                    int len = leerLongitudMqtt(dIn);
                    byte[] data = new byte[len];
                    dIn.readFully(data);

                    int tipo = header & 0xF0;
                    if (tipo == 0x30) {
                        DataInputStream pIn = new DataInputStream(new ByteArrayInputStream(data));
                        int topicLen = pIn.readUnsignedShort();
                        pIn.skipBytes(topicLen);
                        int qos = (header & 0x06) >> 1;
                        if (qos > 0) {
                            pIn.readUnsignedShort();
                        }
                        int disp = pIn.available();
                        byte[] objBytes = new byte[disp];
                        pIn.readFully(objBytes);
                        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(objBytes))) {
                            Object obj = ois.readObject();
                            if (obj instanceof MensajeDTO msg) {
                                avisarUnico(msg);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            } finally {
                cerrar();
            }
        }

        private void pingLoop() {
            while (conectado && activo) {
                try {
                    Thread.sleep(20000);
                    synchronized (this) {
                        if (activo) {
                            dOut.writeByte(0xC0);
                            dOut.writeByte(0x00);
                            dOut.flush();
                        }
                    }
                } catch (Exception e) {
                    break;
                }
            }
        }

        synchronized boolean publicar(byte[] bytes) {
            if (!activo) return false;
            try {
                dOut.writeByte(0x30);
                escribirLongitudMqtt(dOut, bytes.length);
                dOut.write(bytes);
                dOut.flush();
                return true;
            } catch (Exception e) {
                cerrar();
                return false;
            }
        }

        boolean estaActivo() {
            return activo && sock != null && sock.isConnected() && !sock.isClosed();
        }

        void cerrar() {
            activo = false;
            try {
                if (dOut != null) dOut.close();
            } catch (Exception ignored) {}
            try {
                if (dIn != null) dIn.close();
            } catch (Exception ignored) {}
            try {
                if (sock != null && !sock.isClosed()) sock.close();
            } catch (Exception ignored) {}
        }
    }
}
