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
 * Servicio de registro de usuarios. Solo un Admin con sesion iniciada puede
 * usarlo (lo controla SeguridadFilter con las reglas de Permisos).
 *   POST /api/registro   {nombre, correo, clave, rol?}
 * rol puede ser "Admin" o "Vendedor" (por defecto Vendedor).
 * La contrasena se guarda cifrada con PBKDF2 (ver util.Clave).
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/registro")
public class RegistroApi extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = ApiUtil.leerJson(request);
        if (json == null) {
            ApiUtil.error(response, 400, "El cuerpo de la petición debe ser JSON");
            return;
        }
        String nombre = ApiUtil.texto(json, "nombre");
        String correo = ApiUtil.texto(json, "correo");
        String clave = ApiUtil.texto(json, "clave");
        String rol = ApiUtil.texto(json, "rol");

        if (nombre == null || nombre.length() < 3 || nombre.length() > 100
                || !nombre.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")) {
            ApiUtil.error(response, 400, "El nombre debe tener entre 3 y 100 letras");
            return;
        }
        if (!ApiUtil.correoValido(correo)) {
            ApiUtil.error(response, 400, "El correo no es válido");
            return;
        }
        if (clave == null || clave.length() < 8) {
            ApiUtil.error(response, 400, "La contraseña debe tener al menos 8 caracteres");
            return;
        }
        if (rol == null) {
            rol = "Vendedor";
        } else if (!rol.equals("Admin") && !rol.equals("Vendedor")) {
            ApiUtil.error(response, 400, "El rol debe ser Admin o Vendedor");
            return;
        }

        correo = correo.toLowerCase();
        if (usuarioDAO.consultarPorCorreo(correo) != null) {
            ApiUtil.error(response, 409, "Ya existe un usuario con ese correo");
            return;
        }

        Usuario usuario = new Usuario(nombre, correo, Clave.cifrar(clave), rol);
        if (!usuarioDAO.insertarUsuario(usuario)) {
            ApiUtil.error(response, 500, "No se pudo registrar el usuario");
            return;
        }

        // Nunca se devuelve la contrasena ni su hash
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Usuario registrado correctamente");
        respuesta.put("id", usuario.getId());
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("rol", usuario.getRol());
        ApiUtil.responder(response, 201, respuesta);
    }
}
