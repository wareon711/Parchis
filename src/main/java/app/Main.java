package app;

import java.awt.Point;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import negocios.FachadaReglasMock;
import negocios.IFachadaReglas;
import presentacion.ControladorPartida;
import presentacion.ModeloPartida;
import presentacion.VistaPartida;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
            }

            IFachadaReglas fachada = new FachadaReglasMock(2);
            ModeloPartida modelo = new ModeloPartida(fachada);

            ControladorPartida controlador1 = new ControladorPartida("J1", modelo);
            ControladorPartida controlador2 = new ControladorPartida("J2", modelo);

            VistaPartida vista1 = new VistaPartida("J1", "Parchís – Jugador 1 (Azul)", controlador1, modelo);
            VistaPartida vista2 = new VistaPartida("J2", "Parchís – Jugador 2 (Verde)", controlador2, modelo);

            vista1.setLocation(new Point(30, 20));
            vista2.setLocation(new Point(550, 20));

            vista1.setVisible(true);
            vista2.setVisible(true);
        });
    }
}
