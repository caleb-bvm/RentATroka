package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.AlquilerDAO;
import com.mycompany.rentatroka.dao.ClienteDAO;
import com.mycompany.rentatroka.dao.VehiculoDAO;
import com.mycompany.rentatroka.modelo.Alquiler;
import com.mycompany.rentatroka.modelo.AlquilerDetalle;
import com.mycompany.rentatroka.modelo.Cliente;
import com.mycompany.rentatroka.modelo.Vehiculo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Reglas del módulo de alquileres: valida fechas y traduce el resultado de la transacción
// a mensajes para el formulario. La disponibilidad del vehículo la revisa el DAO.
public class AlquilerService {

    // Clave del mapa de errores para los problemas que no son de un campo
    public static final String ERROR_GENERAL = "general";

    public enum ResultadoEliminacion {
        ELIMINADO,
        NO_EXISTE
    }

    private static final String ALQUILER_INEXISTENTE = "El alquiler ya no existe. Vuelve al listado.";

    private final AlquilerDAO alquilerDao;
    private final ClienteDAO clienteDao;
    private final VehiculoDAO vehiculoDao;

    public AlquilerService() {
        this(new AlquilerDAO(), new ClienteDAO(), new VehiculoDAO());
    }

    public AlquilerService(AlquilerDAO alquilerDao, ClienteDAO clienteDao, VehiculoDAO vehiculoDao) {
        this.alquilerDao = alquilerDao;
        this.clienteDao = clienteDao;
        this.vehiculoDao = vehiculoDao;
    }

    // listado con JOIN
    public List<AlquilerDetalle> listar() throws SQLException {
        return alquilerDao.listarDetalle();
    }

    public Optional<Alquiler> buscarPorId(int id) throws SQLException {
        return alquilerDao.buscarPorId(id);
    }

    // datos para llenar los select del formulario
    public List<Cliente> listarClientes() throws SQLException {
        return clienteDao.listar();
    }

    public List<Vehiculo> listarVehiculos() throws SQLException {
        return vehiculoDao.listar();
    }

    // Si el mapa de errores viene vacío, el alquiler quedó registrado
    public Map<String, String> registrar(Alquiler alquiler) throws SQLException {
        Map<String, String> errores = validar(alquiler, true);
        if (!errores.isEmpty()) {
            return errores;
        }

        AlquilerDAO.Resultado resultado = alquilerDao.registrar(alquiler);
        agregarError(resultado, errores);
        return errores;
    }

    // Si el mapa de errores viene vacío, los cambios quedaron guardados
    public Map<String, String> actualizar(Alquiler alquiler) throws SQLException {
        Map<String, String> errores = validar(alquiler, false);
        if (alquiler.getId() <= 0) {
            errores.put(ERROR_GENERAL, ALQUILER_INEXISTENTE);
        }
        if (!errores.isEmpty()) {
            return errores;
        }

        AlquilerDAO.Resultado resultado = alquilerDao.actualizar(alquiler);
        agregarError(resultado, errores);
        return errores;
    }

    public ResultadoEliminacion eliminar(int id) throws SQLException {
        boolean eliminado = alquilerDao.eliminar(id);
        if (eliminado) {
            return ResultadoEliminacion.ELIMINADO;
        }
        return ResultadoEliminacion.NO_EXISTE;
    }

    // Valida el formulario sin consultar la base de datos.
    // esNuevo = true cuando se registra (no se permite empezar en el pasado).
    public Map<String, String> validar(Alquiler alquiler, boolean esNuevo) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (alquiler.getClienteId() <= 0) {
            errores.put("cliente", "Selecciona un cliente.");
        }
        if (alquiler.getVehiculoId() <= 0) {
            errores.put("vehiculo", "Selecciona un vehículo.");
        }

        LocalDate inicio = alquiler.getFechaInicio();
        LocalDate fin = alquiler.getFechaFin();
        LocalDate hoy = LocalDate.now(ZoneId.of("America/El_Salvador"));

        if (inicio == null) {
            errores.put("fechaInicio", "La fecha de inicio es obligatoria y debe ser válida.");
        } else if (esNuevo && inicio.isBefore(hoy)) {
            errores.put("fechaInicio", "La fecha de inicio no puede ser anterior a hoy.");
        }

        if (fin == null) {
            errores.put("fechaFin", "La fecha final es obligatoria y debe ser válida.");
        } else if (inicio != null && !fin.isAfter(inicio)) {
            errores.put("fechaFin", "La fecha final debe ser posterior a la fecha de inicio.");
        }

        return errores;
    }

    // Días cobrados: la fecha final es exclusiva (del 2 al 5 son 3 días)
    public static long calcularDias(Alquiler alquiler) {
        return ChronoUnit.DAYS.between(alquiler.getFechaInicio(), alquiler.getFechaFin());
    }

    // Importe = días × tarifa aplicada (la que quedó guardada en el alquiler)
    public static BigDecimal calcularTotal(Alquiler alquiler) {
        BigDecimal dias = BigDecimal.valueOf(calcularDias(alquiler));
        return alquiler.getTarifaDiariaAplicada().multiply(dias);
    }

    // Convierte el resultado de la transacción en un mensaje para el formulario
    private void agregarError(AlquilerDAO.Resultado resultado, Map<String, String> errores) {
        if (resultado == AlquilerDAO.Resultado.CLIENTE_INEXISTENTE) {
            errores.put("cliente", "El cliente seleccionado ya no existe.");
        } else if (resultado == AlquilerDAO.Resultado.VEHICULO_INEXISTENTE) {
            errores.put("vehiculo", "El vehículo seleccionado ya no existe.");
        } else if (resultado == AlquilerDAO.Resultado.VEHICULO_OCUPADO) {
            errores.put("vehiculo", "Ese vehículo ya está alquilado en alguna de las fechas elegidas. "
                    + "Cambia las fechas o elige otro vehículo.");
        } else if (resultado == AlquilerDAO.Resultado.ALQUILER_INEXISTENTE) {
            errores.put(ERROR_GENERAL, ALQUILER_INEXISTENTE);
        }
    }
}
