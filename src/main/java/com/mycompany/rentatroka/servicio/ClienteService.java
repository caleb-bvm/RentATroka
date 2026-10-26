package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.ClienteDAO;
import com.mycompany.rentatroka.modelo.Cliente;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Reglas del módulo de clientes: limpia y valida los datos y traduce los errores de MySQL.
public class ClienteService {

    // Clave del mapa de errores para los problemas que no son de un campo
    public static final String ERROR_GENERAL = "general";

    public enum ResultadoEliminacion {
        ELIMINADO,
        NO_EXISTE,
        CON_ALQUILERES
    }

    // Códigos de error de MySQL
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

    // Si el mapa de errores viene vacío, el cliente quedó registrado con su id
    public Map<String, String> registrar(Cliente cliente) throws SQLException {
        limpiar(cliente);
        Map<String, String> errores = validar(cliente);
        if (!errores.isEmpty()) {
            return errores;
        }

        if (dao.existeDocumento(cliente.getDocumento(), 0)) {
            errores.put("documento", DOCUMENTO_REPETIDO);
            return errores;
        }

        try {
            cliente.setId(dao.insertar(cliente));
        } catch (SQLException ex) {
            // otra petición registró el mismo documento justo antes
            if (ex.getErrorCode() == MYSQL_ENTRADA_DUPLICADA) {
                errores.put("documento", DOCUMENTO_REPETIDO);
            } else {
                throw ex;
            }
        }
        return errores;
    }

    // Si el mapa de errores viene vacío, los cambios quedaron guardados
    public Map<String, String> actualizar(Cliente cliente) throws SQLException {
        limpiar(cliente);
        Map<String, String> errores = validar(cliente);
        if (cliente.getId() <= 0) {
            errores.put(ERROR_GENERAL, CLIENTE_INEXISTENTE);
        }
        if (!errores.isEmpty()) {
            return errores;
        }

        if (dao.existeDocumento(cliente.getDocumento(), cliente.getId())) {
            errores.put("documento", DOCUMENTO_REPETIDO);
            return errores;
        }

        try {
            boolean actualizado = dao.actualizar(cliente);
            if (!actualizado) {
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
            boolean eliminado = dao.eliminar(id);
            if (eliminado) {
                return ResultadoEliminacion.ELIMINADO;
            }
            return ResultadoEliminacion.NO_EXISTE;
        } catch (SQLException ex) {
            // MySQL no deja borrar un cliente que tiene alquileres
            if (ex.getErrorCode() == MYSQL_FILA_REFERENCIADA) {
                return ResultadoEliminacion.CON_ALQUILERES;
            }
            throw ex;
        }
    }

    // Valida los campos del formulario sin consultar la base de datos
    public Map<String, String> validar(Cliente cliente) {
        Map<String, String> errores = new LinkedHashMap<>();

        String nombre = cliente.getNombre();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "El nombre es obligatorio.");
        } else if (nombre.length() > 100) {
            errores.put("nombre", "El nombre no puede superar 100 caracteres.");
        }

        String documento = cliente.getDocumento();
        if (documento == null || documento.isBlank()) {
            errores.put("documento", "El documento es obligatorio.");
        } else if (documento.length() > 30) {
            errores.put("documento", "El documento no puede superar 30 caracteres.");
        } else if (!documento.matches("[A-Z0-9-]+")) {
            errores.put("documento", "El documento solo admite letras, números y guiones.");
        }

        String telefono = cliente.getTelefono();
        if (telefono == null || telefono.isBlank()) {
            errores.put("telefono", "El teléfono es obligatorio.");
        } else if (telefono.length() > 20) {
            errores.put("telefono", "El teléfono no puede superar 20 caracteres.");
        } else if (!telefono.matches("[0-9 ()+-]{7,20}")) {
            errores.put("telefono", "El teléfono debe tener al menos 7 caracteres: números, espacios, guiones, paréntesis o +.");
        }

        return errores;
    }

    // Quita espacios sobrantes y guarda el documento en mayúsculas
    private void limpiar(Cliente cliente) {
        if (cliente.getNombre() == null) {
            cliente.setNombre("");
        }
        if (cliente.getDocumento() == null) {
            cliente.setDocumento("");
        }
        if (cliente.getTelefono() == null) {
            cliente.setTelefono("");
        }
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setDocumento(cliente.getDocumento().trim().toUpperCase());
        cliente.setTelefono(cliente.getTelefono().trim());
    }
}
