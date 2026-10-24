package com.mycompany.rentatroka.dao;

import com.mycompany.rentatroka.config.ConexionBD;
import com.mycompany.rentatroka.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consultas JDBC de la tabla clientes. */
public class ClienteDAO {

    private static final String SELECT_BASE =
            "SELECT id, nombre, documento, telefono FROM clientes";

    public List<Cliente> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY nombre, id";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql);
                ResultSet resultado = consulta.executeQuery()) {
            List<Cliente> clientes = new ArrayList<>();
            while (resultado.next()) {
                clientes.add(leerCliente(resultado));
            }
            return clientes;
        }
    }

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next()
                        ? Optional.of(leerCliente(resultado))
                        : Optional.empty();
            }
        }
    }

    /**
     * Comprueba si otro cliente utiliza el documento. Al editar se excluye
     * el ID del propio cliente para permitir conservar su documento.
     */
    public boolean existeDocumento(String documento, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE documento = ? AND id <> ? LIMIT 1";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, documento);
            consulta.setInt(2, excluirId);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public int insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, documento, telefono) VALUES (?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getDocumento());
            consulta.setString(3, cliente.getTelefono());
            consulta.executeUpdate();
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
            throw new SQLException("MySQL no devolvió el id del cliente registrado.");
        }
    }

    /** Devuelve false si el cliente ya no existe. */
    public boolean actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ?, documento = ?, telefono = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getDocumento());
            consulta.setString(3, cliente.getTelefono());
            consulta.setInt(4, cliente.getId());
            return consulta.executeUpdate() > 0;
        }
    }

    /** MySQL impedirá borrar el cliente si existen alquileres que lo referencian. */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        }
    }

    private static Cliente leerCliente(ResultSet resultado) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(resultado.getInt("id"));
        cliente.setNombre(resultado.getString("nombre"));
        cliente.setDocumento(resultado.getString("documento"));
        cliente.setTelefono(resultado.getString("telefono"));
        return cliente;
    }
}
