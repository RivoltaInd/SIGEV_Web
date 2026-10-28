package com.sigev.api;

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
 * Lista de usuarios del sistema (solo Admin, ver Permisos).
 *   GET /api/usuarios
 * Nunca devuelve la contrasena ni su hash. Para crear usuarios se usa /api/registro.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/usuarios")
public class UsuarioApi extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Usuario u : usuarioDAO.consultarUsuarios()) {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", u.getId());
            json.put("nombre", u.getNombre());
            json.put("correo", u.getCorreo());
            json.put("rol", u.getRol());
            lista.add(json);
        }
        ApiUtil.responder(response, 200, lista);
    }
}
