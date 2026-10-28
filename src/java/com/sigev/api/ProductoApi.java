package com.sigev.api;

import com.google.gson.JsonObject;
import com.sigev.dao.ProductoDAO;
import com.sigev.modelo.Producto;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servicio REST de Productos.
 *   GET    /api/productos        lista todos
 *   GET    /api/productos/{id}   consulta uno
 *   POST   /api/productos        crea   {nombre, categoria?, precio, stock}
 *   PUT    /api/productos/{id}   actualiza
 *   DELETE /api/productos/{id}   elimina
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/productos/*")
public class ProductoApi extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer codigo = ApiUtil.idDeRuta(request);
        if (codigo == null) {
            ApiUtil.responder(response, 200, productoDAO.consultarProductos());
            return;
        }
        Producto producto = productoDAO.consultarProductoPorCodigo(codigo);
        if (producto == null) {
            ApiUtil.error(response, 404, "El producto no existe");
            return;
        }
        ApiUtil.responder(response, 200, producto);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = ApiUtil.leerJson(request);
        String error = validar(json);
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        String categoria = ApiUtil.texto(json, "categoria");
        Producto producto = new Producto(
                ApiUtil.texto(json, "nombre"),
                categoria != null ? categoria : "General",
                ApiUtil.decimal(json, "precio"),
                ApiUtil.entero(json, "stock"));

        if (!productoDAO.insertarProducto(producto)) {
            ApiUtil.error(response, 500, "No se pudo guardar el producto");
            return;
        }
        ApiUtil.responder(response, 201, producto);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer codigo = ApiUtil.idDeRuta(request);
        if (codigo == null || codigo < 0) {
            ApiUtil.error(response, 400, "Falta el código del producto en la URL");
            return;
        }
        Producto existente = productoDAO.consultarProductoPorCodigo(codigo);
        if (existente == null) {
            ApiUtil.error(response, 404, "El producto no existe");
            return;
        }
        JsonObject json = ApiUtil.leerJson(request);
        String error = validar(json);
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        // Si no mandan categoria se conserva la que tenia
        String categoria = ApiUtil.texto(json, "categoria");
        existente.setNombre(ApiUtil.texto(json, "nombre"));
        existente.setCategoria(categoria != null ? categoria : existente.getCategoria());
        existente.setPrecio(ApiUtil.decimal(json, "precio"));
        existente.setStock(ApiUtil.entero(json, "stock"));

        if (!productoDAO.actualizarProducto(existente)) {
            ApiUtil.error(response, 500, "No se pudo actualizar el producto");
            return;
        }
        ApiUtil.responder(response, 200, existente);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer codigo = ApiUtil.idDeRuta(request);
        if (codigo == null || codigo < 0) {
            ApiUtil.error(response, 400, "Falta el código del producto en la URL");
            return;
        }
        if (productoDAO.consultarProductoPorCodigo(codigo) == null) {
            ApiUtil.error(response, 404, "El producto no existe");
            return;
        }
        // La base de datos no deja borrar un producto que ya tiene ventas
        if (!productoDAO.eliminarProducto(codigo)) {
            ApiUtil.error(response, 409, "No se puede eliminar: el producto tiene ventas registradas");
            return;
        }
        response.setStatus(204);
    }

    // Mismas reglas del formulario del front-end; devuelve el mensaje de error o null
    private String validar(JsonObject json) {
        if (json == null) {
            return "El cuerpo de la petición debe ser JSON";
        }
        if (ApiUtil.texto(json, "nombre") == null) {
            return "El nombre es obligatorio";
        }
        Double precio = ApiUtil.decimal(json, "precio");
        if (precio == null || precio <= 0) {
            return "El precio debe ser mayor a 0";
        }
        Integer stock = ApiUtil.entero(json, "stock");
        if (stock == null || stock < 0) {
            return "El stock debe ser un número entero mayor o igual a 0";
        }
        return null;
    }
}
