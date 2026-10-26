package com.mycompany.rentatroka.dao;

import com.mycompany.rentatroka.config.ConexionBD;
import com.mycompany.rentatroka.modelo.Alquiler;
import com.mycompany.rentatroka.modelo.AlquilerDetalle;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Consultas JDBC de la tabla alquileres.
// Registrar y actualizar usan una transacción: primero se bloquea el vehículo
// (SELECT ... FOR UPDATE), luego se revisan cruces de fechas y al final se escribe.
// Así dos altas simultáneas del mismo vehículo no pueden cruzarse.

public class AlquilerDAO {

    // Resultado de registrar o actualizar
    public enum Resultado {
        GUARDADO,
        ALQUILER_INEXISTENTE,
        CLIENTE_INEXISTENTE,
        VEHICULO_INEXISTENTE,
        VEHICULO_OCUPADO
    }

    // JOIN: junta alquileres con clientes y vehiculos y calcula días e importe en MySQL
    private static final String SELECT_DETALLE = """
            SELECT a.id, a.cliente_id, a.vehiculo_id,
                   c.nombre AS cliente, v.placa, v.marca, v.modelo,
                   a.fecha_inicio, a.fecha_fin, a.tarifa_diaria_aplicada,
                   DATEDIFF(a.fecha_fin, a.fecha_inicio) AS dias,
                   DATEDIFF(a.fecha_fin, a.fecha_inicio) * a.tarifa_diaria_aplicada AS total
            FROM alquileres a
            JOIN clientes c ON c.id = a.cliente_id
            JOIN vehiculos v ON v.id = a.vehiculo_id
            ORDER BY a.fecha_inicio DESC, a.id DESC
            """;

    // Listado combinado (JOIN)
    public List<AlquilerDetalle> listarDetalle() throws SQLException {
        List<AlquilerDetalle> alquileres = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(SELECT_DETALLE);
                ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                AlquilerDetalle detalle = new AlquilerDetalle();
                leerAlquiler(resultado, detalle);
                detalle.setClienteNombre(resultado.getString("cliente"));
                detalle.setPlaca(resultado.getString("placa"));
                detalle.setMarca(resultado.getString("marca"));
                detalle.setModelo(resultado.getString("modelo"));
                detalle.setDias(resultado.getInt("dias"));
                detalle.setTotal(resultado.getBigDecimal("total"));
                alquileres.add(detalle);
            }
        }
        return alquileres;
    }

    public Optional<Alquiler> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, cliente_id, vehiculo_id, fecha_inicio, fecha_fin, tarifa_diaria_aplicada "
                + "FROM alquileres WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    Alquiler alquiler = new Alquiler();
                    leerAlquiler(resultado, alquiler);
                    return Optional.of(alquiler);
                }
            }
        }
        return Optional.empty();
    }

    // Registra el alquiler copiando la tarifa ACTUAL del vehículo a tarifa_diaria_aplicada.
    // Si todo sale bien deja el id y la tarifa en el objeto recibido.
    public Resultado registrar(Alquiler alquiler) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Resultado resultado = registrarEnTransaccion(conexion, alquiler);
                terminarTransaccion(conexion, resultado);
                return resultado;
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    // Actualiza el alquiler. Si el vehículo es el mismo se conserva la tarifa que ya tenía;
    // si cambió de vehículo se toma la tarifa actual del vehículo nuevo.
    public Resultado actualizar(Alquiler alquiler) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Resultado resultado = actualizarEnTransaccion(conexion, alquiler);
                terminarTransaccion(conexion, resultado);
                return resultado;
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    // Devuelve falso cuando ya no existe un alquiler con ese id
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM alquileres WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        }
    }

    // ---------- pasos dentro de la transacción ----------

    private Resultado registrarEnTransaccion(Connection conexion, Alquiler alquiler) throws SQLException {
        BigDecimal tarifaVehiculo = bloquearVehiculo(conexion, alquiler.getVehiculoId());
        if (tarifaVehiculo == null) {
            return Resultado.VEHICULO_INEXISTENTE;
        }
        if (!existeCliente(conexion, alquiler.getClienteId())) {
            return Resultado.CLIENTE_INEXISTENTE;
        }
        if (hayCruce(conexion, alquiler, 0)) {
            return Resultado.VEHICULO_OCUPADO;
        }

        alquiler.setTarifaDiariaAplicada(tarifaVehiculo);

        String sql = "INSERT INTO alquileres (cliente_id, vehiculo_id, fecha_inicio, fecha_fin, tarifa_diaria_aplicada) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setInt(1, alquiler.getClienteId());
            consulta.setInt(2, alquiler.getVehiculoId());
            consulta.setObject(3, alquiler.getFechaInicio());
            consulta.setObject(4, alquiler.getFechaFin());
            consulta.setBigDecimal(5, alquiler.getTarifaDiariaAplicada());
            consulta.executeUpdate();

            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (claves.next()) {
                    alquiler.setId(claves.getInt(1));
                }
            }
        }
        return Resultado.GUARDADO;
    }

    private Resultado actualizarEnTransaccion(Connection conexion, Alquiler alquiler) throws SQLException {
        // datos que ya tenía guardados el alquiler (también se bloquea su fila)
        int vehiculoGuardado;
        BigDecimal tarifaGuardada;

        String sqlActual = "SELECT vehiculo_id, tarifa_diaria_aplicada FROM alquileres WHERE id = ? FOR UPDATE";
        try (PreparedStatement consulta = conexion.prepareStatement(sqlActual)) {
            consulta.setInt(1, alquiler.getId());
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return Resultado.ALQUILER_INEXISTENTE;
                }
                vehiculoGuardado = resultado.getInt("vehiculo_id");
                tarifaGuardada = resultado.getBigDecimal("tarifa_diaria_aplicada");
            }
        }

        BigDecimal tarifaVehiculo = bloquearVehiculo(conexion, alquiler.getVehiculoId());
        if (tarifaVehiculo == null) {
            return Resultado.VEHICULO_INEXISTENTE;
        }
        if (!existeCliente(conexion, alquiler.getClienteId())) {
            return Resultado.CLIENTE_INEXISTENTE;
        }
        // al editar se excluye el propio alquiler del chequeo de cruces
        if (hayCruce(conexion, alquiler, alquiler.getId())) {
            return Resultado.VEHICULO_OCUPADO;
        }

        if (alquiler.getVehiculoId() == vehiculoGuardado) {
            alquiler.setTarifaDiariaAplicada(tarifaGuardada);
        } else {
            alquiler.setTarifaDiariaAplicada(tarifaVehiculo);
        }

        String sql = "UPDATE alquileres SET cliente_id = ?, vehiculo_id = ?, fecha_inicio = ?, fecha_fin = ?, "
                + "tarifa_diaria_aplicada = ? WHERE id = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, alquiler.getClienteId());
            consulta.setInt(2, alquiler.getVehiculoId());
            consulta.setObject(3, alquiler.getFechaInicio());
            consulta.setObject(4, alquiler.getFechaFin());
            consulta.setBigDecimal(5, alquiler.getTarifaDiariaAplicada());
            consulta.setInt(6, alquiler.getId());
            consulta.executeUpdate();
        }
        return Resultado.GUARDADO;
    }

    // Solo se confirma si se guardó; en cualquier otro caso se deshace
    private void terminarTransaccion(Connection conexion, Resultado resultado) throws SQLException {
        if (resultado == Resultado.GUARDADO) {
            conexion.commit();
        } else {
            conexion.rollback();
        }
    }

    // Bloquea la fila del vehículo hasta el commit y devuelve su tarifa actual (null si no existe)
    private BigDecimal bloquearVehiculo(Connection conexion, int vehiculoId) throws SQLException {
        String sql = "SELECT tarifa_diaria FROM vehiculos WHERE id = ? FOR UPDATE";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, vehiculoId);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getBigDecimal("tarifa_diaria");
                }
                return null;
            }
        }
    }

    // FOR SHARE evita que otro borre al cliente mientras se guarda el alquiler
    private boolean existeCliente(Connection conexion, int clienteId) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE id = ? FOR SHARE";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, clienteId);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    // Cruce = otro alquiler del mismo vehículo con fecha_inicio < nuevo fin Y fecha_fin > nuevo inicio.
    // La fecha final es exclusiva, así que devolver un día y alquilar ese mismo día NO es cruce.
    private boolean hayCruce(Connection conexion, Alquiler alquiler, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM alquileres "
                + "WHERE vehiculo_id = ? AND id <> ? AND fecha_inicio < ? AND fecha_fin > ? LIMIT 1";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, alquiler.getVehiculoId());
            consulta.setInt(2, excluirId);
            consulta.setObject(3, alquiler.getFechaFin());
            consulta.setObject(4, alquiler.getFechaInicio());
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    // Copia las columnas de la tabla alquileres al objeto
    private void leerAlquiler(ResultSet resultado, Alquiler alquiler) throws SQLException {
        alquiler.setId(resultado.getInt("id"));
        alquiler.setClienteId(resultado.getInt("cliente_id"));
        alquiler.setVehiculoId(resultado.getInt("vehiculo_id"));
        alquiler.setFechaInicio(resultado.getObject("fecha_inicio", LocalDate.class));
        alquiler.setFechaFin(resultado.getObject("fecha_fin", LocalDate.class));
        alquiler.setTarifaDiariaAplicada(resultado.getBigDecimal("tarifa_diaria_aplicada"));
    }
}
