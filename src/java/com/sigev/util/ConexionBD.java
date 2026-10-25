package com.sigev.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Las credenciales se leen de db.properties (en la raiz del classpath),
 * que no se sube a git. Copiar db.properties.example como db.properties
 * y poner los datos de la instalacion local de MySQL.
 */
public class ConexionBD {

    private static final String URL;
    private static final String USUARIO;
    private static final String CONTRASENA;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver JDBC de MySQL", e);
        }

        Properties config = new Properties();
        try (InputStream entrada = ConexionBD.class.getResourceAsStream("/db.properties")) {
            if (entrada == null) {
                throw new RuntimeException("No se encontró db.properties. Cree el archivo a partir de db.properties.example");
            }
            config.load(entrada);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer db.properties", e);
        }
        URL = config.getProperty("db.url");
        USUARIO = config.getProperty("db.usuario");
        CONTRASENA = config.getProperty("db.contrasena");
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
