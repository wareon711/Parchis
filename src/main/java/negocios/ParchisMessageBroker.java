package negocios;

import dto.MensajeDTO;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ParchisMessageBroker {

    public static final int PUERTO_DEFAULT = 27015;

    private final int puerto;
    private ServerSocket serverSocket;
    private boolean activo = false;
    private final Set<ClienteHandler> clientes = Collections.synchronizedSet(new HashSet<>());
    private final BlockingQueue<MensajeDTO> cola = new LinkedBlockingQueue<>();
    private Thread hiloCola;
    private Thread hiloClientes;

    public ParchisMessageBroker() {
        this(PUERTO_DEFAULT);
    }

    public ParchisMessageBroker(int puerto) {
        this.puerto = puerto;
    }

    public synchronized void iniciar() throws IOException {
        if (activo) {
            return;
        }
        serverSocket = new ServerSocket(puerto);
        activo = true;

        iniciarCola();
        aceptarClientes();
    }

    private void iniciarCola() {
        hiloCola = new Thread(() -> {
            while (activo) {
                try {
                    MensajeDTO msg = cola.take();
                    difundir(msg);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception ignored) {
                }
            }
        });
        hiloCola.setDaemon(true);
        hiloCola.start();
    }

    private void aceptarClientes() {
        hiloClientes = new Thread(() -> {
            while (activo) {
                try {
                    Socket socket = serverSocket.accept();
                    ClienteHandler cliente = new ClienteHandler(socket);
                    clientes.add(cliente);
                    cliente.start();
                } catch (Exception e) {
                    if (!activo) {
                        break;
                    }
                }
            }
        });
        hiloClientes.setDaemon(true);
        hiloClientes.start();
    }

    public synchronized void detener() {
        activo = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
        if (hiloCola != null) {
            hiloCola.interrupt();
        }
        if (hiloClientes != null) {
            hiloClientes.interrupt();
        }

        synchronized (clientes) {
            for (ClienteHandler c : clientes) {
                c.cerrar();
            }
            clientes.clear();
        }
    }

    public void publicarMensaje(MensajeDTO mensaje) {
        if (mensaje != null) {
            cola.offer(mensaje);
        }
    }

    private void difundir(MensajeDTO mensaje) {
        synchronized (clientes) {
            for (ClienteHandler c : clientes) {
                c.enviar(mensaje);
            }
        }
    }

    private class ClienteHandler extends Thread {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean conectado = true;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                while (conectado && activo) {
                    Object obj = in.readObject();
                    if (obj instanceof MensajeDTO msg) {
                        cola.offer(msg);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                cerrar();
                clientes.remove(this);
            }
        }

        public synchronized void enviar(MensajeDTO mensaje) {
            if (!conectado || out == null) {
                return;
            }
            try {
                out.writeObject(mensaje);
                out.flush();
            } catch (Exception e) {
                cerrar();
            }
        }

        public void cerrar() {
            conectado = false;
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
