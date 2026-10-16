package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.UsuarioDAO;
import com.mycompany.rentatroka.modelo.Usuario;
import com.mycompany.rentatroka.seguridad.ClaveSegura;
import java.sql.SQLException;
import java.util.Optional;

/** Reglas de acceso: el Servlet solo coordina la petición y la respuesta. */
public class AutenticacionService {

    private final UsuarioDAO usuarioDAO;

    public AutenticacionService() {
        this(new UsuarioDAO());
    }

    public AutenticacionService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Optional<Usuario> autenticar(String nombreUsuario, char[] clave) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.isBlank() || nombreUsuario.length() > 50
                || clave == null || clave.length == 0 || clave.length > 128) {
            return Optional.empty();
        }
        Optional<Usuario> encontrado = usuarioDAO.buscarPorNombreUsuario(nombreUsuario.trim());
        if (encontrado.isEmpty() || !ClaveSegura.verificar(clave, encontrado.get().getClaveHash())) {
            return Optional.empty();
        }
        Usuario usuario = encontrado.get();
        // La sesión necesita la identidad, no el hash de la contraseña.
        usuario.setClaveHash(null);
        return Optional.of(usuario);
    }
}

