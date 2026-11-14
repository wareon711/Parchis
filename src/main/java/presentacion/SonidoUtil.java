package presentacion;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class SonidoUtil {

    private static volatile boolean activo = true;

    private SonidoUtil() {
    }

    public static boolean isActivo() {
        return activo;
    }

    public static void setActivo(boolean valor) {
        activo = valor;
    }

    public static void dado() {
        reproducir(new int[]{340, 480, 620}, new int[]{45, 45, 70});
    }

    public static void paso() {
        reproducir(new int[]{520}, new int[]{55});
    }

    public static void premio() {
        reproducir(new int[]{587, 880}, new int[]{90, 140});
    }

    public static void turno() {
        reproducir(new int[]{660, 880}, new int[]{75, 110});
    }

    public static void victoria() {
        reproducir(new int[]{523, 659, 784, 1046}, new int[]{110, 110, 110, 240});
    }

    private static void reproducir(int[] freqs, int[] durs) {
        if (!activo) {
            return;
        }
        Thread hilo = new Thread(() -> {
            try {
                float tasa = 22050f;
                AudioFormat fmt = new AudioFormat(tasa, 8, 1, true, false);
                try (SourceDataLine linea = AudioSystem.getSourceDataLine(fmt)) {
                    linea.open(fmt);
                    linea.start();
                    for (int i = 0; i < freqs.length; i++) {
                        int muestras = (int) ((durs[i] / 1000.0) * tasa);
                        byte[] buf = new byte[muestras];
                        for (int m = 0; m < muestras; m++) {
                            double ang = 2.0 * Math.PI * m * freqs[i] / tasa;
                            double env = 1.0 - ((double) m / muestras);
                            buf[m] = (byte) (Math.sin(ang) * 38.0 * env);
                        }
                        linea.write(buf, 0, buf.length);
                    }
                    linea.drain();
                }
            } catch (Exception ignored) {
            }
        }, "Sonido-FX");
        hilo.setDaemon(true);
        hilo.start();
    }
}

