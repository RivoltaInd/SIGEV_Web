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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de inicio de sesion.
 *   POST /api/login   {correo, clave, recordar?}
 * Responde 200 con {mensaje, nombre, correo, rol} si la autenticacion es correcta,
 * 401 si el correo o la contrasena no coinciden, 403 si la cuenta esta desactivada
 * y 429 si hubo demasiados intentos.
 *
 * Si es correcta, crea una sesion nueva en el servidor con el usuario. La sesion
 * dura 30 minutos de inactividad, u 8 horas si marco "Recordarme".
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
@WebServlet("/api/login")
public class LoginApi extends HttpServlet {

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MS = 5 * 60 * 1000;   // 5 minutos
    private static final int MAX_CORREOS_VIGILADOS = 10000;

    // Hash de una clave aleatoria que nadie conoce: solo sirve para gastar el mismo
    // tiempo cuando el correo no existe (ver doPost)
    private static final String HASH_RELLENO = Clave.cifrar(java.util.UUID.randomUUID().toString());

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Intentos fallidos por correo: {cantidad, momento del ultimo fallo}
    private final Map<String, long[]> intentos = new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = ApiUtil.leerJson(request);
        String correo = json == null ? null : ApiUtil.texto(json, "correo");
        String clave = json == null ? null : ApiUtil.texto(json, "clave");
        boolean recordar = json != null && "true".equals(ApiUtil.texto(json, "recordar"));

        if (correo == null || clave == null) {
            ApiUtil.error(response, 400, "Ingrese correo y contraseña");
            return;
        }
        // Limites de tamano: calcular PBKDF2 sobre un texto enorme gastaria la CPU
        // del servidor, y ningun correo o clave valido supera estos largos
        if (correo.length() > 120 || clave.length() > 100) {
            ApiUtil.error(response, 401, "Correo o contraseña incorrectos");
            return;
        }
        correo = correo.toLowerCase();

        if (estaBloqueado(correo)) {
            ApiUtil.error(response, 429, "Demasiados intentos fallidos. Espere 5 minutos e intente de nuevo");
            return;
        }

        Usuario usuario = usuarioDAO.consultarPorCorreo(correo);

        // El mismo mensaje para correo inexistente o clave errada: no se revela cual fallo.
        // Si el correo no existe igual se calcula un hash (contra uno de relleno) para
        // que la respuesta tarde lo mismo: sin esto, midiendo el tiempo se podria
        // saber que correos estan registrados.
        String hash = usuario != null ? usuario.getClaveHash() : HASH_RELLENO;
        boolean claveCorrecta = Clave.verificar(clave, hash);
        if (usuario == null || !claveCorrecta) {
            registrarFallo(correo);
            ApiUtil.error(response, 401, "Correo o contraseña incorrectos");
            return;
        }
        intentos.remove(correo);

        // Solo se avisa despues de verificar la clave: asi no se revela a un extraño
        // que la cuenta existe
        if (!usuario.isActivo()) {
            ApiUtil.error(response, 403, "Tu cuenta está desactivada. Habla con un Administrador");
            return;
        }

        // Sesion nueva: se descarta cualquier sesion anterior (evita fijacion de sesion)
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = request.getSession(true);
        sesion.setMaxInactiveInterval(recordar ? 8 * 60 * 60 : 30 * 60);

        usuario.setClaveHash(null);   // el hash nunca queda en memoria de sesion
        sesion.setAttribute(ApiUtil.USUARIO_SESION, usuario);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Autenticación satisfactoria");
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("rol", usuario.getRol());
        ApiUtil.responder(response, 200, respuesta);
    }

    private boolean estaBloqueado(String correo) {
        long[] datos = intentos.get(correo);
        if (datos == null || datos[0] < MAX_INTENTOS) {
            return false;
        }
        if (System.currentTimeMillis() - datos[1] > BLOQUEO_MS) {
            intentos.remove(correo);   // ya paso el tiempo de bloqueo
            return false;
        }
        return true;
    }

    private void registrarFallo(String correo) {
        // Si alguien prueba miles de correos distintos, se borran los registros ya
        // vencidos para que este mapa no crezca hasta llenar la memoria
        if (intentos.size() >= MAX_CORREOS_VIGILADOS) {
            long ahora = System.currentTimeMillis();
            intentos.values().removeIf(datos -> ahora - datos[1] > BLOQUEO_MS);
            if (intentos.size() >= MAX_CORREOS_VIGILADOS) {
                intentos.clear();
            }
        }
        intentos.compute(correo, (c, datos) -> datos == null
                ? new long[]{1, System.currentTimeMillis()}
                : new long[]{datos[0] + 1, System.currentTimeMillis()});
    }
}
