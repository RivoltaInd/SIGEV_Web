package com.sigev.servlet;

import com.sigev.dao.ClienteDAO;
import com.sigev.dao.ProductoDAO;
import com.sigev.dao.VentaDAO;
import com.sigev.modelo.Cliente;
import com.sigev.modelo.DetalleVenta;
import com.sigev.modelo.Producto;
import com.sigev.modelo.Venta;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el modulo de Ventas: una venta puede incluir varios productos
 * y esta asociada a un cliente. GET carga clientes, productos e historial
 * y hace forward a registrar_venta.jsp. POST procesa el formulario,
 * arma los detalles de venta y delega en VentaDAO (que maneja la
 * transaccion y el descuento de stock).
 *
 * Evidencia GA7-220501096-AA3-EV01
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/VentaServlet")
public class VentaServlet extends HttpServlet {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Producto> listaProductos = productoDAO.consultarProductos();
        List<Cliente> listaClientes = clienteDAO.consultarClientes();
        List<Venta> listaVentas = ventaDAO.consultarVentas();

        request.setAttribute("listaProductos", listaProductos);
        request.setAttribute("listaClientes", listaClientes);
        request.setAttribute("listaVentas", listaVentas);

        request.getRequestDispatcher("registrar_venta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int idCliente = Integer.parseInt(request.getParameter("idCliente"));

        // Los tres arreglos llegan en el mismo orden (una posicion por fila de la tabla)
        String[] idsProducto = request.getParameterValues("idProducto");
        String[] precios = request.getParameterValues("precioUnitario");
        String[] cantidades = request.getParameterValues("cantidad");

        List<DetalleVenta> detalles = new ArrayList<>();

        if (idsProducto != null) {
            for (int i = 0; i < idsProducto.length; i++) {
                int cantidad = Integer.parseInt(cantidades[i]);

                // Solo se incluyen los productos donde el usuario puso cantidad > 0
                if (cantidad > 0) {
                    int idProducto = Integer.parseInt(idsProducto[i]);
                    double precioUnitario = Double.parseDouble(precios[i]);
                    detalles.add(new DetalleVenta(idProducto, cantidad, precioUnitario));
                }
            }
        }

        boolean ventaRegistrada = false;

        if (!detalles.isEmpty()) {
            Venta venta = new Venta(idCliente, detalles);
            ventaRegistrada = ventaDAO.insertarVenta(venta);
        }

        request.setAttribute("ventaRegistrada", ventaRegistrada);

        // Reutiliza doGet para recargar clientes, productos (con stock actualizado) e historial
        doGet(request, response);
    }
}