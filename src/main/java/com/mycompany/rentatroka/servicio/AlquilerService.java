package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.AlquilerDAO;
import com.mycompany.rentatroka.dao.AlquilerDAO.Resultado;
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

public class AlquilerService {

    public static final String ERROR_GENERAL = "general";

    public enum ResultadoEliminacion {
        ELIMINADO,
        NO_EXISTE
    }

    private static final ZoneId ZONA = ZoneId.of("America/El_Salvador");

    private static final String ALQUILER_INEXISTENTE =
            "El alquiler ya no existe. Vuelve al listado.";

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

    // registrar alquiler nuevo (valida campos y fechas; la disponibilidad se revisa en la transacción)
    public Map<String, String> registrar(Alquiler alquiler) throws SQLException {
        Map<String, String> errores = validar(alquiler, true);

        if (!errores.isEmpty()) {
            return errores;
        }

        traducir(alquilerDao.registrar(alquiler), errores);
        return errores;
    }

    // actualizar alquiler existente
    public Map<String, String> actualizar(Alquiler alquiler) throws SQLException {
        Map<String, String> errores = validar(alquiler, false);

        if (alquiler.getId() <= 0) {
            errores.put(ERROR_GENERAL, ALQUILER_INEXISTENTE);
        }

        if (!errores.isEmpty()) {
            return errores;
        }

        traducir(alquilerDao.actualizar(alquiler), errores);
        return errores;
    }

    public ResultadoEliminacion eliminar(int id) throws SQLException {
        return alquilerDao.eliminar(id)
                ? ResultadoEliminacion.ELIMINADO
                : ResultadoEliminacion.NO_EXISTE;
    }

    // validaciones del formulario (no consulta la base de datos)
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

        if (inicio == null) {
            errores.put("fechaInicio", "La fecha de inicio es obligatoria y debe ser válida.");
        } else if (esNuevo && inicio.isBefore(hoy())) {
            // Un alquiler nuevo no puede empezar en el pasado.
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
        return alquiler.getTarifaDiariaAplicada()
                .multiply(BigDecimal.valueOf(calcularDias(alquiler)));
    }

    private static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    // convierte el resultado de la transacción en mensajes para el formulario
    private static void traducir(Resultado resultado, Map<String, String> errores) {
        switch (resultado) {
            case GUARDADO -> {
            }
            case CLIENTE_INEXISTENTE ->
                errores.put("cliente", "El cliente seleccionado ya no existe.");
            case VEHICULO_INEXISTENTE ->
                errores.put("vehiculo", "El vehículo seleccionado ya no existe.");
            case VEHICULO_OCUPADO ->
                errores.put("vehiculo",
                        "Ese vehículo ya está alquilado en alguna de las fechas elegidas. "
                        + "Cambia las fechas o elige otro vehículo.");
            case ALQUILER_INEXISTENTE ->
                errores.put(ERROR_GENERAL, ALQUILER_INEXISTENTE);
        }
    }
}