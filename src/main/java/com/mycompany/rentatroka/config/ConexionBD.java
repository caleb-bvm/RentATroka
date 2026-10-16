package com.mycompany.rentatroka.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Punto único de acceso a MySQL. Cada DAO cierra su conexión con try-with-resources. */
public final class ConexionBD {

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        Properties propiedades = new Properties();
        try (InputStream archivo = ConexionBD.class.getResourceAsStream("/db.properties")) {
            if (archivo != null) {
                propiedades.load(archivo);
            }
        } catch (IOException ex) {
            throw new SQLException("No se pudo leer la configuración de MySQL.", ex);
        }

        String url = obtenerValor("DB_URL", "db.url", propiedades);
        String usuario = obtenerValor("DB_USER", "db.user", propiedades);
        String clave = obtenerValor("DB_PASSWORD", "db.password", propiedades);
        if (url == null || usuario == null || clave == null) {
            throw new SQLException("Configura DB_URL, DB_USER y DB_PASSWORD o el archivo db.properties.");
        }
        try {
            // Necesario también al cargar el controlador desde WEB-INF/lib en Tomcat.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontró el controlador JDBC de MySQL.", ex);
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String obtenerValor(String variable, String propiedad, Properties propiedades) {
        String valor = System.getenv(variable);
        return valor != null ? valor : propiedades.getProperty(propiedad);
    }
}

