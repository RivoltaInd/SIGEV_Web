package com.sigev.api;

import com.google.gson.JsonObject;
import com.sigev.dao.UsuarioDAO;
import com.sigev.modelo.Usuario;
import com.sigev.util.Clave;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servicio de inicio de sesion.
 *   POST /api/login   {correo, clave}
 * Responde 200 con {mensaje, nombre, correo, rol} si la autenticacion es correcta,
 * o 401 con {error} si el correo o la contrasena no coinciden.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/login")
public class LoginApi extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = ApiUtil.leerJson(request);
        String correo = json == null ? null : ApiUtil.texto(json, "correo");
        String clave = json == null ? null : ApiUtil.texto(json, "clave");

        if (correo == null || clave == null) {
            ApiUtil.error(response, 400, "Ingrese correo y contraseña");
            return;
        }

        Usuario usuario = usuarioDAO.consultarPorCorreo(correo.toLowerCase());

        // El mismo mensaje para correo inexistente o clave errada: no se revela cual fallo
        if (usuario == null || !Clave.verificar(clave, usuario.getClaveHash())) {
            ApiUtil.error(response, 401, "Correo o contraseña incorrectos");
            return;
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Autenticación satisfactoria");
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("rol", usuario.getRol());
        ApiUtil.responder(response, 200, respuesta);
    }
}
