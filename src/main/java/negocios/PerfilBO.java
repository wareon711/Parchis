package negocios;

import datos.IPerfilDAO;
import datos.PerfilEntidad;
import datos.PersistenciaException;
import dto.JugadorDTO;
import java.util.Optional;

public class PerfilBO {

    private final IPerfilDAO dao;

    public PerfilBO(IPerfilDAO dao) {
        this.dao = dao;
    }

    public JugadorDTO obtenerOCrear(JugadorDTO nuevo) throws SalaException {
        try {
            Optional<PerfilEntidad> opt = dao.buscar(nuevo.getNombreUsuario());
            if (opt.isPresent()) {
                PerfilEntidad actual = opt.get();
                PerfilEntidad actualizado = new PerfilEntidad(
                        actual.nombre(),
                        nuevo.getColor() != null ? nuevo.getColor() : actual.colorPreferido(),
                        nuevo.getAvatar() != null ? nuevo.getAvatar() : actual.avatar(),
                        actual.partidasJugadas(),
                        actual.partidasGanadas()
                );
                dao.guardar(actualizado);
                return aDTO(actualizado);
            }

            PerfilEntidad e = new PerfilEntidad(
                    nuevo.getNombreUsuario(),
                    nuevo.getColor(),
                    nuevo.getAvatar(),
                    0,
                    0
            );
            dao.guardar(e);
            return aDTO(e);
        } catch (PersistenciaException ex) {
            throw new SalaException("No se pudo acceder a los perfiles.", ex);
        }
    }

    public void registrarPartidaTerminada(String nombreUsuario, boolean gano) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || "BOT".equalsIgnoreCase(nombreUsuario)) {
            return;
        }
        try {
            dao.registrarPartida(nombreUsuario, gano);
        } catch (Exception ignored) {
        }
    }

    public Optional<dto.PerfilDTO> consultarPerfil(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return dao.buscar(nombreUsuario).map(e -> new dto.PerfilDTO(
                e.nombre(),
                e.colorPreferido(),
                e.avatar(),
                e.partidasJugadas(),
                e.partidasGanadas()
            ));
        } catch (PersistenciaException ex) {
            return Optional.empty();
        }
    }

    private JugadorDTO aDTO(PerfilEntidad e) {
        return new JugadorDTO("J1", e.nombre(), e.colorPreferido(), e.avatar(), 0, true);
    }
}
