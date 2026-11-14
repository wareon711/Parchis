package negocios;

import datos.ConexionBD;
import datos.IPerfilDAO;
import datos.PerfilDAOMemoria;
import datos.PerfilDAOMySQL;

public final class FabricaSubsistemas {

    private static boolean usarMocks = false;
    private static IPerfilDAO daoSingleton;

    private FabricaSubsistemas() {
    }

    public static boolean isUsarMocks() {
        return usarMocks;
    }

    public static void setUsarMocks(boolean usar) {
        usarMocks = usar;
    }

    public static IFachadaSala crearFachadaSala(int capacidadMax) {
        if (usarMocks) {
            return new FachadaSalaMock(capacidadMax);
        }
        return new FachadaSala(crearComunicacion(), crearPerfilBO(), capacidadMax);
    }

    public static IComunicacion crearComunicacion() {
        return new ComunicacionBroker();
    }

    public static PerfilBO crearPerfilBO() {
        return new PerfilBO(crearPerfilDAO());
    }

    public static synchronized IPerfilDAO crearPerfilDAO() {
        if (daoSingleton != null) {
            return daoSingleton;
        }
        if (mysqlDisponible()) {
            daoSingleton = new PerfilDAOMySQL();
        } else {
            daoSingleton = new PerfilDAOMemoria();
        }
        return daoSingleton;
    }

    private static boolean mysqlDisponible() {
        try {
            ConexionBD.obtener().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
