package presentacion;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;

public final class Recursos {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Recursos() {
    }

    public static Icon icono(Image img, int width, int height) {
        if (img == null) {
            return null;
        }
        return new HighResIcon(img, width, height);
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
        return imagen("Perfil.png");
    }

    public static BufferedImage tablero() {
        return imagen("Tablero.png");
    }

    public static BufferedImage fondoPantallas() {
        return imagen("fondo_pantallas.png");
    }

    public static BufferedImage logoLogin() {
        BufferedImage img = imagen("LogoLogin.png");
        return img;
    }

    public static BufferedImage tituloParchis() {
        return imagen("titulo_parchis.png");
    }

    public static BufferedImage iconUser() {
        return imagen("icon_user.png");
    }

    public static BufferedImage iconMail() {
        return imagen("icon_mail.png");
    }

    public static BufferedImage iconLock() {
        return imagen("icon_lock.png");
    }

    public static BufferedImage btnCuenta() {
        return imagen("btn_cuenta.png");
    }

    public static BufferedImage btnModo2J() {
        BufferedImage img = imagen("btn_modo_2j.png");
        if (img == null) {
            img = imagen("ModoOnline2.png");
        }
        return img;
    }

    public static BufferedImage btnModo4J() {
        BufferedImage img = imagen("btn_modo_4j.png");
        if (img == null) {
            img = imagen("ModoOnline4.png");
        }
        return img;
    }

    public static BufferedImage headerInicio() {
        return imagen("header_inicio.png");
    }

    public static BufferedImage modoOnline2() {
        return imagen("ModoOnline2.png");
    }

    public static BufferedImage modoOnline4() {
        return imagen("ModoOnline4.png");
    }

    public static BufferedImage pawn(String color) {
        if (color == null) {
            return imagen("pawn_azul.png");
        }
        switch (color.toUpperCase()) {
            case "AZUL":
                return imagen("pawn_azul.png");
            case "VERDE":
                return imagen("pawn_verde.png");
            case "ROJO":
                return imagen("pawn_rojo.png");
            case "AMARILLO":
            default:
                return imagen("pawn_amarillo.png");
        }
    }

    public static BufferedImage boardModal() {
        BufferedImage img = imagen("board_modal.png");
        if (img == null) {
            img = logoLogin();
        }
        return img;
    }

    public static BufferedImage btnIniciar() {
        return imagen("btn_iniciar.png");
    }

    public static BufferedImage btnContinuar() {
        if (CACHE.containsKey("__btn_continuar_custom__")) {
            return CACHE.get("__btn_continuar_custom__");
        }
        BufferedImage base = btnIniciar();
        if (base == null) {
            return null;
        }
        int w = base.getWidth();
        int h = base.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = out.createGraphics();
        g2.drawImage(base, 0, 0, null);

        int sampleX = Math.max(10, (int) (w * 0.14));
        int startX = (int) (w * 0.15);
        int endX = (int) (w * 0.85);
        for (int y = 0; y < h; y++) {
            int rgb = base.getRGB(sampleX, y);
            for (int x = startX; x <= endX; x++) {
                out.setRGB(x, y, rgb);
            }
        }

        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int fontSize = Math.max(18, (int) (h * 0.36));
        java.awt.Font font = new java.awt.Font("Arial Black", java.awt.Font.BOLD, fontSize);
        g2.setFont(font);
        String texto = "CONTINUAR";
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(texto)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent() - (int) (h * 0.04);

        g2.setColor(new java.awt.Color(25, 95, 20, 160));
        g2.drawString(texto, tx, ty + 2);
        g2.setColor(java.awt.Color.WHITE);
        g2.drawString(texto, tx, ty);
        g2.dispose();

        CACHE.put("__btn_continuar_custom__", out);
        return out;
    }

    public static BufferedImage trofeo() {
        return imagen("trofeo.png");
    }

    public static BufferedImage liston() {
        return imagen("liston.png");
    }

    public static BufferedImage brillo() {
        return imagen("brillo.png");
    }

    public static String codigoAvatar(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "AV1";
        }
        if (raw.contains("|")) {
            String[] p = raw.split("\\|");
            if (p.length >= 2 && !p[1].isEmpty()) {
                return p[1].toUpperCase();
            }
        }
        String u = raw.toUpperCase();
        if ("AV1".equals(u) || "AV2".equals(u) || "AV3".equals(u) || "AV4".equals(u) || "BOT".equals(u)) {
            return u;
        }
        return "AV1";
    }

    public static BufferedImage avatar(String raw) {
        String cod = codigoAvatar(raw);
        if ("AV1".equals(cod)) {
            BufferedImage base = perfil();
            if (base != null) {
                return base;
            }
        }
        String clave = "__avatar_" + cod + "__";
        if (CACHE.containsKey(clave)) {
            return CACHE.get(clave);
        }
        int s = 88;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        java.awt.Color bg;
        java.awt.Color borde;
        String simbolo;
        switch (cod) {
            case "AV2":
                bg = new java.awt.Color(25, 95, 165);
                borde = new java.awt.Color(90, 210, 255);
                simbolo = "★";
                break;
            case "AV3":
                bg = new java.awt.Color(150, 35, 85);
                borde = new java.awt.Color(255, 130, 185);
                simbolo = "⚡";
                break;
            case "AV4":
                bg = new java.awt.Color(20, 115, 75);
                borde = new java.awt.Color(95, 240, 165);
                simbolo = "♛";
                break;
            case "BOT":
                bg = new java.awt.Color(65, 55, 115);
                borde = new java.awt.Color(190, 175, 255);
                simbolo = "🤖";
                break;
            default:
                bg = new java.awt.Color(40, 30, 95);
                borde = new java.awt.Color(245, 195, 65);
                simbolo = "♟";
                break;
        }

        g.setColor(bg);
        g.fillOval(4, 4, s - 8, s - 8);
        g.setColor(borde);
        g.setStroke(new java.awt.BasicStroke(4.5f));
        g.drawOval(4, 4, s - 8, s - 8);

        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("Segoe UI Symbol", java.awt.Font.BOLD, 38));
        java.awt.FontMetrics fm = g.getFontMetrics();
        int tx = (s - fm.stringWidth(simbolo)) / 2;
        int ty = (s - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(simbolo, tx, ty);
        g.dispose();

        CACHE.put(clave, img);
        return img;
    }
}
