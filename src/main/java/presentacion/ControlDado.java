package presentacion;

import java.awt.AlphaComposite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ControlDado extends JPanel {

    private static final int TAMANIO_DADO = 48;

    private final JButton botonDado = new JButton();
    private Runnable alClicDado = () -> {};
    private Timer timerDado;

    private boolean orientacionIzquierda = true;
    private boolean mostrarFlecha = false;
    private boolean ocultar = false;
    private boolean habilitado = true;
    private boolean esOponente = false;
    private int valorActual = 0;

    public ControlDado() {
        setOpaque(false);
        setLayout(null);

        botonDado.setContentAreaFilled(false);
        botonDado.setBorderPainted(false);
        botonDado.setFocusPainted(false);
        botonDado.setOpaque(false);
        botonDado.setMargin(new Insets(0, 0, 0, 0));
        botonDado.addActionListener(e -> {
            if (alClicDado != null && !ocultar && habilitado) {
                alClicDado.run();
            }
        });

        add(botonDado);
        setPreferredSize(new Dimension(84, 92));

        mostrarValor(0);
        actualizarPosicionBoton();
    }

    public ControlDado(Runnable alClicDado) {
        this();
        configurar(alClicDado);
    }

    public void configurar(Runnable alClicDado) {
        this.alClicDado = alClicDado != null ? alClicDado : () -> {};
    }

    public void setOrientacionIzquierda(boolean orientacionIzquierda) {
        this.orientacionIzquierda = orientacionIzquierda;
        actualizarPosicionBoton();
        repaint();
    }

    public void setMostrarFlecha(boolean mostrarFlecha) {
        this.mostrarFlecha = mostrarFlecha;
        repaint();
    }

    public void setOcultar(boolean ocultar) {
        this.ocultar = ocultar;
        botonDado.setVisible(!ocultar);
        repaint();
    }

    public void setEsOponente(boolean esOponente) {
        this.esOponente = esOponente;
        mostrarValor(this.valorActual);
    }

    private void actualizarPosicionBoton() {
        int burbujaX = orientacionIzquierda ? 10 : 4;
        int burbujaY = 24;
        int bx = orientacionIzquierda ? burbujaX + 16 : burbujaX + 6;
        int by = burbujaY + 9;
        botonDado.setBounds(bx, by, TAMANIO_DADO, TAMANIO_DADO);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(84, 92);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(84, 92);
    }

    public void mostrarValor(int valor) {
        this.valorActual = valor;
        BufferedImage img = Recursos.dado(valor);
        if (img == null) {
            botonDado.setText(valor == 0 ? "?" : String.valueOf(valor));
            return;
        }
        Image escalada = img.getScaledInstance(TAMANIO_DADO, TAMANIO_DADO, Image.SCALE_SMOOTH);
        if (esOponente) {
            BufferedImage atenuada = new BufferedImage(TAMANIO_DADO, TAMANIO_DADO, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = atenuada.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
            g.drawImage(escalada, 0, 0, null);
            g.dispose();
            botonDado.setIcon(new ImageIcon(atenuada));
        } else {
            botonDado.setIcon(new ImageIcon(escalada));
        }
        repaint();
    }

    public void animarTirada(int valorFinal, Runnable alTerminar) {
        if (timerDado != null && timerDado.isRunning()) {
            timerDado.stop();
        }
        setMostrarFlecha(false);
        habilitar(false);

        final int[] pasos = {0};
        timerDado = new Timer(65, null);
        timerDado.addActionListener(e -> {
            pasos[0]++;
            int aleatorio = 1 + (int) (Math.random() * 6);
            mostrarValor(aleatorio);
            if (pasos[0] >= 8) {
                timerDado.stop();
                mostrarValor(valorFinal);
                if (alTerminar != null) {
                    alTerminar.run();
                }
            }
        });
        timerDado.start();
    }

    public void habilitar(boolean habilitado) {
        this.habilitado = habilitado;
        botonDado.setCursor(habilitado ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (ocultar) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        actualizarPosicionBoton();

        int burbujaX = orientacionIzquierda ? 10 : 4;
        int burbujaY = 24;
        int burbujaAncho = 70;
        int burbujaAlto = 66;

        boolean esInicio = mostrarFlecha && (valorActual == 0);
        BufferedImage imgBurbuja = esInicio ? Recursos.burbujaInicio() : Recursos.burbujaJuego();
        if (imgBurbuja != null) {
            if (orientacionIzquierda) {
                g.drawImage(imgBurbuja, burbujaX, burbujaY, burbujaAncho, burbujaAlto, null);
            } else {
                g.drawImage(imgBurbuja, burbujaX + burbujaAncho, burbujaY, -burbujaAncho, burbujaAlto, null);
            }
        }

        if (esInicio) {
            BufferedImage imgFlecha = Recursos.flecha();
            if (imgFlecha != null) {
                int flechaAncho = 36;
                int flechaAlto = 26;
                int bx = orientacionIzquierda ? burbujaX + 16 : burbujaX + 6;
                int flechaX = bx + (TAMANIO_DADO - flechaAncho) / 2;
                int flechaY = 0;
                g.drawImage(imgFlecha, flechaX, flechaY, flechaAncho, flechaAlto, null);
            }
        }

        g.dispose();
    }
}
