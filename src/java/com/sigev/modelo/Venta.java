package com.sigev.modelo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una venta: un cliente, una fecha y una o mas lineas de detalle
 * (producto + cantidad + precio unitario).
 */
public class Venta {

    private int id;
    private Timestamp fecha;
    private int idCliente;
    private String nombreCliente; // solo para mostrar en pantalla
    private List<DetalleVenta> detalles = new ArrayList<>();
    private Double totalGuardado; // total tal como quedo guardado en la fila de venta (columna "total")

    public Venta() {
    }

    public Venta(int idCliente, List<DetalleVenta> detalles) {
        this.idCliente = idCliente;
        this.detalles = detalles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetalleVenta detalle) {
        this.detalles.add(detalle);
    }

    public double getTotal() {
        if (totalGuardado != null) {
            return totalGuardado;
        }
        double total = 0;
        for (DetalleVenta d : detalles) {
            total += d.getSubtotal();
        }
        return total;
    }

    public void setTotalGuardado(double totalGuardado) {
        this.totalGuardado = totalGuardado;
    }
}
