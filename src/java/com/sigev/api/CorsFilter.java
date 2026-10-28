package com.sigev.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Permite que el front-end en React (Vite, http://localhost:5173) llame a la API,
 * que corre en otro puerto (Tomcat, 8080). Sin esto el navegador bloquea las
 * peticiones por la politica CORS.
 * Solo se acepta ese origen exacto, y con credenciales para que viaje la
 * cookie de sesion. Va antes de SeguridadFilter (orden definido en web.xml).
 */
public class CorsFilter extends HttpFilter {

    private static final String ORIGEN_FRONT = "http://localhost:5173";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        response.setHeader("Access-Control-Allow-Origin", ORIGEN_FRONT);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Vary", "Origin");

        // El navegador manda OPTIONS antes de POST/PUT/DELETE para preguntar si puede
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        chain.doFilter(request, response);
    }
}
