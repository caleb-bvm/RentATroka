package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.ClienteDAO;
import com.mycompany.rentatroka.modelo.Cliente;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Normaliza datos, valida los campos y aplica las reglas del módulo de clientes. */
public class ClienteService {

    public static final String ERROR_GENERAL = "general";

    public enum ResultadoEliminacion {
        ELIMINADO, NO_EXISTE, CON_ALQUILERES
    }

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_DOCUMENTO = 30;
    private static final int MAX_TELEFONO = 20;

    private static final int MYSQL_ENTRADA_DUPLICADA = 1062;
    private static final int MYSQL_FILA_REFERENCIADA = 1451;

    private static final String DOCUMENTO_REPETIDO = "Ya existe un cliente con ese documento.";
    private static final String CLIENTE_INEXISTENTE = "El cliente ya no existe. Vuelve al listado.";

    private final ClienteDAO dao;

    public ClienteService() {
        this(new ClienteDAO());
    }

    public ClienteService(ClienteDAO dao) {
        this.dao = dao;
    }

    public List<Cliente> listar() throws SQLException {
        return dao.listar();
    }

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    /** Si el mapa vuelve vacío, el cliente se registró y tiene asignado su ID. */
    public Map<String, String> registrar(Cliente cliente) throws SQLException {
        normalizar(cliente);
        Map<String, String> errores = validar(cliente);
        if (!errores.isEmpty()) {
            return errores;
        }

        try {
            if (dao.existeDocumento(cliente.getDocumento(), 0)) {
                errores.put("documento", DOCUMENTO_REPETIDO);
            } else {
                cliente.setId(dao.insertar(cliente));
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_ENTRADA_DUPLICADA) {
                errores.put("documento", DOCUMENTO_REPETIDO);
            } else {
                throw ex;
            }
        }
        return errores;
    }

    /** Si el mapa vuelve vacío, los cambios quedaron guardados. */
    public Map<String, String> actualizar(Cliente cliente) throws SQLException {
        normalizar(cliente);
        Map<String, String> errores = validar(cliente);
        if (cliente.getId() <= 0) {
            errores.put(ERROR_GENERAL, CLIENTE_INEXISTENTE);
        }
        if (!errores.isEmpty()) {
            return errores;
        }

        try {
            if (dao.existeDocumento(cliente.getDocumento(), cliente.getId())) {
                errores.put("documento", DOCUMENTO_REPETIDO);
            } else if (!dao.actualizar(cliente)) {
                errores.put(ERROR_GENERAL, CLIENTE_INEXISTENTE);
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_ENTRADA_DUPLICADA) {
                errores.put("documento", DOCUMENTO_REPETIDO);
            } else {
                throw ex;
            }
        }
        return errores;
    }

    public ResultadoEliminacion eliminar(int id) throws SQLException {
        try {
            return dao.eliminar(id)
                    ? ResultadoEliminacion.ELIMINADO
                    : ResultadoEliminacion.NO_EXISTE;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_FILA_REFERENCIADA) {
                return ResultadoEliminacion.CON_ALQUILERES;
            }
            throw ex;
        }
    }

    /** Valida valores normalizados sin consultar la base de datos. */
    public Map<String, String> validar(Cliente cliente) {
        Map<String, String> errores = new LinkedHashMap<>();

        validarTexto(errores, "nombre", "El nombre es obligatorio.",
                "El nombre", cliente.getNombre(), MAX_NOMBRE);
        validarTexto(errores, "documento", "El documento es obligatorio.",
                "El documento", cliente.getDocumento(), MAX_DOCUMENTO);
        validarTexto(errores, "telefono", "El teléfono es obligatorio.",
                "El teléfono", cliente.getTelefono(), MAX_TELEFONO);

        return errores;
    }

    private static void validarTexto(Map<String, String> errores, String campo,
            String mensajeObligatorio, String sujeto, String valor, int maximo) {
        if (valor == null || valor.isEmpty()) {
            errores.put(campo, mensajeObligatorio);
        } else if (valor.length() > maximo) {
            errores.put(campo, sujeto + " no puede superar los " + maximo + " caracteres.");
        }
    }

    private static void normalizar(Cliente cliente) {
        cliente.setNombre(cliente.getNombre() == null ? "" : cliente.getNombre().trim());
        cliente.setDocumento(cliente.getDocumento() == null
                ? ""
                : cliente.getDocumento().trim().toUpperCase(Locale.ROOT));
        cliente.setTelefono(cliente.getTelefono() == null ? "" : cliente.getTelefono().trim());
    }
}
