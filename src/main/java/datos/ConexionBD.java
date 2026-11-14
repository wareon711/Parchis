package datos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

    private static final Properties props = new Properties();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Throwable ignored) {
        }

        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream("conexion.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
        }

        File externo = new File("conexion.properties");
        if (externo.exists()) {
            try (FileInputStream fin = new FileInputStream(externo)) {
                props.load(fin);
            } catch (Exception ignored) {
            }
        }
    }

    private ConexionBD() {
    }

    public static Connection obtener() throws SQLException {
        String host = props.getProperty("db.host", "localhost").trim();
        String port = props.getProperty("db.port", "3306").trim();
        String db = props.getProperty("db.database", "parchis").trim();
        String user = props.getProperty("db.user", "root").trim();
        String pass = props.getProperty("db.password", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?createDatabaseIfNotExist=true&sslMode=PREFERRED&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=2500&socketTimeout=2500";
        return DriverManager.getConnection(url, user, pass);
    }
}
