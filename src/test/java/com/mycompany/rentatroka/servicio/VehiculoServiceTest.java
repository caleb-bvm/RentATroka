package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.dao.VehiculoDAO;
import com.mycompany.rentatroka.modelo.Vehiculo;
import com.mycompany.rentatroka.servicio.VehiculoService.ResultadoEliminacion;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehiculoServiceTest {

    @Test
    void normalizaYRegistraElVehiculo() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        Vehiculo vehiculo = vehiculo(" p004-004 ", "  Toyota ", " Hilux", "65.5");

        Map<String, String> errores = new VehiculoService(dao).registrar(vehiculo);

        assertTrue(errores.isEmpty());
        assertEquals("P004-004", dao.ultimoGuardado.getPlaca());
        assertEquals("Toyota", dao.ultimoGuardado.getMarca());
        assertEquals("Hilux", dao.ultimoGuardado.getModelo());
        assertEquals(new BigDecimal("65.50"), dao.ultimoGuardado.getTarifaDiaria());
        assertEquals(99, vehiculo.getId());
    }

    @Test
    void rechazaDatosInvalidosSinConsultarLaBase() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        VehiculoService servicio = new VehiculoService(dao);

        Map<String, String> vacios = servicio.registrar(vehiculo("  ", "", null, null));
        assertEquals(java.util.Set.of("placa", "marca", "modelo", "tarifaDiaria"), vacios.keySet());
        assertEquals("La placa es obligatoria.", vacios.get("placa"));

        assertTrue(servicio.registrar(vehiculo("P001-001", "x".repeat(51), "Hilux", "10")).containsKey("marca"));
        assertTrue(servicio.registrar(vehiculo("P001-001", "Toyota", "x".repeat(51), "10")).containsKey("modelo"));
        assertEquals(0, dao.consultas);
    }

    @Test
    void validaElFormatoDeLaPlaca() throws SQLException {
        VehiculoService servicio = new VehiculoService(new VehiculoDAOPrueba());
        for (String invalida : new String[] {"AB", "ABCDEFGHIJKLMNOP", "P001 001", "P001_001", "P'; DROP", "PLACÁ-1"}) {
            assertTrue(servicio.registrar(vehiculo(invalida, "Toyota", "Hilux", "10")).containsKey("placa"),
                    "Debió rechazar la placa: " + invalida);
        }
        for (String valida : new String[] {"P001-001", "abc123", "C-123456"}) {
            assertFalse(servicio.registrar(vehiculo(valida, "Toyota", "Hilux", "10")).containsKey("placa"),
                    "Debió aceptar la placa: " + valida);
        }
    }

    @Test
    void laTarifaDebeSerPositivaYTenerComoMaximoDosDecimales() throws SQLException {
        assertEquals(new BigDecimal("65.50"), VehiculoService.tarifaDesdeTexto(" 65.5 "));
        assertEquals(new BigDecimal("10.00"), VehiculoService.tarifaDesdeTexto("10"));
        assertEquals(new BigDecimal("99999999.99"), VehiculoService.tarifaDesdeTexto("99999999.99"));
        assertNull(VehiculoService.tarifaDesdeTexto("65.555"));
        assertNull(VehiculoService.tarifaDesdeTexto("100000000"));
        assertNull(VehiculoService.tarifaDesdeTexto("abc"));
        assertNull(VehiculoService.tarifaDesdeTexto("1e3"));
        assertNull(VehiculoService.tarifaDesdeTexto(""));
        assertNull(VehiculoService.tarifaDesdeTexto(null));

        VehiculoService servicio = new VehiculoService(new VehiculoDAOPrueba());
        assertEquals("La tarifa diaria debe ser mayor que 0.",
                servicio.registrar(vehiculo("P001-001", "Toyota", "Hilux", "0")).get("tarifaDiaria"));
        assertEquals("La tarifa diaria debe ser mayor que 0.",
                servicio.registrar(vehiculo("P001-001", "Toyota", "Hilux", "-5")).get("tarifaDiaria"));
    }

    @Test
    void noRegistraUnaPlacaRepetida() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        dao.placaExistente = "P001-001";

        Map<String, String> errores = new VehiculoService(dao)
                .registrar(vehiculo("p001-001", "Toyota", "Hilux", "65"));

        assertEquals("Ya existe un vehículo con esa placa.", errores.get("placa"));
        assertNull(dao.ultimoGuardado);
    }

    @Test
    void siOtraPeticionRegistraLaMismaPlacaSeInformaComoErrorDeCampo() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        dao.errorAlGuardar = new SQLException("Duplicate entry", "23000", 1062);

        Map<String, String> errores = new VehiculoService(dao)
                .registrar(vehiculo("P009-009", "Toyota", "Hilux", "65"));

        assertTrue(errores.containsKey("placa"));
    }

    @Test
    void otrosFallosDeMySqlSePropagan() {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        dao.errorAlGuardar = new SQLException("Conexión no disponible", "08S01", 0);
        assertThrows(SQLException.class,
                () -> new VehiculoService(dao).registrar(vehiculo("P009-009", "Toyota", "Hilux", "65")));
    }

    @Test
    void alEditarSeExcluyeElPropioVehiculoDeLaPlacaUnica() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        Vehiculo vehiculo = vehiculo("P001-001", "Toyota", "Hilux", "70");
        vehiculo.setId(1);

        Map<String, String> errores = new VehiculoService(dao).actualizar(vehiculo);

        assertTrue(errores.isEmpty());
        assertEquals(1, dao.ultimoIdExcluido);
        assertEquals(new BigDecimal("70.00"), dao.ultimoGuardado.getTarifaDiaria());
    }

    @Test
    void actualizarUnVehiculoQueYaNoExisteDevuelveUnErrorGeneral() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        dao.actualizacionAfectaFilas = false;
        Vehiculo vehiculo = vehiculo("P001-001", "Toyota", "Hilux", "70");
        vehiculo.setId(7);
        assertTrue(new VehiculoService(dao).actualizar(vehiculo).containsKey(VehiculoService.ERROR_GENERAL));

        Vehiculo sinId = vehiculo("P001-001", "Toyota", "Hilux", "70");
        assertTrue(new VehiculoService(dao).actualizar(sinId).containsKey(VehiculoService.ERROR_GENERAL));
    }

    @Test
    void explicaQueUnVehiculoConAlquileresNoSePuedeEliminar() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        VehiculoService servicio = new VehiculoService(dao);

        assertEquals(ResultadoEliminacion.ELIMINADO, servicio.eliminar(3));

        dao.eliminacionAfectaFilas = false;
        assertEquals(ResultadoEliminacion.NO_EXISTE, servicio.eliminar(3));

        dao.errorAlEliminar = new SQLException("Cannot delete or update a parent row", "23000", 1451);
        assertEquals(ResultadoEliminacion.CON_ALQUILERES, servicio.eliminar(1));

        dao.errorAlEliminar = new SQLException("Conexión no disponible", "08S01", 0);
        assertThrows(SQLException.class, () -> servicio.eliminar(1));
    }

    @Test
    void listaTodosOBuscaSegunElCriterio() throws SQLException {
        VehiculoDAOPrueba dao = new VehiculoDAOPrueba();
        VehiculoService servicio = new VehiculoService(dao);

        servicio.listar(null);
        servicio.listar("   ");
        assertEquals(2, dao.listados);
        assertNull(dao.ultimaBusqueda);

        servicio.listar("  hilux ");
        assertEquals("hilux", dao.ultimaBusqueda);

        servicio.listar("x".repeat(80));
        assertEquals(50, dao.ultimaBusqueda.length());
    }

    private static Vehiculo vehiculo(String placa, String marca, String modelo, String tarifa) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(placa);
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setTarifaDiaria(VehiculoService.tarifaDesdeTexto(tarifa));
        return vehiculo;
    }

    // DAO en memoria: registra lo que recibe y permite simular los errores del gestor
    
    private static class VehiculoDAOPrueba extends VehiculoDAO {
        private int consultas;
        private int listados;
        private String ultimaBusqueda;
        private String placaExistente;
        private int ultimoIdExcluido = -1;
        private Vehiculo ultimoGuardado;
        private SQLException errorAlGuardar;
        private SQLException errorAlEliminar;
        private boolean actualizacionAfectaFilas = true;
        private boolean eliminacionAfectaFilas = true;

        @Override
        public List<Vehiculo> listar() {
            listados++;
            return List.of();
        }

        @Override
        public List<Vehiculo> buscar(String termino) {
            ultimaBusqueda = termino;
            return List.of();
        }

        @Override
        public boolean existePlaca(String placa, int excluirId) {
            consultas++;
            ultimoIdExcluido = excluirId;
            return placa.equals(placaExistente);
        }

        @Override
        public int insertar(Vehiculo vehiculo) throws SQLException {
            consultas++;
            if (errorAlGuardar != null) {
                throw errorAlGuardar;
            }
            ultimoGuardado = vehiculo;
            return 99;
        }

        @Override
        public boolean actualizar(Vehiculo vehiculo) throws SQLException {
            consultas++;
            if (errorAlGuardar != null) {
                throw errorAlGuardar;
            }
            ultimoGuardado = vehiculo;
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
