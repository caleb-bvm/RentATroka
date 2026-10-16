package com.mycompany.rentatroka.dao;

import com.mycompany.rentatroka.config.ConexionBD;
import com.mycompany.rentatroka.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Las consultas SQL pertenecen al modelo, nunca al Servlet ni a la JSP. */
public class UsuarioDAO {

    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT id, nombre, nombre_usuario, clave_hash FROM usuarios WHERE nombre_usuario = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nombreUsuario);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                Usuario usuario = new Usuario();
                usuario.setId(resultado.getInt("id"));
                usuario.setNombre(resultado.getString("nombre"));
                usuario.setNombreUsuario(resultado.getString("nombre_usuario"));
                usuario.setClaveHash(resultado.getString("clave_hash"));
                return Optional.of(usuario);
            }
        }
    }
}

