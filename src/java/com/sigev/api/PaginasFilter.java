package com.sigev.api;

import com.sigev.modelo.Usuario;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * Protege las paginas JSP y los servlets de formulario (AA2/AA3), que antes
 * se podian usar sin iniciar sesion.
 * Usa la misma sesion que la API (se inicia desde el front-end) y las mismas
 * reglas de Permisos. Si falla responde 401/403, y web.xml muestra la pagina
 * de error con la marca SIGEV.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
public class PaginasFilter extends HttpFilter {

    // Que recurso de Permisos corresponde a cada pagina o servlet
    private static final Map<String, String> RECURSO = Map.of(
            "/ProductoServlet", "productos",
            "/index.jsp", "productos",
            "/lista.jsp", "productos",
            "/ClienteServlet", "clientes",
            "/clientes.jsp", "clientes",
            "/VentaServlet", "ventas",
            "/registrar_venta.jsp", "ventas"
    );

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpSession sesion = request.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute(ApiUtil.USUARIO_SESION);
        if (usuario == null) {
            response.sendError(401);
            return;
        }

        String ruta = request.getServletPath();
        if (ruta.equals("/")) {
            ruta = "/index.jsp";
        }
        // Una pagina que no este en el mapa queda solo para Admin (seguro por defecto)
        String recurso = RECURSO.getOrDefault(ruta, "pagina-desconocida");
        if (!Permisos.permitido(usuario.getRol(), request.getMethod(), recurso)) {
            response.sendError(403);
            return;
        }
        chain.doFilter(request, response);
    }
}
