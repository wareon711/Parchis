package datos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PerfilDAOMemoria implements IPerfilDAO {

    private static final String ARCHIVO_LOCAL = "perfiles_local.dat";
    private final Map<String, PerfilEntidad> datos = new ConcurrentHashMap<>();

    public PerfilDAOMemoria() {
        cargar();
    }

    @Override
    public void guardar(PerfilEntidad perfil) throws PersistenciaException {
        if (perfil == null || perfil.nombre() == null) {
            throw new PersistenciaException("El perfil o su nombre no pueden ser nulos.");
        }
        datos.put(perfil.nombre().toLowerCase(), perfil);
        guardarArchivo();
    }

    @Override
    public Optional<PerfilEntidad> buscar(String nombre) throws PersistenciaException {
        if (nombre == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(datos.get(nombre.toLowerCase()));
    }

    @Override
    public void registrarPartida(String nombre, boolean gano) throws PersistenciaException {
        if (nombre == null) return;
        PerfilEntidad actual = datos.get(nombre.toLowerCase());
        if (actual != null) {
            PerfilEntidad actualizado = new PerfilEntidad(
                    actual.nombre(),
                    actual.colorPreferido(),
                    actual.avatar(),
                    actual.partidasJugadas() + 1,
                    actual.partidasGanadas() + (gano ? 1 : 0)
            );
            datos.put(nombre.toLowerCase(), actualizado);
            guardarArchivo();
        }
    }

    @SuppressWarnings("unchecked")
    private void cargar() {
        File f = new File(ARCHIVO_LOCAL);
        if (f.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
                Map<String, PerfilEntidad> map = (Map<String, PerfilEntidad>) in.readObject();
                if (map != null) {
                    datos.putAll(map);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void guardarArchivo() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARCHIVO_LOCAL))) {
            out.writeObject(new java.util.HashMap<>(datos));
        } catch (Exception ignored) {
        }
    }
}

