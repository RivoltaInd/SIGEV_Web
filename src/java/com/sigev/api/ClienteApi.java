package com.sigev.api;

import com.google.gson.JsonObject;
import com.sigev.dao.ClienteDAO;
import com.sigev.modelo.Cliente;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio REST de Clientes.
 *   GET    /api/clientes        lista todos
 *   GET    /api/clientes/{id}   consulta uno
 *   POST   /api/clientes        crea   {documento, nombre, telefono?, correo?}
 *   PUT    /api/clientes/{id}   actualiza
 *   DELETE /api/clientes/{id}   elimina
 *
 * En la base de datos la columna se llama "email"; en el JSON se usa "correo",
 * que es el nombre que maneja el front-end.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/clientes/*")
public class ClienteApi extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = ApiUtil.idDeRuta(request);
        if (id == null) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Cliente c : clienteDAO.consultarClientes()) {
                lista.add(aJson(c));
            }
            ApiUtil.responder(response, 200, lista);
            return;
        }
        Cliente cliente = clienteDAO.consultarClientePorId(id);
        if (cliente == null) {
            ApiUtil.error(response, 404, "El cliente no existe");
            return;
        }
        ApiUtil.responder(response, 200, aJson(cliente));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = ApiUtil.leerJson(request);
        String error = validar(json, 0);
        if (error != null) {
            ApiUtil.error(response, error.startsWith("Ya existe") ? 409 : 400, error);
            return;
        }
        Cliente cliente = new Cliente(
                ApiUtil.texto(json, "nombre"),
                ApiUtil.texto(json, "documento"),
                ApiUtil.texto(json, "telefono"),
                ApiUtil.texto(json, "correo"));

        if (!clienteDAO.insertarCliente(cliente)) {
            ApiUtil.error(response, 500, "No se pudo guardar el cliente");
            return;
        }
        ApiUtil.responder(response, 201, aJson(cliente));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = ApiUtil.idDeRuta(request);
        if (id == null || id < 0) {
            ApiUtil.error(response, 400, "Falta el id del cliente en la URL");
            return;
        }
        if (clienteDAO.consultarClientePorId(id) == null) {
            ApiUtil.error(response, 404, "El cliente no existe");
            return;
        }
        JsonObject json = ApiUtil.leerJson(request);
        String error = validar(json, id);
        if (error != null) {
            ApiUtil.error(response, error.startsWith("Ya existe") ? 409 : 400, error);
            return;
        }
        Cliente cliente = new Cliente(id,
                ApiUtil.texto(json, "nombre"),
                ApiUtil.texto(json, "documento"),
                ApiUtil.texto(json, "telefono"),
                ApiUtil.texto(json, "correo"));

        if (!clienteDAO.actualizarCliente(cliente)) {
            ApiUtil.error(response, 500, "No se pudo actualizar el cliente");
            return;
        }
        ApiUtil.responder(response, 200, aJson(cliente));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = ApiUtil.idDeRuta(request);
        if (id == null || id < 0) {
            ApiUtil.error(response, 400, "Falta el id del cliente en la URL");
            return;
        }
        if (clienteDAO.consultarClientePorId(id) == null) {
            ApiUtil.error(response, 404, "El cliente no existe");
            return;
        }
        // La base de datos no deja borrar un cliente que ya tiene ventas
        if (!clienteDAO.eliminarCliente(id)) {
            ApiUtil.error(response, 409, "No se puede eliminar: el cliente tiene ventas registradas");
            return;
        }
        response.setStatus(204);
    }

    // Cliente -> JSON con los nombres de campo del front-end
    private Map<String, Object> aJson(Cliente c) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", c.getId());
        json.put("documento", c.getDocumento());
        json.put("nombre", c.getNombre());
        json.put("telefono", c.getTelefono());
        json.put("correo", c.getEmail());
        return json;
    }

    /**
     * Mismas reglas del formulario del front-end, mas que el documento no se repita.
     * idActual es el cliente que se edita (0 si es nuevo), para no compararlo consigo mismo.
     */
    private String validar(JsonObject json, int idActual) {
        if (json == null) {
            return "El cuerpo de la petición debe ser JSON";
        }
        String documento = ApiUtil.texto(json, "documento");
        if (documento == null || !documento.matches("\\d{6,10}")) {
            return "El documento debe tener entre 6 y 10 números";
        }
        String nombre = ApiUtil.texto(json, "nombre");
        if (nombre == null || nombre.length() < 3 || !nombre.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")) {
            return "El nombre debe tener al menos 3 letras y solo letras";
        }
        String telefono = ApiUtil.texto(json, "telefono");
        if (telefono != null && !telefono.matches("\\d{7,10}")) {
            return "El teléfono debe tener entre 7 y 10 números";
        }
        String correo = ApiUtil.texto(json, "correo");
        if (correo != null && !ApiUtil.correoValido(correo)) {
            return "El correo no es válido";
        }
        for (Cliente c : clienteDAO.consultarClientes()) {
            if (documento.equals(c.getDocumento()) && c.getId() != idActual) {
                return "Ya existe un cliente con ese documento";
            }
        }
        return null;
    }
}
