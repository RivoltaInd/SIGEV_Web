package com.sigev.api;

import com.google.gson.JsonObject;
import com.sigev.dao.ClienteDAO;
import com.sigev.dao.ProductoDAO;
import com.sigev.dao.VentaDAO;
import com.sigev.modelo.DetalleVenta;
import com.sigev.modelo.Producto;
import com.sigev.modelo.Venta;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio REST de Ventas.
 *   GET  /api/ventas   historial, de la mas antigua a la mas reciente
 *   POST /api/ventas   registra {idCliente, codigoProducto, cantidad}
 *
 * El precio se toma de la base de datos (no del JSON) y VentaDAO descuenta el
 * stock en la misma transaccion, asi no se puede vender mas de lo que hay.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/ventas/*")
public class VentaApi extends HttpServlet {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Venta> ventas = ventaDAO.consultarVentas();
        ventas.sort(Comparator.comparingInt(Venta::getId));

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Venta v : ventas) {
            // En la base de datos cada fila de venta es un solo producto
            DetalleVenta d = v.getDetalles().get(0);
            String fecha = v.getFecha() != null ? v.getFecha().toLocalDateTime().toLocalDate().toString() : null;
            lista.add(aJson(v.getId(), v.getIdCliente(), d.getIdProducto(), d.getCantidad(), fecha, v.getTotal()));
        }
        ApiUtil.responder(response, 200, lista);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = ApiUtil.leerJson(request);
        if (json == null) {
            ApiUtil.error(response, 400, "El cuerpo de la petición debe ser JSON");
            return;
        }
        Integer idCliente = ApiUtil.entero(json, "idCliente");
        Integer codigoProducto = ApiUtil.entero(json, "codigoProducto");
        Integer cantidad = ApiUtil.entero(json, "cantidad");

        if (idCliente == null || clienteDAO.consultarClientePorId(idCliente) == null) {
            ApiUtil.error(response, 400, "Debe seleccionar un cliente válido");
            return;
        }
        Producto producto = codigoProducto == null ? null : productoDAO.consultarProductoPorCodigo(codigoProducto);
        if (producto == null) {
            ApiUtil.error(response, 400, "El producto no existe");
            return;
        }
        if (cantidad == null || cantidad <= 0) {
            ApiUtil.error(response, 400, "La cantidad debe ser un entero mayor a 0");
            return;
        }
        if (cantidad > producto.getStock()) {
            ApiUtil.error(response, 409, "Stock insuficiente. Disponible: " + producto.getStock());
            return;
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(new DetalleVenta(codigoProducto, cantidad, producto.getPrecio()));
        Venta venta = new Venta(idCliente, detalles);

        // insertarVenta devuelve false si otra venta se llevo el stock al mismo tiempo
        if (!ventaDAO.insertarVenta(venta)) {
            ApiUtil.error(response, 409, "No se pudo registrar la venta: stock insuficiente");
            return;
        }
        ApiUtil.responder(response, 201, aJson(venta.getId(), idCliente, codigoProducto, cantidad,
                LocalDate.now().toString(), venta.getTotal()));
    }

    // Venta -> JSON con los nombres de campo del front-end
    private Map<String, Object> aJson(int idVenta, int idCliente, int codigoProducto, int cantidad,
                                      String fecha, double total) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("idVenta", idVenta);
        json.put("idCliente", idCliente);
        json.put("codigoProducto", codigoProducto);
        json.put("cantidad", cantidad);
        json.put("fecha", fecha);
        json.put("total", total);
        return json;
    }
}
