package com.sigev.api;

import com.sigev.modelo.Usuario;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Protege toda la API. Se ejecuta despues de CorsFilter (orden definido en web.xml).
 *   - Sin sesion iniciada           -> 401 No autenticado
 *   - Rol sin permiso para la ruta  -> 403 Acceso denegado
 *   - POST/PUT sin JSON             -> 415 (evita envios de formularios desde otros sitios)
 *   - Error inesperado              -> 500 con mensaje generico (el detalle va al log)
 * Las reglas de cada rol estan en Permisos.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
public class SeguridadFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String metodo = request.getMethod();
        String recurso = recursoDe(request);

        // Rutas publicas: iniciar y cerrar sesion
        if (recurso.equals("login") || recurso.equals("logout")) {
            chain.doFilter(request, response);
            return;
        }

        if ((metodo.equals("POST") || metodo.equals("PUT")) && !esJson(request)) {
            ApiUtil.error(response, 415, "El contenido debe enviarse como JSON");
            return;
        }

        HttpSession sesion = request.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute(ApiUtil.USUARIO_SESION);
        if (usuario == null) {
            ApiUtil.error(response, 401, "Debe iniciar sesión");
            return;
        }

        if (!Permisos.permitido(usuario.getRol(), metodo, recurso)) {
            ApiUtil.error(response, 403, "Su rol (" + usuario.getRol() + ") no tiene permiso para esta acción");
            return;
        }

        // Si algo falla adentro, el detalle queda en el log del servidor y el
        // cliente solo recibe un mensaje generico (no se filtran datos internos)
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            getServletContext().log("Error en " + metodo + " " + request.getRequestURI(), e);
            if (!response.isCommitted()) {
                response.reset();
                ApiUtil.error(response, 500, "Ocurrió un error interno. Intente de nuevo más tarde");
            }
        }
    }

    // /SIGEV_Web/api/productos/5 -> "productos"
    private String recursoDe(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        String[] partes = ruta.split("/");
        // partes: ["", "api", "productos", "5"]
        return partes.length > 2 ? partes[2] : "";
    }

    private boolean esJson(HttpServletRequest request) {
        String tipo = request.getContentType();
        return tipo != null && tipo.toLowerCase().startsWith("application/json");
    }
}
