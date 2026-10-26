package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.ClienteDAO;
import com.mycompany.rentatroka.modelo.Cliente;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class ClienteService {

    public static final String ERROR_GENERAL = "general";

    public enum ResultadoEliminacion {
        ELIMINADO,
        NO_EXISTE,
        CON_ALQUILERES
    }

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_DOCUMENTO = 30;
    private static final int MAX_TELEFONO = 20;

    // documento ya normalizado en mayúsculas: letras, números y guiones
    private static final Pattern DOCUMENTO = Pattern.compile("[A-Z0-9-]+");

    // teléfono: dígitos con espacios, guiones o paréntesis; puede iniciar con +
    private static final Pattern TELEFONO = Pattern.compile("\\+?\\(?[0-9][0-9 ()-]{6,}");

    private static final int MYSQL_DOCUMENTO_DUPLICADO = 1062;
    private static final int MYSQL_FILA_REFERENCIADA = 1451;

    private static final String DOCUMENTO_REPETIDO =
            "Ya existe un cliente con ese documento.";

    private static final String CLIENTE_INEXISTENTE =
            "El cliente ya no existe. Vuelve al listado.";

    private final ClienteDAO dao;

    public ClienteService() {
        this(new ClienteDAO());
    }

    public ClienteService(ClienteDAO dao) {
        this.dao = dao;
    }

    // listado de clientes
    public List<Cliente> listar() throws SQLException {
        return dao.listar();
    }

    // busca cliente con el id
    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // registrar cliente despues de validar los campos
    public Map<String, String> registrar(Cliente cliente)
            throws SQLException {

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
            errores.putAll(traducirDuplicado(ex));
        }

        return errores;
    }

    // actualizar cliente existente
    public Map<String, String> actualizar(Cliente cliente)
            throws SQLException {

        normalizar(cliente);

        Map<String, String> errores = validar(cliente);

        if (cliente.getId() <= 0) {
            errores.put(ERROR_GENERAL, CLIENTE_INEXISTENTE);
        }

        if (!errores.isEmpty()) {
            return errores;
        }

        try {
            if (dao.existeDocumento(
                    cliente.getDocumento(), cliente.getId())) {

                errores.put("documento", DOCUMENTO_REPETIDO);

            } else if (!dao.actualizar(cliente)) {
                errores.put(ERROR_GENERAL, CLIENTE_INEXISTENTE);
            }
        } catch (SQLException ex) {
            errores.putAll(traducirDuplicado(ex));
        }

        return errores;
    }

    // eliminar cliente
    public ResultadoEliminacion eliminar(int id)
            throws SQLException {

        try {
            return dao.eliminar(id)
                    ? ResultadoEliminacion.ELIMINADO
                    : ResultadoEliminacion.NO_EXISTE;

        } catch (SQLException ex) {

            // No eliminar clientes que tengan alquileres asociados.
            if (ex.getErrorCode() == MYSQL_FILA_REFERENCIADA) {
                return ResultadoEliminacion.CON_ALQUILERES;
            }

            throw ex;
        }
    }

    // validaciones en los campos del formulario
    public Map<String, String> validar(Cliente cliente) {

        Map<String, String> errores = new LinkedHashMap<>();

        validarTexto(
                errores,
                "nombre",
                "El nombre es obligatorio.",
                "El nombre",
                cliente.getNombre(),
                MAX_NOMBRE
        );

        validarTexto(
                errores,
                "documento",
                "El documento es obligatorio.",
                "El documento",
                cliente.getDocumento(),
                MAX_DOCUMENTO
        );

        validarTexto(
                errores,
                "telefono",
                "El teléfono es obligatorio.",
                "El teléfono",
                cliente.getTelefono(),
                MAX_TELEFONO
        );

        validarFormato(
                errores,
                "documento",
                "El documento solo admite letras, números y guiones.",
                cliente.getDocumento(),
                DOCUMENTO
        );

        validarFormato(
                errores,
                "telefono",
                "El teléfono debe tener al menos 7 dígitos y solo admite números, espacios, guiones, paréntesis y +.",
                cliente.getTelefono(),
                TELEFONO
        );

        return errores;
    }

    // solo revisa el formato si el campo no tiene ya otro error
    private static void validarFormato(
            Map<String, String> errores,
            String campo,
            String mensaje,
            String valor,
            Pattern formato) {

        if (!errores.containsKey(campo)
                && !formato.matcher(valor).matches()) {
            errores.put(campo, mensaje);
        }
    }

    private static void validarTexto(
            Map<String, String> errores,
            String campo,
            String obligatorio,
            String sujeto,
            String valor,
            int maximo) {

        if (valor == null || valor.isBlank()) {
            errores.put(campo, obligatorio);

        } else if (valor.length() > maximo) {
            errores.put(
                    campo,
                    sujeto + " no puede superar "
                            + maximo + " caracteres."
            );
        }
    }

    // error de clave sql
    private static Map<String, String> traducirDuplicado(
            SQLException ex) throws SQLException {

        if (ex.getErrorCode() != MYSQL_DOCUMENTO_DUPLICADO) {
            throw ex;
        }

        return Map.of("documento", DOCUMENTO_REPETIDO);
    }

    // se eliminan los espacios sobrantes
    private static void normalizar(Cliente cliente) {

        cliente.setNombre(
                cliente.getNombre() == null
                        ? ""
                        : cliente.getNombre().trim()
        );

        cliente.setDocumento(
                cliente.getDocumento() == null
                        ? ""
                        : cliente.getDocumento().trim()
                                .toUpperCase(Locale.ROOT)
        );

        cliente.setTelefono(
                cliente.getTelefono() == null
                        ? ""
                        : cliente.getTelefono().trim()
        );
    }
}