package com.sigev.api;

import com.google.gson.JsonObject;
import com.sigev.dao.UsuarioDAO;
import com.sigev.modelo.Usuario;
import com.sigev.util.Clave;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * Cambio de contrasena del usuario conectado (cualquier rol).
 *   POST /api/cambiar-clave   {claveActual, claveNueva}
 * Solo cambia la contrasena de quien tiene la sesion: el id sale de la sesion,
 * nunca del JSON, asi nadie puede cambiar la clave de otro.
 * Exige la clave actual, para que alguien que encuentre la sesion abierta
 * no pueda cambiarla. Tras 5 claves actuales incorrectas se cierra la sesion (401).
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/cambiar-clave")
public class CambiarClaveApi extends HttpServlet {

    private static final int MAX_FALLOS = 5;
    private static final String FALLOS_SESION = "fallosCambioClave";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = ApiUtil.leerJson(request);
        String actual = json == null ? null : ApiUtil.texto(json, "claveActual");
        String nueva = json == null ? null : ApiUtil.texto(json, "claveNueva");

        if (actual == null || nueva == null) {
            ApiUtil.error(response, 400, "Ingrese la contraseña actual y la nueva");
            return;
        }

        // SeguridadFilter ya verifico la sesion; se carga el usuario con su hash
        Usuario enSesion = (Usuario) request.getSession().getAttribute(ApiUtil.USUARIO_SESION);
        Usuario usuario = usuarioDAO.consultarPorId(enSesion.getId());

        HttpSession sesion = request.getSession();
        if (!Clave.verificar(actual, usuario.getClaveHash())) {
            // Contra adivinar la clave desde una sesion abierta: al 5o fallo se cierra
            // la sesion y hay que volver a entrar por el login (que tiene su propio bloqueo)
            Integer previos = (Integer) sesion.getAttribute(FALLOS_SESION);
            int fallos = (previos == null ? 0 : previos) + 1;
            if (fallos >= MAX_FALLOS) {
                sesion.invalidate();
                ApiUtil.error(response, 401, "Demasiados intentos fallidos. Inicie sesión de nuevo");
                return;
            }
            sesion.setAttribute(FALLOS_SESION, fallos);
            ApiUtil.error(response, 400, "La contraseña actual no es correcta");
            return;
        }
        sesion.removeAttribute(FALLOS_SESION);
        String error = validarNueva(nueva, actual, usuario.getCorreo());
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        if (!usuarioDAO.actualizarClave(usuario.getId(), Clave.cifrar(nueva))) {
            ApiUtil.error(response, 500, "No se pudo cambiar la contraseña");
            return;
        }
        // Id de sesion nuevo despues de un cambio de credenciales
        request.changeSessionId();
        ApiUtil.responder(response, 200, Map.of("mensaje", "Contraseña actualizada"));
    }

    // Reglas para una contrasena nueva; devuelve el mensaje de error o null
    static String validarNueva(String nueva, String actual, String correo) {
        if (nueva.length() < 10 || nueva.length() > 100) {
            return "La contraseña nueva debe tener entre 10 y 100 caracteres";
        }
        if (!nueva.matches(".*[A-Za-z].*") || !nueva.matches(".*\\d.*")) {
            return "La contraseña nueva debe tener letras y números";
        }
        if (nueva.equals(actual)) {
            return "La contraseña nueva debe ser diferente a la actual";
        }
        if (nueva.equalsIgnoreCase(correo) || nueva.toLowerCase().contains("sigev")) {
            return "La contraseña nueva es muy fácil de adivinar";
        }
        return null;
    }
}
