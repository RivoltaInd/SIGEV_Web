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

        // Los dos arreglos llegan en el mismo orden (una posicion por fila de la tabla)
        String[] idsProducto = request.getParameterValues("idProducto");
        String[] cantidades = request.getParameterValues("cantidad");

        boolean ventaRegistrada = false;

        // Todo lo que llega del navegador se valida aqui: el usuario puede cambiar
        // cualquier campo del formulario (incluso los ocultos) antes de enviarlo.
        try {
            int idCliente = Integer.parseInt(request.getParameter("idCliente"));
            List<DetalleVenta> detalles = new ArrayList<>();
            boolean datosValidos = clienteDAO.consultarClientePorId(idCliente) != null
                    && idsProducto != null && cantidades != null
                    && idsProducto.length == cantidades.length;

            for (int i = 0; datosValidos && i < idsProducto.length; i++) {
                int cantidad = Integer.parseInt(cantidades[i]);

                // Solo se incluyen los productos donde el usuario puso cantidad > 0
                if (cantidad > 0) {
                    // SEGURIDAD: el precio se toma de la base de datos, nunca del
                    // formulario. Antes venia en un campo oculto y cualquiera podia
                    // cambiarlo para vender a un precio inventado.
                    Producto producto = productoDAO.consultarProductoPorCodigo(Integer.parseInt(idsProducto[i]));
                    if (producto == null) {
                        datosValidos = false;
                    } else {
                        detalles.add(new DetalleVenta(producto.getCodigo(), cantidad, producto.getPrecio()));
                    }
                } else if (cantidad < 0) {
                    datosValidos = false;
                }
            }

            if (datosValidos && !detalles.isEmpty()) {
                Venta venta = new Venta(idCliente, detalles);
                ventaRegistrada = ventaDAO.insertarVenta(venta);
            }
        } catch (NumberFormatException e) {
            // Un dato que no es numero: no se registra nada
            ventaRegistrada = false;
        }

        request.setAttribute("ventaRegistrada", ventaRegistrada);

        // Reutiliza doGet para recargar clientes, productos (con stock actualizado) e historial
        doGet(request, response);
    }
}