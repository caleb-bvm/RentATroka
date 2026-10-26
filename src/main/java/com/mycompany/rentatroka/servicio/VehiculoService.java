package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.VehiculoDAO;
import com.mycompany.rentatroka.modelo.Vehiculo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Reglas del módulo de vehículos: limpia y valida los datos, revisa que la placa no se repita
// y traduce los errores de MySQL. El Servlet solo coordina la petición y la respuesta.
public class VehiculoService {

    // Clave del mapa de errores para los problemas que no son de un campo
    public static final String ERROR_GENERAL = "general";

    public enum ResultadoEliminacion {
        ELIMINADO,
        NO_EXISTE,
        CON_ALQUILERES
    }

    private static final int MAX_TEXTO = 50;

    // Códigos de error de MySQL
    private static final int MYSQL_ENTRADA_DUPLICADA = 1062;
    private static final int MYSQL_FILA_REFERENCIADA = 1451;

    private static final String PLACA_REPETIDA = "Ya existe un vehículo con esa placa.";
    private static final String VEHICULO_INEXISTENTE = "El vehículo ya no existe. Vuelve al listado.";

    private final VehiculoDAO dao;

    public VehiculoService() {
        this(new VehiculoDAO());
    }

    public VehiculoService(VehiculoDAO dao) {
        this.dao = dao;
    }

    // Convierte el texto del formulario en una tarifa con 2 decimales.
    // Devuelve null si no es un número con hasta 8 enteros y 2 decimales (como DECIMAL(10,2)).
    public static BigDecimal tarifaDesdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        if (!limpio.matches("-?[0-9]{1,8}(\\.[0-9]{1,2})?")) {
            return null;
        }
        return new BigDecimal(limpio).setScale(2);
    }

    // Sin criterio devuelve todos los vehículos; con criterio, los que coinciden por placa, marca o modelo
    public List<Vehiculo> listar(String criterio) throws SQLException {
        if (criterio == null || criterio.isBlank()) {
            return dao.listar();
        }
        String texto = criterio.trim();
        if (texto.length() > MAX_TEXTO) {
            texto = texto.substring(0, MAX_TEXTO);
        }
        return dao.buscar(texto);
    }

    public Optional<Vehiculo> buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // Si el mapa de errores viene vacío, el vehículo quedó registrado con su id
    public Map<String, String> registrar(Vehiculo vehiculo) throws SQLException {
        limpiar(vehiculo);
        Map<String, String> errores = validar(vehiculo);
        if (!errores.isEmpty()) {
            return errores;
        }

        if (dao.existePlaca(vehiculo.getPlaca(), 0)) {
            errores.put("placa", PLACA_REPETIDA);
            return errores;
        }

        try {
            vehiculo.setId(dao.insertar(vehiculo));
        } catch (SQLException ex) {
            // otra petición registró la misma placa justo antes
            if (ex.getErrorCode() == MYSQL_ENTRADA_DUPLICADA) {
                errores.put("placa", PLACA_REPETIDA);
            } else {
                throw ex;
            }
        }
        return errores;
    }

    // Si el mapa de errores viene vacío, los cambios quedaron guardados
    public Map<String, String> actualizar(Vehiculo vehiculo) throws SQLException {
        limpiar(vehiculo);
        Map<String, String> errores = validar(vehiculo);
        if (vehiculo.getId() <= 0) {
            errores.put(ERROR_GENERAL, VEHICULO_INEXISTENTE);
        }
        if (!errores.isEmpty()) {
            return errores;
        }

        if (dao.existePlaca(vehiculo.getPlaca(), vehiculo.getId())) {
            errores.put("placa", PLACA_REPETIDA);
            return errores;
        }

        try {
            boolean actualizado = dao.actualizar(vehiculo);
            if (!actualizado) {
                errores.put(ERROR_GENERAL, VEHICULO_INEXISTENTE);
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_ENTRADA_DUPLICADA) {
                errores.put("placa", PLACA_REPETIDA);
            } else {
                throw ex;
            }
        }
        return errores;
    }

    public ResultadoEliminacion eliminar(int id) throws SQLException {
        try {
            boolean eliminado = dao.eliminar(id);
            if (eliminado) {
                return ResultadoEliminacion.ELIMINADO;
            }
            return ResultadoEliminacion.NO_EXISTE;
        } catch (SQLException ex) {
            // MySQL no deja borrar un vehículo que tiene alquileres
            if (ex.getErrorCode() == MYSQL_FILA_REFERENCIADA) {
                return ResultadoEliminacion.CON_ALQUILERES;
            }
            throw ex;
        }
    }

    // Valida los datos ya limpios, sin consultar la base de datos
    public Map<String, String> validar(Vehiculo vehiculo) {
        Map<String, String> errores = new LinkedHashMap<>();

        String placa = vehiculo.getPlaca();
        if (placa == null || placa.isEmpty()) {
            errores.put("placa", "La placa es obligatoria.");
        } else if (!placa.matches("[A-Z0-9-]{3,15}")) {
            errores.put("placa", "La placa solo admite letras, números y guiones (de 3 a 15 caracteres).");
        }

        String marca = vehiculo.getMarca();
        if (marca == null || marca.isEmpty()) {
            errores.put("marca", "La marca es obligatoria.");
        } else if (marca.length() > MAX_TEXTO) {
            errores.put("marca", "La marca no puede superar los 50 caracteres.");
        }

        String modelo = vehiculo.getModelo();
        if (modelo == null || modelo.isEmpty()) {
            errores.put("modelo", "El modelo es obligatorio.");
        } else if (modelo.length() > MAX_TEXTO) {
            errores.put("modelo", "El modelo no puede superar los 50 caracteres.");
        }

        BigDecimal tarifa = vehiculo.getTarifaDiaria();
        if (tarifa == null) {
            errores.put("tarifaDiaria", "Ingresa una tarifa diaria válida, por ejemplo 65.50 (máximo 2 decimales).");
        } else if (tarifa.compareTo(BigDecimal.ZERO) <= 0) {
            errores.put("tarifaDiaria", "La tarifa diaria debe ser mayor que 0.");
        }

        return errores;
    }

    // Quita espacios sobrantes y guarda la placa en mayúsculas
    private void limpiar(Vehiculo vehiculo) {
        if (vehiculo.getPlaca() == null) {
            vehiculo.setPlaca("");
        }
        if (vehiculo.getMarca() == null) {
            vehiculo.setMarca("");
        }
        if (vehiculo.getModelo() == null) {
            vehiculo.setModelo("");
        }
        vehiculo.setPlaca(vehiculo.getPlaca().trim().toUpperCase());
        vehiculo.setMarca(vehiculo.getMarca().trim());
        vehiculo.setModelo(vehiculo.getModelo().trim());
    }
}
