package com.sigev.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Metodos comunes de los servicios REST de SIGEV:
 * leer el JSON que llega, responder JSON y sacar el id de la URL.
 */
public class ApiUtil {

    private static final Gson GSON = new Gson();

    // Lee el cuerpo de la peticion como objeto JSON; devuelve null si no es JSON valido
    public static JsonObject leerJson(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            JsonElement json = JsonParser.parseReader(request.getReader());
            return json.isJsonObject() ? json.getAsJsonObject() : null;
        } catch (JsonParseException e) {
            return null;
        }
    }

    // Responde con el codigo HTTP indicado y el objeto convertido a JSON
    public static void responder(HttpServletResponse response, int estado, Object datos) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(datos));
    }

    // Responde un error con el formato {"error": "mensaje"}
    public static void error(HttpServletResponse response, int estado, String mensaje) throws IOException {
        responder(response, estado, Map.of("error", mensaje));
    }

    /**
     * Saca el id de URLs como /api/productos/5.
     * Devuelve null si la URL no trae id (/api/productos) y -1 si el id no es un numero.
     */
    public static Integer idDeRuta(HttpServletRequest request) {
        String ruta = request.getPathInfo();
        if (ruta == null || ruta.equals("/")) {
            return null;
        }
        try {
            return Integer.parseInt(ruta.substring(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ---------- Lectura de campos del JSON (null si falta o tiene otro tipo) ----------

    public static String texto(JsonObject json, String campo) {
        try {
            JsonElement valor = json.get(campo);
            if (valor == null || valor.isJsonNull()) {
                return null;
            }
            String texto = valor.getAsString().trim();
            return texto.isEmpty() ? null : texto;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Integer entero(JsonObject json, String campo) {
        try {
            JsonElement valor = json.get(campo);
            if (valor == null || valor.isJsonNull()) {
                return null;
            }
            double numero = valor.getAsDouble();
            return numero == Math.floor(numero) ? (int) numero : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Double decimal(JsonObject json, String campo) {
        try {
            JsonElement valor = json.get(campo);
            return (valor == null || valor.isJsonNull()) ? null : valor.getAsDouble();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
