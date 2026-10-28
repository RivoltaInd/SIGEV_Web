package com.sigev.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Cifrado de contrasenas con PBKDF2-SHA256 (incluido en Java, sin librerias extra).
 * Se guarda "sal:hash" en Base64. La sal es aleatoria para cada usuario, asi dos
 * usuarios con la misma contrasena no quedan con el mismo hash.
 */
public class Clave {

    private static final int ITERACIONES = 120000;
    private static final int LONGITUD_BITS = 256;

    public static String cifrar(String clave) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] hash = calcular(clave, sal);
        Base64.Encoder b64 = Base64.getEncoder();
        return b64.encodeToString(sal) + ":" + b64.encodeToString(hash);
    }

    public static boolean verificar(String clave, String guardado) {
        String[] partes = guardado.split(":");
        if (partes.length != 2) {
            return false;
        }
        Base64.Decoder b64 = Base64.getDecoder();
        byte[] sal = b64.decode(partes[0]);
        byte[] esperado = b64.decode(partes[1]);
        // MessageDigest.isEqual compara en tiempo constante
        return MessageDigest.isEqual(esperado, calcular(clave, sal));
    }

    private static byte[] calcular(String clave, byte[] sal) {
        try {
            KeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, ITERACIONES, LONGITUD_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cifrar la contraseña", e);
        }
    }
}
