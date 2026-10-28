package com.sigev.dao;

import com.sigev.modelo.Producto;
import com.sigev.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Inserta el producto y le asigna el código AUTO_INCREMENT generado por MySQL
    public boolean insertarProducto(Producto producto) {
        String sql = "INSERT INTO producto (nombre, categoria, precio, stock) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, producto.getNombre());
            sentencia.setString(2, producto.getCategoria());
            sentencia.setDouble(3, producto.getPrecio());
            sentencia.setInt(4, producto.getStock());

            int filasAfectadas = sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setCodigo(claves.getInt(1));
                }
            }
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("Error al insertar el producto: " + e.getMessage());
            return false;
        }
    }

    public List<Producto> consultarProductos() {
        List<Producto> listaProductos = new ArrayList<>();
        String sql = "SELECT codigo, nombre, categoria, precio, stock FROM producto";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                listaProductos.add(leerProducto(resultado));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al consultar los productos: " + e.getMessage(), e);
        }

        return listaProductos;
    }

    // Devuelve el producto con ese código, o null si no existe
    public Producto consultarProductoPorCodigo(int codigo) {
        String sql = "SELECT codigo, nombre, categoria, precio, stock FROM producto WHERE codigo = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, codigo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return leerProducto(resultado);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar el producto: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizarProducto(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, categoria = ?, precio = ?, stock = ? WHERE codigo = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, producto.getNombre());
            sentencia.setString(2, producto.getCategoria());
            sentencia.setDouble(3, producto.getPrecio());
            sentencia.setInt(4, producto.getStock());
            sentencia.setInt(5, producto.getCodigo());
            return sentencia.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar el producto: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarProducto(int codigo) {
        String sql = "DELETE FROM producto WHERE codigo = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, codigo);
            int filasAfectadas = sentencia.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar el producto: " + e.getMessage());
            return false;
        }
    }

    private Producto leerProducto(ResultSet resultado) throws SQLException {
        return new Producto(
                resultado.getInt("codigo"),
                resultado.getString("nombre"),
                resultado.getString("categoria"),
                resultado.getDouble("precio"),
                resultado.getInt("stock"));
    }
}
