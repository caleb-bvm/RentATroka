package com.mycompany.rentatroka.dao;

import com.mycompany.rentatroka.config.ConexionBD;
import com.mycompany.rentatroka.modelo.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//  Consultas JDBC de la tabla vehiculos

public class VehiculoDAO {

    private static final String SELECT_BASE = "SELECT id, placa, marca, modelo, tarifa_diaria FROM vehiculos";

    public List<Vehiculo> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY placa";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql);
                ResultSet resultado = consulta.executeQuery()) {
            return leerVehiculos(resultado);
        }
    }

    // Buscar coincidencias parciales por placa, marca o modelo, sin distinguir mayúsculas
    public List<Vehiculo> buscar(String termino) throws SQLException {
        String sql = SELECT_BASE + " WHERE placa LIKE ? OR marca LIKE ? OR modelo LIKE ? ORDER BY placa";
        String patron = "%" + termino + "%";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, patron);
            consulta.setString(2, patron);
            consulta.setString(3, patron);
            try (ResultSet resultado = consulta.executeQuery()) {
                return leerVehiculos(resultado);
            }
        }
    }

    public Optional<Vehiculo> buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(leerVehiculo(resultado));
                }
            }
        }
        return Optional.empty();
    }

    // Indica si otra fila ya usa la placa. Al editar se excluye el id del propio vehículo
    
    public boolean existePlaca(String placa, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM vehiculos WHERE placa = ? AND id <> ? LIMIT 1";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, placa);
            consulta.setInt(2, excluirId);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    // Registra el vehículo y devuelve el id que genero el gestor de DB
    
    public int insertar(Vehiculo vehiculo) throws SQLException {
        String sql = "INSERT INTO vehiculos (placa, marca, modelo, tarifa_diaria) VALUES (?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, vehiculo.getPlaca());
            consulta.setString(2, vehiculo.getMarca());
            consulta.setString(3, vehiculo.getModelo());
            consulta.setBigDecimal(4, vehiculo.getTarifaDiaria());
            consulta.executeUpdate();
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
            throw new SQLException("MySQL no devolvió el id del vehículo registrado.");
        }
    }

    // Devuelve falso cuando ya no existe un vehículo con ese id
    
    public boolean actualizar(Vehiculo vehiculo) throws SQLException {
        String sql = "UPDATE vehiculos SET placa = ?, marca = ?, modelo = ?, tarifa_diaria = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, vehiculo.getPlaca());
            consulta.setString(2, vehiculo.getMarca());
            consulta.setString(3, vehiculo.getModelo());
            consulta.setBigDecimal(4, vehiculo.getTarifaDiaria());
            consulta.setInt(5, vehiculo.getId());
            return consulta.executeUpdate() > 0;
        }
    }

    /*
        Devuelve falso cuando el vehículo no existe. Si tiene alquileres, el gestor rechaza el
        borrado por la llave y la exception llega tal cual al servicio.
     */
    
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM vehiculos WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        }
    }

    private static List<Vehiculo> leerVehiculos(ResultSet resultado) throws SQLException {
        List<Vehiculo> vehiculos = new ArrayList<>();
        while (resultado.next()) {
            vehiculos.add(leerVehiculo(resultado));
        }
        return vehiculos;
    }

    private static Vehiculo leerVehiculo(ResultSet resultado) throws SQLException {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(resultado.getInt("id"));
        vehiculo.setPlaca(resultado.getString("placa"));
        vehiculo.setMarca(resultado.getString("marca"));
        vehiculo.setModelo(resultado.getString("modelo"));
        vehiculo.setTarifaDiaria(resultado.getBigDecimal("tarifa_diaria"));
        return vehiculo;
    }
}
