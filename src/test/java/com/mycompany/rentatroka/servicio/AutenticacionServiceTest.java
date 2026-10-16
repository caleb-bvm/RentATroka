package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.UsuarioDAO;
import com.mycompany.rentatroka.modelo.Usuario;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutenticacionServiceTest {

    private static final String HASH_SEMILLA = "pbkdf2_sha256$600000$b/knvC+x6knMG1iRODZ2hw==$+qokPQfBs6o/OVl3vhYuIYnk6lGBhtBaIM7xkbCv8b8=";

    @Test
    void validaDatosAntesDeConsultarLaBase() throws SQLException {
        UsuarioDAOPrueba dao = new UsuarioDAOPrueba();
        AutenticacionService servicio = new AutenticacionService(dao);
        assertTrue(servicio.autenticar(" ", "clave".toCharArray()).isEmpty());
        assertTrue(servicio.autenticar(null, "clave".toCharArray()).isEmpty());
        assertTrue(servicio.autenticar("admin", null).isEmpty());
        assertTrue(servicio.autenticar("admin", new char[0]).isEmpty());
        assertTrue(servicio.autenticar("x".repeat(51), "clave".toCharArray()).isEmpty());
        assertTrue(servicio.autenticar("admin", new char[129]).isEmpty());
        assertEquals(0, dao.consultas);
    }

    @Test
    void autenticaYRetiraElHashAntesDeGuardarLaIdentidadEnSesion() throws SQLException {
        UsuarioDAOPrueba dao = new UsuarioDAOPrueba();
        Usuario usuario = new AutenticacionService(dao)
                .autenticar(" admin ", "Admin123!".toCharArray()).orElseThrow();
        assertEquals("admin", dao.ultimoNombre);
        assertEquals(1, usuario.getId());
        assertEquals("Administrador", usuario.getNombre());
        assertNull(usuario.getClaveHash());
    }

    @Test
    void rechazaUsuarioInexistenteYClaveIncorrecta() throws SQLException {
        UsuarioDAOPrueba dao = new UsuarioDAOPrueba();
        AutenticacionService servicio = new AutenticacionService(dao);
        assertTrue(servicio.autenticar("inexistente", "Admin123!".toCharArray()).isEmpty());
        assertTrue(servicio.autenticar("admin", "incorrecta".toCharArray()).isEmpty());
    }

    @Test
    void unFalloDeConexionSeDistingueDeUnaClaveIncorrecta() {
        UsuarioDAO dao = new UsuarioDAO() {
            @Override
            public Optional<Usuario> buscarPorNombreUsuario(String nombre) throws SQLException {
                throw new SQLException("Conexión no disponible");
            }
        };
        assertThrows(SQLException.class,
                () -> new AutenticacionService(dao).autenticar("admin", "Admin123!".toCharArray()));
    }

    private static class UsuarioDAOPrueba extends UsuarioDAO {
        private int consultas;
        private String ultimoNombre;

        @Override
        public Optional<Usuario> buscarPorNombreUsuario(String nombre) {
            consultas++;
            ultimoNombre = nombre;
            if (!"admin".equals(nombre)) {
                return Optional.empty();
            }
            Usuario usuario = new Usuario();
            usuario.setId(1);
            usuario.setNombre("Administrador");
            usuario.setNombreUsuario("admin");
            usuario.setClaveHash(HASH_SEMILLA);
            return Optional.of(usuario);
        }
    }
}
