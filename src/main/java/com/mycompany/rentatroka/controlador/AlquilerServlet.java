package com.mycompany.rentatroka.controlador;

import com.mycompany.rentatroka.modelo.Alquiler;
import com.mycompany.rentatroka.modelo.AlquilerDetalle;
import com.mycompany.rentatroka.servicio.AlquilerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Controlador del módulo de alquileres.
// GET muestra pantallas y nunca modifica datos; POST guarda, actualiza o elimina y redirige al listado.
@WebServlet(name = "AlquilerServlet", urlPatterns = "/alquileres")
public class AlquilerServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/alquileres/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/alquileres/formulario.jsp";

    private final AlquilerService servicio = new AlquilerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null || accion.isBlank()) {
            accion = "listar";
        }

        try {
            switch (accion) {
                case "listar" -> mostrarLista(request, response, servicio.listar());
                case "nuevo" -> mostrarFormulario(request, response, new Alquiler(), Map.of());
                case "editar" -> editar(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            falloDeBaseDeDatos(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        try {
            switch (accion) {
                case "guardar" -> guardar(request, response);
                case "actualizar" -> actualizar(request, response);
                case "eliminar" -> eliminar(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            falloDeBaseDeDatos(request, response, ex);
        }
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = Utilidades.aEntero(request.getParameter("id"));
        Optional<Alquiler> alquiler = servicio.buscarPorId(id);

        if (alquiler.isEmpty()) {
            Utilidades.avisar(request, Utilidades.AVISO_ERROR, "El alquiler que intentas editar ya no existe.");
            irAlListado(request, response);
            return;
        }
        mostrarFormulario(request, response, alquiler.get(), Map.of());
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Alquiler alquiler = leerFormulario(request);
        Map<String, String> errores = servicio.registrar(alquiler);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, alquiler, errores);
            return;
        }
        Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                "Alquiler registrado correctamente. " + resumenImporte(alquiler));
        irAlListado(request, response);
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Alquiler alquiler = leerFormulario(request);
        Map<String, String> errores = servicio.actualizar(alquiler);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, alquiler, errores);
            return;
        }
        Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                "Alquiler actualizado correctamente. " + resumenImporte(alquiler));
        irAlListado(request, response);
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = Utilidades.aEntero(request.getParameter("id"));

        switch (servicio.eliminar(id)) {
            case ELIMINADO -> Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                    "Alquiler eliminado correctamente.");
            case NO_EXISTE -> Utilidades.avisar(request, Utilidades.AVISO_ERROR,
                    "El alquiler ya no existe.");
        }
        irAlListado(request, response);
    }

    // Texto del importe, por ejemplo "3 días × $65.00 = $195.00".
    // Se calcula en el servidor con la tarifa guardada, no con lo que envía el navegador.
    private String resumenImporte(Alquiler alquiler) {
        long dias = AlquilerService.calcularDias(alquiler);
        String palabraDias = "días";
        if (dias == 1) {
            palabraDias = "día";
        }
        return dias + " " + palabraDias + " × $" + alquiler.getTarifaDiariaAplicada()
                + " = $" + AlquilerService.calcularTotal(alquiler) + ".";
    }

    // Arma el alquiler con lo que llegó del formulario
    private Alquiler leerFormulario(HttpServletRequest request) {
        Alquiler alquiler = new Alquiler();
        alquiler.setId(Utilidades.aEntero(request.getParameter("id")));
        alquiler.setClienteId(Utilidades.aEntero(request.getParameter("clienteId")));
        alquiler.setVehiculoId(Utilidades.aEntero(request.getParameter("vehiculoId")));
        alquiler.setFechaInicio(aFecha(request.getParameter("fechaInicio")));
        alquiler.setFechaFin(aFecha(request.getParameter("fechaFin")));
        return alquiler;
    }

    private void mostrarLista(HttpServletRequest request, HttpServletResponse response,
            List<AlquilerDetalle> alquileres) throws ServletException, IOException {
        Utilidades.pasarAvisosALaVista(request);
        request.setAttribute("alquileres", alquileres);
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    // El formulario también necesita los clientes y vehículos para los select
    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
            Alquiler alquiler, Map<String, String> errores) throws SQLException, ServletException, IOException {
        request.setAttribute("alquiler", alquiler);
        request.setAttribute("errores", errores);
        request.setAttribute("clientes", servicio.listarClientes());
        request.setAttribute("vehiculos", servicio.listarVehiculos());

        // Al editar, la vista necesita el vehículo y la tarifa guardados
        // para mostrar el importe estimado correcto.
        if (alquiler.getId() > 0) {
            Optional<Alquiler> guardado = servicio.buscarPorId(alquiler.getId());
            if (guardado.isPresent()) {
                request.setAttribute("vehiculoGuardado", guardado.get().getVehiculoId());
                request.setAttribute("tarifaGuardada", guardado.get().getTarifaDiariaAplicada());
            }
        }

        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    private void falloDeBaseDeDatos(HttpServletRequest request, HttpServletResponse response, SQLException ex)
            throws ServletException, IOException {
        getServletContext().log("No se pudo consultar MySQL en el módulo de alquileres.", ex);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.setAttribute("error", "No pudimos conectar con la base de datos. Inténtalo de nuevo en unos momentos.");
        mostrarLista(request, response, List.of());
    }

    private void irAlListado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/alquileres");
    }

    // Convierte el texto yyyy-MM-dd (lo que manda <input type="date">) a fecha; null si no es válida
    private LocalDate aFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            LocalDate fecha = LocalDate.parse(texto.trim());
            // MySQL DATE solo acepta años de 1000 a 9999
            if (fecha.getYear() < 1000 || fecha.getYear() > 9999) {
                return null;
            }
            return fecha;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
