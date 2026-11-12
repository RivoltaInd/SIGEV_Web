package com.sigev.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Agrega cabeceras de seguridad a TODAS las respuestas (API, JSP y paginas de error).
 * Le dicen al navegador que sea estricto con lo que recibe de SIGEV:
 *   - X-Content-Type-Options: no adivina el tipo de archivo (evita que un texto
 *     se ejecute como script)
 *   - X-Frame-Options / frame-ancestors: SIGEV no se puede meter dentro de un
 *     marco de otra pagina (evita el engano de "clickjacking")
 *   - Referrer-Policy: no se filtra la direccion de SIGEV a otros sitios
 *   - Cache-Control: no se guardan en disco datos de clientes ni ventas
 *   - Content-Security-Policy en la API: una respuesta JSON nunca carga nada
 * Es el primer filtro en web.xml.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
public class CabecerasFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Cache-Control", "no-store");

        boolean esApi = request.getRequestURI().startsWith(request.getContextPath() + "/api/");
        response.setHeader("Content-Security-Policy",
                esApi ? "default-src 'none'; frame-ancestors 'none'" : "frame-ancestors 'none'");

        chain.doFilter(request, response);
    }
}
