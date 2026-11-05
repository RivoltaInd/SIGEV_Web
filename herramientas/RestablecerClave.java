package herramientas;

import com.sigev.util.Clave;
import com.sigev.util.ConexionBD;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Herramienta de consola para restablecer la contrasena de un usuario
 * cuando se olvido (no hay forma de recuperarla: solo se guarda el hash).
 *
 * Pide el correo y la contrasena nueva sin mostrarla en pantalla, la cifra
 * con PBKDF2 (util.Clave, igual que la app) y la guarda en la tabla usuario.
 * Tambien deja el usuario activo. Usa db.properties, igual que la app.
 *
 * No hace parte de la aplicacion web (no esta en src/java, no va en el .war).
 * Se ejecuta con herramientas\restablecer_clave.ps1 en una consola local.
 *
 * Evidencia GA7-220501096-AA5
 * Aprendiz: Faber Stiven Londoño Gil
 */
public class RestablecerClave {

    public static void main(String[] args) {
        Console consola = System.console();
        if (consola == null) {
            System.err.println("Ejecute esta herramienta en una consola (PowerShell o CMD), no desde el IDE.");
            System.exit(1);
        }

        String correo = consola.readLine("Correo del usuario: ").trim().toLowerCase();
        char[] nueva = consola.readPassword("Contraseña nueva (no se muestra): ");
        char[] repetida = consola.readPassword("Repita la contraseña nueva: ");

        try {
            if (!Arrays.equals(nueva, repetida)) {
                System.err.println("Las contraseñas no coinciden. No se cambió nada.");
                System.exit(1);
            }
            String error = validar(new String(nueva), correo);
            if (error != null) {
                System.err.println(error + ". No se cambió nada.");
                System.exit(1);
            }

            String sql = "UPDATE usuario SET clave_hash = ?, activo = 1 WHERE correo = ?";
            try (Connection con = ConexionBD.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, Clave.cifrar(new String(nueva)));
                ps.setString(2, correo);
                if (ps.executeUpdate() == 0) {
                    System.err.println("No existe un usuario con ese correo. No se cambió nada.");
                    System.exit(1);
                }
            }
            System.out.println("Contraseña restablecida para " + correo + ".");
            System.out.println("Si hubo 5 intentos fallidos, espere 5 minutos o reinicie Tomcat antes de entrar.");
        } catch (SQLException e) {
            System.err.println("No se pudo conectar o actualizar la base de datos: " + e.getMessage());
            System.exit(1);
        } finally {
            // No dejar la contrasena en memoria mas de lo necesario
            Arrays.fill(nueva, '\0');
            Arrays.fill(repetida, '\0');
        }
    }

    // Mismas reglas que CambiarClaveApi.validarNueva (sin la clave actual)
    private static String validar(String nueva, String correo) {
        if (nueva.length() < 10 || nueva.length() > 100) {
            return "La contraseña debe tener entre 10 y 100 caracteres";
        }
        if (!nueva.matches(".*[A-Za-z].*") || !nueva.matches(".*\\d.*")) {
            return "La contraseña debe tener letras y números";
        }
        if (nueva.equalsIgnoreCase(correo) || nueva.toLowerCase().contains("sigev")) {
            return "La contraseña es muy fácil de adivinar";
        }
        return null;
    }
}
