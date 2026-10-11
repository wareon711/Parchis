package dto;

public class JugadorDTO {

    private String idJugador;
    private String nombreUsuario;
    private String color;
    private String avatar;
    private int fichasEnMeta;
    private boolean activo;

    public JugadorDTO() {
    }

    public JugadorDTO(String idJugador, String nombreUsuario, String color, String avatar,
            int fichasEnMeta, boolean activo) {
        this.idJugador = idJugador;
        this.nombreUsuario = nombreUsuario;
        this.color = color;
        this.avatar = avatar;
        this.fichasEnMeta = fichasEnMeta;
        this.activo = activo;
    }

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getFichasEnMeta() {
        return fichasEnMeta;
    }

    public void setFichasEnMeta(int fichasEnMeta) {
        this.fichasEnMeta = fichasEnMeta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
