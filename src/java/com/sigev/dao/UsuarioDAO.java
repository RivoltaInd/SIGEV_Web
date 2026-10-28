package com.sigev.dao;

import com.sigev.modelo.Usuario;
import com.sigev.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acceso a datos para la tabla usuario (registro e inicio de sesion, AA5).
 */
public class UsuarioDAO {

    // Inserta el usuario y le asigna el id generado. Falla si el correo ya existe (UNIQUE).
    public boolean insertarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, correo, clave_hash, rol) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setString(3, usuario.getClaveHash());
            ps.setString(4, usuario.getRol());
            int filas = ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setId(claves.getInt(1));
                }
            }
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al insertar el usuario: " + e.getMessage());
            return false;
        }
    }

    // Devuelve el usuario con ese correo, o null si no existe
    public Usuario consultarPorCorreo(String correo) {
        String sql = "SELECT id, nombre, correo, clave_hash, rol FROM usuario WHERE correo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getString("nombre"),
                            rs.getString("correo"),
                            rs.getString("clave_hash"),
                            rs.getString("rol"));
                    u.setId(rs.getInt("id"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar el usuario: " + e.getMessage());
        }
        return null;
    }
}
