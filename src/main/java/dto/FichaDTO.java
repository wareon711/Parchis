package dto;

public class FichaDTO {

    private String idFicha;
    private String idJugador;
    private String color;
    private int posicion;
    private boolean enBase;
    private boolean enMeta;
    private boolean movible;

    public FichaDTO() {
    }

    public FichaDTO(String idFicha, String idJugador, String color, int posicion,
            boolean enBase, boolean enMeta, boolean movible) {
        this.idFicha = idFicha;
        this.idJugador = idJugador;
        this.color = color;
        this.posicion = posicion;
        this.enBase = enBase;
        this.enMeta = enMeta;
        this.movible = movible;
    }

    public String getIdFicha() {
        return idFicha;
    }

    public void setIdFicha(String idFicha) {
        this.idFicha = idFicha;
    }

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public boolean isEnBase() {
        return enBase;
    }

    public void setEnBase(boolean enBase) {
        this.enBase = enBase;
    }

    public boolean isEnMeta() {
        return enMeta;
    }

    public void setEnMeta(boolean enMeta) {
        this.enMeta = enMeta;
    }

    public boolean isMovible() {
        return movible;
    }

    public void setMovible(boolean movible) {
        this.movible = movible;
    }
}
