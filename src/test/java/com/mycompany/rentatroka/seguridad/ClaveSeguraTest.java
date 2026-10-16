package com.mycompany.rentatroka.seguridad;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ClaveSeguraTest {

    private static final String HASH_SEMILLA = "pbkdf2_sha256$600000$b/knvC+x6knMG1iRODZ2hw==$+qokPQfBs6o/OVl3vhYuIYnk6lGBhtBaIM7xkbCv8b8=";

    @Test
    void aceptaLaCuentaSemillaDelScriptSql() {
        assertTrue(ClaveSegura.verificar("Admin123!".toCharArray(), HASH_SEMILLA));
    }

    @Test
    void rechazaUnaClaveIncorrecta() {
        assertFalse(ClaveSegura.verificar("otra-clave".toCharArray(), HASH_SEMILLA));
    }

    @Test
    void cadaHashUsaUnaSalDiferenteYConservaLosCaracteresEspeciales() {
        char[] clave = "Camión-con-ñ-2026!".toCharArray();
        String primero = ClaveSegura.generarHash(clave);
        String segundo = ClaveSegura.generarHash(clave);
        assertNotEquals(primero, segundo);
        assertTrue(ClaveSegura.verificar(clave, primero));
        assertTrue(ClaveSegura.verificar(clave, segundo));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"sin-formato", "pbkdf2_sha256$abc$x$y", "pbkdf2_sha256$999999999$x$y",
        "pbkdf2_sha256$600000$!$!", "pbkdf2_sha256$600000$$"})
    void unHashCorruptoNoAutenticaNiRompeLaPeticion(String hash) {
        assertFalse(ClaveSegura.verificar("Admin123!".toCharArray(), hash));
    }
}

