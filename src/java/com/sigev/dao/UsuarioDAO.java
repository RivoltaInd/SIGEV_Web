package com.sigev.dao;

import com.sigev.modelo.Usuario;
import com.sigev.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla usuario (registro, inicio de sesion,
 * cambio de contrasena y activacion/desactivacion, AA5).
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

    // Lista de usuarios SIN el hash de la contrasena (no se consulta esa columna)
    public List<Usuario> consultarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, correo, rol, activo FROM usuario ORDER BY nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario(rs.getString("nombre"), rs.getString("correo"), null, rs.getString("rol"));
                u.setId(rs.getInt("id"));
                u.setActivo(rs.getBoolean("activo"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar los usuarios: " + e.getMessage());
        }
        return lista;
    }

    // Devuelve el usuario con ese correo (con su hash, para verificar la clave), o null
    public Usuario consultarPorCorreo(String correo) {
        return consultarUno("SELECT id, nombre, correo, clave_hash, rol, activo FROM usuario WHERE correo = ?", correo);
    }

    // Devuelve el usuario con ese id (con su hash), o null
    public Usuario consultarPorId(int id) {
        return consultarUno("SELECT id, nombre, correo, clave_hash, rol, activo FROM usuario WHERE id = ?", id);
    }

    public boolean actualizarClave(int id, String claveHash) {
        return actualizar("UPDATE usuario SET clave_hash = ? WHERE id = ?", claveHash, id);
    }

    public boolean cambiarActivo(int id, boolean activo) {
        return actualizar("UPDATE usuario SET activo = ? WHERE id = ?", activo, id);
    }

    // Cuantos Admin activos hay (para no dejar el sistema sin ningun Admin)
    public int contarAdminsActivos() {
        String sql = "SELECT COUNT(*) FROM usuario WHERE rol = 'Admin' AND activo = 1";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.out.println("Error al contar administradores: " + e.getMessage());
            return 0;
        }
    }

    private Usuario consultarUno(String sql, Object valor) {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getString("nombre"),
                            rs.getString("correo"),
                            rs.getString("clave_hash"),
                            rs.getString("rol"));
                    u.setId(rs.getInt("id"));
                    u.setActivo(rs.getBoolean("activo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar el usuario: " + e.getMessage());
        }
        return null;
    }

    private boolean actualizar(String sql, Object valor, int id) {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, valor);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar el usuario: " + e.getMessage());
            return false;
        }
    }
}
