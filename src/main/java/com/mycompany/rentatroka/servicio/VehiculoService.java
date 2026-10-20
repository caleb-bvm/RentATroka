package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.VehiculoDAO;
import com.mycompany.rentatroka.modelo.Vehiculo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/*
    Reglas del módulo de vehículos - normaliza y valida los datos, comprueba la placa única
    y traduce los errores del gestor DB. El Servlet solo coordina la petición y la respuesta.
 */

public class VehiculoService {

    // Clave del mapa de errores para los problemas que no pertenecen a un campo concreto
    
    public static final String ERROR_GENERAL = "general";

    public enum ResultadoEliminacion { ELIMINADO, NO_EXISTE, CON_ALQUILERES }

    private static final int MAX_TEXTO = 50;
    private static final Pattern PLACA = Pattern.compile("[A-Z0-9-]{3,15}");
    
    // Hasta 8 enteros y 2 decimales, igual que DECIMAL(10,2) en la tabla
    private static final Pattern TARIFA = Pattern.compile("-?\\d{1,8}(\\.\\d{1,2})?");

    // Códigos de error de MySQL - entrada duplicada y fila referenciada por otra tabla.
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

    // Convierte el texto del formulario en una tarifa con 2 decimales, null si no es válido
    
    public static BigDecimal tarifaDesdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return TARIFA.matcher(limpio).matches() ? new BigDecimal(limpio).setScale(2) : null;
    }

    // Sin criterio devuelve todos los vehículos - con criterio, los que coinciden por placa, marca o modelo
    
    public List<Vehiculo> listar(String criterio) throws SQLException {
        String texto = criterio == null ? "" : criterio.trim();
        if (texto.length() > MAX_TEXTO) {
            texto = texto.substring(0, MAX_TEXTO);
        }
        return texto.isEmpty() ? dao.listar() : dao.buscar(texto);
    }

    public Optional<Vehiculo> buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // Devuelve los errores por campo: si el mapa viene vacío, el vehículo quedó registrado con su id. */
    
    public Map<String, String> registrar(Vehiculo vehiculo) throws SQLException {
        normalizar(vehiculo);
        Map<String, String> errores = validar(vehiculo);
        if (!errores.isEmpty()) {
            return errores;
        }
        try {
            if (dao.existePlaca(vehiculo.getPlaca(), 0)) {
                errores.put("placa", PLACA_REPETIDA);
            } else {
                vehiculo.setId(dao.insertar(vehiculo));
            }
        } catch (SQLException ex) {
            errores.putAll(traducirDuplicado(ex));
        }
        return errores;
    }

    // Devuelve los errores por campo: si el mapa viene vacío, los cambios quedaron guardados. 
    
    public Map<String, String> actualizar(Vehiculo vehiculo) throws SQLException {
        normalizar(vehiculo);
        Map<String, String> errores = validar(vehiculo);
        if (vehiculo.getId() <= 0) {
            errores.put(ERROR_GENERAL, VEHICULO_INEXISTENTE);
        }
        if (!errores.isEmpty()) {
            return errores;
        }
        try {
            if (dao.existePlaca(vehiculo.getPlaca(), vehiculo.getId())) {
                errores.put("placa", PLACA_REPETIDA);
            } else if (!dao.actualizar(vehiculo)) {
                errores.put(ERROR_GENERAL, VEHICULO_INEXISTENTE);
            }
        } catch (SQLException ex) {
            errores.putAll(traducirDuplicado(ex));
        }
        return errores;
    }

    public ResultadoEliminacion eliminar(int id) throws SQLException {
        try {
            return dao.eliminar(id) ? ResultadoEliminacion.ELIMINADO : ResultadoEliminacion.NO_EXISTE;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_FILA_REFERENCIADA) {
                return ResultadoEliminacion.CON_ALQUILERES;
            }
            throw ex;
        }
    }

    // Valida los datos ya normalizados, sin consultar la base
    
    public Map<String, String> validar(Vehiculo vehiculo) {
        Map<String, String> errores = new LinkedHashMap<>();
        String placa = vehiculo.getPlaca() == null ? "" : vehiculo.getPlaca();
        if (placa.isEmpty()) {
            errores.put("placa", "La placa es obligatoria.");
        } else if (!PLACA.matcher(placa).matches()) {
            errores.put("placa", "La placa solo admite letras, números y guiones (de 3 a 15 caracteres).");
        }
        validarTexto(errores, "marca", "La marca es obligatoria.", "La marca", vehiculo.getMarca());
        validarTexto(errores, "modelo", "El modelo es obligatorio.", "El modelo", vehiculo.getModelo());
        BigDecimal tarifa = vehiculo.getTarifaDiaria();
        if (tarifa == null) {
            errores.put("tarifaDiaria", "Ingresa una tarifa diaria válida, por ejemplo 65.50 (máximo 2 decimales).");
        } else if (tarifa.signum() <= 0) {
            errores.put("tarifaDiaria", "La tarifa diaria debe ser mayor que 0.");
        }
        return errores;
    }

    private static void validarTexto(Map<String, String> errores, String campo, String obligatorio,
            String sujeto, String valor) {
        if (valor == null || valor.isEmpty()) {
            errores.put(campo, obligatorio);
        } else if (valor.length() > MAX_TEXTO) {
            errores.put(campo, sujeto + " no puede superar los " + MAX_TEXTO + " caracteres.");
        }
    }

    // Si otra petición registró la misma placa entre la comprobación y el INSERT, se informa como error de campo
    
    private static Map<String, String> traducirDuplicado(SQLException ex) throws SQLException {
        if (ex.getErrorCode() != MYSQL_ENTRADA_DUPLICADA) {
            throw ex;
        }
        return Map.of("placa", PLACA_REPETIDA);
    }

    private static void normalizar(Vehiculo vehiculo) {
        vehiculo.setPlaca(vehiculo.getPlaca() == null ? "" : vehiculo.getPlaca().trim().toUpperCase(Locale.ROOT));
        vehiculo.setMarca(vehiculo.getMarca() == null ? "" : vehiculo.getMarca().trim());
        vehiculo.setModelo(vehiculo.getModelo() == null ? "" : vehiculo.getModelo().trim());
    }
}
