package datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class PerfilDAOMySQL implements IPerfilDAO {

    public PerfilDAOMySQL() {
        crearTabla();
    }

    private void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS perfil ("
                + "nombre VARCHAR(30) PRIMARY KEY, "
                + "color_preferido VARCHAR(15) NOT NULL DEFAULT 'VERDE', "
                + "avatar VARCHAR(50) NOT NULL DEFAULT 'AV1', "
                + "partidas_jugadas INT UNSIGNED NOT NULL DEFAULT 0, "
                + "partidas_ganadas INT UNSIGNED NOT NULL DEFAULT 0"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void guardar(PerfilEntidad perfil) throws PersistenciaException {
        String sql = "INSERT INTO perfil (nombre, color_preferido, avatar, partidas_jugadas, partidas_ganadas) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE color_preferido = VALUES(color_preferido), avatar = VALUES(avatar)";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, perfil.nombre());
            ps.setString(2, perfil.colorPreferido());
            ps.setString(3, perfil.avatar());
            ps.setInt(4, perfil.partidasJugadas());
            ps.setInt(5, perfil.partidasGanadas());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar perfil: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PerfilEntidad> buscar(String nombre) throws PersistenciaException {
        String sql = "SELECT nombre, color_preferido, avatar, partidas_jugadas, partidas_ganadas FROM perfil WHERE nombre = ?";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new PerfilEntidad(
                            rs.getString("nombre"),
                            rs.getString("color_preferido"),
                            rs.getString("avatar"),
                            rs.getInt("partidas_jugadas"),
                            rs.getInt("partidas_ganadas")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar perfil: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void registrarPartida(String nombre, boolean gano) throws PersistenciaException {
        String sql = "UPDATE perfil SET partidas_jugadas = partidas_jugadas + 1, "
                + "partidas_ganadas = partidas_ganadas + ? WHERE nombre = ?";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gano ? 1 : 0);
            ps.setString(2, nombre);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar partida: " + e.getMessage(), e);
        }
    }
}
