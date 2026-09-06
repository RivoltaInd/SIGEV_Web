package com.sigev.dao;

import com.sigev.modelo.DetalleVenta;
import com.sigev.modelo.Venta;
import com.sigev.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptado a la tabla "venta" YA EXISTENTE en sigev_db:
 * venta(id_venta, codigo_prod, cantidad, fecha, total, id_cliente)
 * Cada producto elegido en el formulario genera UNA fila (no hay tabla
 * detalle_venta separada: aqui "venta" ya funciona como linea de venta).
 * Todas las lineas de una misma compra se insertan en una sola transaccion.
 */
public class VentaDAO {

    public boolean insertarVenta(Venta venta) {
        String sqlInsert = "INSERT INTO venta (id_cliente, codigo_prod, cantidad, fecha, total) VALUES (?, ?, ?, NOW(), ?)";
        String sqlStock = "UPDATE producto SET stock = stock - ? WHERE codigo = ? AND stock >= ?";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                for (DetalleVenta d : venta.getDetalles()) {
                    double total = d.getSubtotal();

                    try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                        psInsert.setInt(1, venta.getIdCliente());
                        psInsert.setInt(2, d.getIdProducto());
                        psInsert.setInt(3, d.getCantidad());
                        psInsert.setDouble(4, total);
                        psInsert.executeUpdate();
                    }

                    try (PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                        psStock.setInt(1, d.getCantidad());
                        psStock.setInt(2, d.getIdProducto());
                        psStock.setInt(3, d.getCantidad());
                        int filas = psStock.executeUpdate();
                        if (filas == 0) {
                            throw new SQLException("Stock insuficiente para el producto " + d.getIdProducto());
                        }
                    }
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Historial: cada fila es una linea de venta (un producto), con el cliente y el nombre de producto. */
    public List<Venta> consultarVentas() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.id_venta, v.fecha, v.id_cliente, c.nombre AS nombre_cliente, "
                + "v.codigo_prod, p.nombre AS nombre_producto, v.cantidad, v.total "
                + "FROM venta v "
                + "LEFT JOIN cliente c ON v.id_cliente = c.id "
                + "LEFT JOIN producto p ON v.codigo_prod = p.codigo "
                + "ORDER BY v.fecha DESC";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getInt("id_venta"));
                v.setFecha(rs.getTimestamp("fecha"));
                v.setIdCliente(rs.getInt("id_cliente"));
                v.setNombreCliente(rs.getString("nombre_cliente"));

                DetalleVenta d = new DetalleVenta(rs.getInt("codigo_prod"), rs.getInt("cantidad"), 0);
                d.setNombreProducto(rs.getString("nombre_producto"));
                double total = rs.getDouble("total");
                v.agregarDetalle(d);
                v.setTotalGuardado(total);

                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
