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
        double precio = Double.parseDouble(request.getParameter("precio"));
        int stock = Integer.parseInt(request.getParameter("stock"));

        Producto nuevoProducto = new Producto(nombre, categoria, precio, stock);
        boolean insertado = productoDAO.insertarProducto(nuevoProducto);

        request.setAttribute("mensaje", insertado ? "Producto registrado correctamente." : "Error al registrar el producto.");

        // Después de insertar, vuelve a consultar la lista actualizada (reutiliza doGet)
        doGet(request, response);
    }
}