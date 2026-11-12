package com.sigev.servlet;

import com.sigev.dao.ProductoDAO;
import com.sigev.modelo.Producto;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet que gestiona el módulo de Productos vía web.
 * GET: consulta y muestra la lista de productos.
 * POST: procesa el formulario e inserta un nuevo producto.
 *
 * Evidencia GA7-220501096-AA2-EV02
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet(name = "ProductoServlet", urlPatterns = {"/ProductoServlet"})
public class ProductoServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    // Maneja las solicitudes GET: consulta y muestra el listado de productos
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Producto> productos = productoDAO.consultarProductos();
        request.setAttribute("productos", productos);

        // Reenvía los datos a la vista JSP para mostrarlos
        request.getRequestDispatcher("lista.jsp").forward(request, response);
    }

    // Maneja las solicitudes POST: recibe el formulario e inserta el producto
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Se leen los datos enviados desde el formulario HTML
        String nombre = request.getParameter("nombre");
        String categoria = request.getParameter("categoria");

        // Validacion en el servidor (las mismas reglas de la API): el "required" y
        // el "min" del formulario se pueden saltar desde el navegador
        boolean insertado = false;
        try {
            double precio = Double.parseDouble(request.getParameter("precio"));
            int stock = Integer.parseInt(request.getParameter("stock"));
            boolean valido = nombre != null && !nombre.isBlank() && nombre.length() <= 100
                    && (categoria == null || categoria.length() <= 50)
                    && precio > 0 && !Double.isInfinite(precio) && !Double.isNaN(precio)
                    && stock >= 0;
            if (valido) {
                Producto nuevoProducto = new Producto(nombre.trim(), categoria, precio, stock);
                insertado = productoDAO.insertarProducto(nuevoProducto);
            }
        } catch (NumberFormatException | NullPointerException e) {
            // Precio o stock vacios o que no son numeros: no se inserta
            insertado = false;
        }

        request.setAttribute("mensaje", insertado ? "Producto registrado correctamente." : "Error al registrar el producto.");

        // Después de insertar, vuelve a consultar la lista actualizada (reutiliza doGet)
        doGet(request, response);
    }
}