package com.mycompany.rentatroka.servicio;

import com.mycompany.rentatroka.modelo.Alquiler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlquilerServiceTest {

    // validar() no toca la base de datos, por eso los DAO pueden ser null
    private final AlquilerService servicio = new AlquilerService(null, null, null);

    private static Alquiler alquiler(int cliente, int vehiculo, String inicio, String fin) {
        Alquiler a = new Alquiler();
        a.setClienteId(cliente);
        a.setVehiculoId(vehiculo);
        a.setFechaInicio(inicio == null ? null : LocalDate.parse(inicio));
        a.setFechaFin(fin == null ? null : LocalDate.parse(fin));
        return a;
    }

    @Test
    void exigeClienteVehiculoYFechas() {
        Map<String, String> errores = servicio.validar(alquiler(0, 0, null, null), false);

        assertEquals(java.util.Set.of("cliente", "vehiculo", "fechaInicio", "fechaFin"), errores.keySet());
    }

    @Test
    void laFechaFinalDebeSerPosteriorALaInicial() {
        assertTrue(servicio.validar(alquiler(1, 1, "2026-11-05", "2026-11-05"), false).containsKey("fechaFin"));
        assertTrue(servicio.validar(alquiler(1, 1, "2026-11-05", "2026-11-02"), false).containsKey("fechaFin"));
        assertTrue(servicio.validar(alquiler(1, 1, "2026-11-02", "2026-11-05"), false).isEmpty());
    }

    @Test
    void unAlquilerNuevoNoPuedeEmpezarEnElPasadoPeroUnoExistenteSiSePuedeEditar() {
        Alquiler viejo = alquiler(1, 1, "2020-01-01", "2020-01-03");

        assertTrue(servicio.validar(viejo, true).containsKey("fechaInicio"));
        assertTrue(servicio.validar(viejo, false).isEmpty());
    }

    @Test
    void laFechaFinalEsExclusivaYElImporteEsDiasPorTarifa() {
        Alquiler a = alquiler(1, 1, "2026-11-02", "2026-11-05");
        a.setTarifaDiariaAplicada(new BigDecimal("65.00"));

        assertEquals(3, AlquilerService.calcularDias(a));
        assertEquals(new BigDecimal("195.00"), AlquilerService.calcularTotal(a));
    }
}