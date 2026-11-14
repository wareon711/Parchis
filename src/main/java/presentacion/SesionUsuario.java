package presentacion;

public class SesionUsuario {

    private static String usuario = "Jugador";
    private static String colorElegido = "VERDE";
    private static String avatarElegido = "AV1";
    private static int ultimoModoJugadores = 2;
    private static int partidasJugadas = 0;
    private static int victorias = 0;

    public static String getUsuario() {
        return usuario;
    }

    public static void setUsuario(String u) {
        if (u != null && !u.trim().isEmpty()) {
            usuario = u.trim();
        }
    }

    public static String getColorElegido() {
        return colorElegido;
    }

    public static void setColorElegido(String c) {
        if (c != null && !c.trim().isEmpty()) {
            colorElegido = c.trim().toUpperCase();
        }
    }

    public static String getAvatarElegido() {
        return avatarElegido;
    }

    public static void setAvatarElegido(String av) {
        if (av != null && !av.trim().isEmpty()) {
            avatarElegido = av.trim().toUpperCase();
        }
    }

    public static int getUltimoModoJugadores() {
        return ultimoModoJugadores;
    }

    public static void setUltimoModoJugadores(int modo) {
        ultimoModoJugadores = (modo == 4) ? 4 : 2;
    }

    public static int getPartidasJugadas() {
        return partidasJugadas;
    }

    public static void sumarPartida() {
        partidasJugadas++;
    }

    public static int getVictorias() {
        return victorias;
    }

    public static void sumarVictoria() {
        victorias++;
    }
}

