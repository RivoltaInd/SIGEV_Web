package com.sigev.api;

import com.google.gson.JsonObject;
import com.sigev.dao.UsuarioDAO;
import com.sigev.modelo.Usuario;

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
 * Usuarios del sistema (solo Admin, ver Permisos).
 *   GET /api/usuarios        lista (nunca devuelve la contrasena ni su hash)
 *   PUT /api/usuarios/{id}   activa o desactiva   {activo: true|false}
 * Para crear usuarios se usa /api/registro.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/usuarios/*")
public class UsuarioApi extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Usuario u : usuarioDAO.consultarUsuarios()) {
            lista.add(aJson(u));
        }
        ApiUtil.responder(response, 200, lista);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = ApiUtil.idDeRuta(request);
        if (id == null || id < 0) {
            ApiUtil.error(response, 400, "Falta el id del usuario en la URL");
            return;
        }
        JsonObject json = ApiUtil.leerJson(request);
        String valor = json == null ? null : ApiUtil.texto(json, "activo");
        if (!"true".equals(valor) && !"false".equals(valor)) {
            ApiUtil.error(response, 400, "Indique activo: true o false");
            return;
        }
        boolean activo = Boolean.parseBoolean(valor);

        Usuario objetivo = usuarioDAO.consultarPorId(id);
        if (objetivo == null) {
            ApiUtil.error(response, 404, "El usuario no existe");
            return;
        }
        Usuario yo = (Usuario) request.getSession().getAttribute(ApiUtil.USUARIO_SESION);
        if (!activo && objetivo.getId() == yo.getId()) {
            ApiUtil.error(response, 409, "No puedes desactivar tu propia cuenta");
            return;
        }
        // Nunca dejar el sistema sin un Admin activo
        if (!activo && Permisos.ADMIN.equals(objetivo.getRol()) && objetivo.isActivo()
                && usuarioDAO.contarAdminsActivos() <= 1) {
            ApiUtil.error(response, 409, "No se puede desactivar el último Administrador activo");
            return;
        }
        if (!usuarioDAO.cambiarActivo(id, activo)) {
            ApiUtil.error(response, 500, "No se pudo actualizar el usuario");
            return;
        }
        objetivo.setActivo(activo);
        ApiUtil.responder(response, 200, aJson(objetivo));
    }

    private Map<String, Object> aJson(Usuario u) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", u.getId());
        json.put("nombre", u.getNombre());
        json.put("correo", u.getCorreo());
        json.put("rol", u.getRol());
        json.put("activo", u.isActivo());
        return json;
    }
}
