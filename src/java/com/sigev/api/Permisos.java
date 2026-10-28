package com.sigev.api;

import java.util.Map;
import java.util.Set;

/**
 * Reglas de acceso por rol a la API. Es el UNICO lugar donde se definen.
 *
 * Cada recurso (/api/{recurso}) dice que metodos HTTP puede usar cada rol.
 * Admin puede todo. Un recurso que no aparezca aqui (por ejemplo un modulo
 * nuevo como /api/proveedores o /api/estadisticas) queda SOLO para Admin
 * hasta que se agregue su regla: es seguro por defecto.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
public class Permisos {

    public static final String ADMIN = "Admin";
    public static final String VENDEDOR = "Vendedor";

    // Lo que puede hacer el Vendedor en cada recurso
    private static final Map<String, Set<String>> VENDEDOR_PUEDE = Map.of(
            "productos", Set.of("GET"),                 // solo consultar precios y stock
            "clientes", Set.of("GET", "POST", "PUT"),   // atender clientes, sin eliminar
            "ventas", Set.of("GET", "POST"),            // registrar y ver ventas
            "sesion", Set.of("GET")                     // saber quien esta conectado
    );

    public static boolean permitido(String rol, String metodo, String recurso) {
        if (ADMIN.equals(rol)) {
            return true;
        }
        if (VENDEDOR.equals(rol)) {
            Set<String> metodos = VENDEDOR_PUEDE.get(recurso);
            return metodos != null && metodos.contains(metodo);
        }
        // Rol desconocido: nada
        return false;
    }
}
