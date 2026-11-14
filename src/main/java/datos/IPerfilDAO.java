package datos;

import java.util.Optional;

public interface IPerfilDAO {

    void guardar(PerfilEntidad perfil) throws PersistenciaException;

    Optional<PerfilEntidad> buscar(String nombre) throws PersistenciaException;

    void registrarPartida(String nombre, boolean gano) throws PersistenciaException;
}

