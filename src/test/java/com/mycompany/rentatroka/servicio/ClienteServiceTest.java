package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.ClienteDAO;
import com.mycompany.rentatroka.modelo.Cliente;
import com.mycompany.rentatroka.servicio.ClienteService.ResultadoEliminacion;
import java.sql.SQLException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {

    @Test
    void normalizaYRegistraElCliente() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        Cliente cliente = cliente("  Ana Martínez ", " doc-009 ", " 7000-1009 ");

        Map<String, String> errores = new ClienteService(dao).registrar(cliente);

        assertTrue(errores.isEmpty());
        assertEquals("Ana Martínez", dao.ultimoGuardado.getNombre());
        assertEquals("DOC-009", dao.ultimoGuardado.getDocumento());
        assertEquals("7000-1009", dao.ultimoGuardado.getTelefono());
        assertEquals(99, cliente.getId());
    }

    @Test
    void rechazaDatosInvalidosSinConsultarLaBase() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        ClienteService servicio = new ClienteService(dao);

        Map<String, String> vacios = servicio.registrar(cliente(" ", null, ""));
        assertEquals(java.util.Set.of("nombre", "documento", "telefono"), vacios.keySet());

        assertTrue(servicio.registrar(cliente("x".repeat(101), "DOC-1", "7000-1001")).containsKey("nombre"));
        assertTrue(servicio.registrar(cliente("Ana", "DOC <1>", "7000-1001")).containsKey("documento"));
        assertTrue(servicio.registrar(cliente("Ana", "DOC-1", "hola")).containsKey("telefono"));
        assertTrue(servicio.registrar(cliente("Ana", "DOC-1", "123")).containsKey("telefono"));
        assertEquals(0, dao.consultas);
    }

    @Test
    void aceptaTelefonosConFormatosComunes() throws SQLException {
        ClienteService servicio = new ClienteService(new ClienteDAOPrueba());
        for (String telefono : new String[] {"7000-1001", "+503 7000 1001", "(503) 2222-3333"}) {
            assertFalse(servicio.registrar(cliente("Ana", "DOC-1", telefono)).containsKey("telefono"),
                    "Debió aceptar el teléfono: " + telefono);
        }
    }

    @Test
    void noRegistraUnDocumentoRepetido() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        dao.documentoExistente = "DOC-001";

        Map<String, String> errores = new ClienteService(dao).registrar(cliente("Ana", "doc-001", "7000-1001"));

        assertTrue(errores.containsKey("documento"));
        assertNull(dao.ultimoGuardado);
    }

    @Test
    void explicaQueUnClienteConAlquileresNoSePuedeEliminar() throws SQLException {
        ClienteDAOPrueba dao = new ClienteDAOPrueba();
        ClienteService servicio = new ClienteService(dao);

        assertEquals(ResultadoEliminacion.ELIMINADO, servicio.eliminar(3));

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

    // DAO en memoria para probar el servicio sin MySQL
    private static class ClienteDAOPrueba extends ClienteDAO {
        private int consultas;
        private String documentoExistente;
        private Cliente ultimoGuardado;
        private SQLException errorAlEliminar;

        @Override
        public boolean existeDocumento(String documento, int excluirId) {
            consultas++;
            return documento.equals(documentoExistente);
        }

        @Override
        public int insertar(Cliente cliente) {
            consultas++;
            ultimoGuardado = cliente;
            return 99;
        }

        @Override
        public boolean eliminar(int id) throws SQLException {
            consultas++;
            if (errorAlEliminar != null) {
                throw errorAlEliminar;
            }
            return true;
        }
    }
}
