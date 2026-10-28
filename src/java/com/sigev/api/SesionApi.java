package com.sigev.api;

import com.sigev.modelo.Usuario;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sesion actual y cierre de sesion.
 *   GET  /api/sesion   usuario conectado (si no hay sesion, SeguridadFilter responde 401)
 *   POST /api/logout   cierra la sesion en el servidor
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet({"/api/sesion", "/api/logout"})
public class SesionApi extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute(ApiUtil.USUARIO_SESION);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("rol", usuario.getRol());
        ApiUtil.responder(response, 200, respuesta);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        response.setStatus(204);
    }
}
