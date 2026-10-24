package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.ClienteDAO;
import com.mycompany.rentatroka.modelo.Cliente;
import com.mycompany.rentatroka.servicio.ClienteService.ResultadoEliminacion;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {

    @Test
    void normalizaYRegistraElCliente() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        Cliente cliente = cliente("  Ana María López  ", " a01234567 ", " 7777-1234 ");

        Map<String, String> errores = new ClienteService(dao).registrar(cliente);

        assertTrue(errores.isEmpty());
        assertEquals("Ana María López", dao.ultimoGuardado.getNombre());
        assertEquals("A01234567", dao.ultimoGuardado.getDocumento());
        assertEquals("7777-1234", dao.ultimoGuardado.getTelefono());
        assertEquals(99, cliente.getId());
    }

    @Test
    void rechazaCamposObligatoriosYLongitudesSinConsultarLaBase() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        ClienteService servicio = new ClienteService(dao);

        Map<String, String> errores = servicio.registrar(cliente("  ", "", null));
        assertEquals(java.util.Set.of("nombre", "documento", "telefono"), errores.keySet());
        assertEquals("El nombre es obligatorio.", errores.get("nombre"));
        assertEquals("El documento es obligatorio.", errores.get("documento"));
        assertEquals("El teléfono es obligatorio.", errores.get("telefono"));

        assertTrue(servicio.registrar(cliente("N".repeat(101), "ABC123", "7777")).containsKey("nombre"));
        assertTrue(servicio.registrar(cliente("Ana", "D".repeat(31), "7777")).containsKey("documento"));
        assertTrue(servicio.registrar(cliente("Ana", "ABC123", "7".repeat(21))).containsKey("telefono"));
        assertEquals(0, dao.consultas);
    }

    @Test
    void rechazaUnDocumentoQueYaPerteneceAOtroCliente() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        dao.documentoExistente = "A01234567";

        Map<String, String> errores = new ClienteService(dao)
                .registrar(cliente("Ana", " a01234567 ", "7777"));

        assertEquals("Ya existe un cliente con ese documento.", errores.get("documento"));
        assertNull(dao.ultimoGuardado);
    }

    @Test
    void unaColisionDeDocumentoAlGuardarSeTraduceEnErrorDeCampo() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        dao.errorAlGuardar = new SQLException("Duplicate entry", "23000", 1062);

        Map<String, String> errores = new ClienteService(dao)
                .registrar(cliente("Ana", "A01234567", "7777"));

        assertTrue(errores.containsKey("documento"));
    }

    @Test
    void otrosErroresDeMySqlSePropagan() {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        dao.errorAlGuardar = new SQLException("Conexión no disponible", "08S01", 0);

        assertThrows(SQLException.class,
                () -> new ClienteService(dao).registrar(cliente("Ana", "A01234567", "7777")));
    }

    @Test
    void actualizarPermiteConservarElDocumentoDelMismoCliente() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        dao.documentoExistente = "A01234567";
        dao.idDocumentoExistente = 7;

        Cliente cliente = cliente("Ana María", "a01234567", "2222");
        cliente.setId(7);
        Map<String, String> errores = new ClienteService(dao).actualizar(cliente);

        assertTrue(errores.isEmpty());
        assertEquals(7, dao.ultimoIdExcluido);
        assertEquals("A01234567", dao.ultimoGuardado.getDocumento());
    }

    @Test
    void noPermiteActualizarConDocumentoDeOtroClienteNiUnIdInvalido() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        dao.documentoExistente = "B12345678";
        dao.idDocumentoExistente = 3;
        ClienteService servicio = new ClienteService(dao);

        Cliente repetido = cliente("Ana", "B12345678", "2222");
        repetido.setId(7);
        assertTrue(servicio.actualizar(repetido).containsKey("documento"));

        Cliente sinId = cliente("Ana", "A12345678", "2222");
        assertTrue(servicio.actualizar(sinId).containsKey(ClienteService.ERROR_GENERAL));
    }

    @Test
    void informaCuandoUnClienteTieneAlquileresYNoPuedeEliminarse() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        ClienteService servicio = new ClienteService(dao);

        assertEquals(ResultadoEliminacion.ELIMINADO, servicio.eliminar(4));

        dao.eliminacionAfectaFilas = false;
        assertEquals(ResultadoEliminacion.NO_EXISTE, servicio.eliminar(4));

        dao.errorAlEliminar = new SQLException("Cannot delete or update a parent row", "23000", 1451);
        assertEquals(ResultadoEliminacion.CON_ALQUILERES, servicio.eliminar(1));

        dao.errorAlEliminar = new SQLException("Conexión no disponible", "08S01", 0);
        assertThrows(SQLException.class, () -> servicio.eliminar(1));
    }

    private static Cliente cliente(String nombre, String documento, String telefono) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setDocumento(documento);
        cliente.setTelefono(telefono);
        return cliente;
    }

    /** DAO en memoria para probar las reglas sin depender de una base MySQL. */
    private static class ClienteDAOPrueba extends ClienteDAO {
        private int consultas;
        private String documentoExistente;
        private int idDocumentoExistente = -1;
        private int ultimoIdExcluido = -1;
        private Cliente ultimoGuardado;
        private SQLException errorAlGuardar;
        private SQLException errorAlEliminar;
        private boolean actualizacionAfectaFilas = true;
        private boolean eliminacionAfectaFilas = true;

        @Override
        public List<Cliente> listar() {
            return List.of();
        }

        @Override
        public boolean existeDocumento(String documento, int excluirId) {
            consultas++;
            ultimoIdExcluido = excluirId;
            return documentoExistente != null
                    && documentoExistente.equalsIgnoreCase(documento)
                    && idDocumentoExistente != excluirId;
        }

        @Override
        public int insertar(Cliente cliente) throws SQLException {
            consultas++;
            if (errorAlGuardar != null) {
                throw errorAlGuardar;
            }
            ultimoGuardado = cliente;
            return 99;
        }

        @Override
        public boolean actualizar(Cliente cliente) throws SQLException {
            consultas++;
            if (errorAlGuardar != null) {
                throw errorAlGuardar;
            }
            ultimoGuardado = cliente;
            return actualizacionAfectaFilas;
        }

        @Override
        public boolean eliminar(int id) throws SQLException {
            consultas++;
            if (errorAlEliminar != null) {
                throw errorAlEliminar;
            }
            return eliminacionAfectaFilas;
        }
    }
}
