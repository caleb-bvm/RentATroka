package com.mycompany.rentatroka.seguridad;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Token compartido por todos los formularios POST del sistema. */
public final class TokenCsrf {

    private TokenCsrf() {
    }

    public static void preparar(HttpSession sesion) {
        if (sesion.getAttribute("csrfToken") == null) {
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            sesion.setAttribute("csrfToken", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }
    }

    public static boolean esValido(HttpSession sesion, String recibido) {
        if (sesion == null || recibido == null) {
            return false;
        }
        String esperado = (String) sesion.getAttribute("csrfToken");
        if (esperado == null) {
            return false;
        }
        // MessageDigest.isEqual compara en tiempo constante (no revela cuántos caracteres coinciden)
        byte[] bytesEsperados = esperado.getBytes(StandardCharsets.UTF_8);
        byte[] bytesRecibidos = recibido.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(bytesEsperados, bytesRecibidos);
    }
}

