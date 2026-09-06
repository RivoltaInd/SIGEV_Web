package com.sigev.modelo;

/**
 * Representa una linea de una venta: un producto, la cantidad vendida
 * y el precio unitario aplicado (se guarda el precio al momento de la venta,
 * para que si el precio del producto cambia despues, el historial no se altere).
 */
public class DetalleVenta {

    private int idProducto;
    private String nombreProducto; // solo para mostrar en pantalla, no se persiste directamente
    private int cantidad;
    private double precioUnitario;

    public DetalleVenta() {
    }

    public DetalleVenta(int idProducto, int cantidad, double precioUnitario) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
