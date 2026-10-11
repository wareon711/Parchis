package presentacion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public final class Recursos {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Recursos() {
    }

    public static synchronized BufferedImage imagen(String nombre) {
        if (CACHE.containsKey(nombre)) {
            return CACHE.get(nombre);
        }

        BufferedImage img = null;
        try (InputStream in = Recursos.class.getResourceAsStream("/img/" + nombre)) {
            if (in != null) {
                img = ImageIO.read(in);
            }
        } catch (Exception e) {
        }

        if (img == null) {
            String[] rutas = {
                "src/main/resources/img/" + nombre,
                "target/classes/img/" + nombre
            };
            for (String ruta : rutas) {
                File archivo = new File(ruta);
                if (archivo.exists()) {
                    try {
                        img = ImageIO.read(archivo);
                        if (img != null) {
                            break;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        CACHE.put(nombre, img);
        return img;
    }

    public static BufferedImage ficha(String color) {
        switch (color) {
            case "AZUL":
                return imagen("ficha_azul.png");
            case "VERDE":
                return imagen("ficha_verde.png");
            case "ROJO":
                return imagen("ficha_roja.png");
            default:
                return imagen("ficha_amarilla.png");
        }
    }

    public static BufferedImage dado(int valor) {
        if (valor == 0) {
            return dadoInicio();
        }
        return imagen("dado_" + valor + ".png");
    }

    public static BufferedImage flecha() {
        BufferedImage img = imagen("flecha.png");
        if (img != null && img.getWidth() == 110 && img.getHeight() == 110) {
            return img.getSubimage(25, 25, 60, 60);
        }
        return img;
    }

    public static BufferedImage burbujaInicio() {
        BufferedImage img = imagen("Burbuja de inisio.png");
        if (img == null) {
            img = imagen("burbuja_inicio.png");
        }
        return img;
    }

    public static BufferedImage burbujaJuego() {
        BufferedImage img = imagen("burbuja de juego.png");
        if (img == null) {
            img = imagen("burbuja_juego.png");
        }
        return img;
    }

    public static BufferedImage dadoInicio() {
        BufferedImage img = imagen("dado de inicio.png");
        if (img == null) {
            img = imagen("dado_inicio.png");
        }
        return img;
    }

    public static BufferedImage perfil() {
        BufferedImage img = imagen("Perfil.png");
        if (img == null) {
            img = imagen("avatar.png");
        }
        return img;
    }

    public static BufferedImage tablero() {
        return imagen("Tablero.png");
    }
}
