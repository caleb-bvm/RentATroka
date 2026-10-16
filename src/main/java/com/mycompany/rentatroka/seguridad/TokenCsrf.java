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
        Object esperado = sesion.getAttribute("csrfToken");
        return esperado instanceof String token && MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8));
    }
}

