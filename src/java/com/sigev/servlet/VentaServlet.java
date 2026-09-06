package com.sigev.servlet;

import com.sigev.dao.ClienteDAO;
import com.sigev.dao.ProductoDAO;
import com.sigev.dao.VentaDAO;
import com.sigev.modelo.DetalleVenta;
import com.sigev.modelo.Venta;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * doGet: carga clientes, productos y el historial de ventas para el formulario/listado.
 * doPost: arma una Venta con su lista de DetalleVenta y la inserta en una sola transaccion.
 *
 * El formulario (registrar_venta.jsp) envia listas paralelas idProducto[] y cantidad[]
 * (una fila por producto agregado a la venta) mas idCliente.
 *
 * NOTA: asume que ya existe com.sigev.dao.ProductoDAO de AA2-EV02 con un metodo
 * consultarProductos(); si el nombre del metodo es distinto, ajustalo aqui.
 */
@WebServlet("/VentaServlet")
public class VentaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("listaClientes", new ClienteDAO().consultarClientes());
        request.setAttribute("listaProductos", new ProductoDAO().consultarProductos());
        request.setAttribute("listaVentas", new VentaDAO().consultarVentas());
        request.getRequestDispatcher("registrar_venta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idCliente = Integer.parseInt(request.getParameter("idCliente"));
        String[] idsProducto = request.getParameterValues("idProducto");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precioUnitario");

        Venta venta = new Venta();
        venta.setIdCliente(idCliente);

        if (idsProducto != null) {
            for (int i = 0; i < idsProducto.length; i++) {
                int idProducto = Integer.parseInt(idsProducto[i]);
                int cantidad = Integer.parseInt(cantidades[i]);
                double precio = Double.parseDouble(precios[i]);
                if (cantidad > 0) {
                    venta.agregarDetalle(new DetalleVenta(idProducto, cantidad, precio));
                }
            }
        }

        boolean ok = new VentaDAO().insertarVenta(venta);
        request.setAttribute("ventaRegistrada", ok);

        doGet(request, response);
    }
}
