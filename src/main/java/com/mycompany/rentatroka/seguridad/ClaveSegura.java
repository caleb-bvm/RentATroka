package com.mycompany.rentatroka.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash con sal individual. La contraseña original no se guarda en la base de datos. */
public final class ClaveSegura {

    private static final int ITERACIONES = 600_000;
    private static final int BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private ClaveSegura() {
    }

    public static String generarHash(char[] clave) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        return "pbkdf2_sha256$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(char[] clave, String hashGuardado) {
        if (clave == null || hashGuardado == null) {
            return false;
        }
        try {
            String[] partes = hashGuardado.split("\\$", -1);
            if (partes.length != 4 || !"pbkdf2_sha256".equals(partes[0])) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            if (iteraciones < 1 || iteraciones > 1_000_000) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            if (sal.length != 16 || esperado.length != BITS / 8) {
                return false;
            }
            byte[] calculado = derivar(clave, sal, iteraciones);
            try {
                return MessageDigest.isEqual(esperado, calculado);
            } finally {
                Arrays.fill(calculado, (byte) 0);
            }
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derivar(char[] clave, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(clave, sal, iteraciones, BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo procesar la contraseña.", ex);
        } finally {
            especificacion.clearPassword();
        }
    }
}

