package com.mycompany.rentatroka.controlador;

import com.mycompany.rentatroka.modelo.Alquiler;
import com.mycompany.rentatroka.modelo.AlquilerDetalle;
import com.mycompany.rentatroka.servicio.AlquilerService;
import com.mycompany.rentatroka.servicio.AlquilerService.ResultadoEliminacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "AlquilerServlet", urlPatterns = "/alquileres")
public class AlquilerServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/alquileres/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/alquileres/formulario.jsp";

    private static final String AVISO_EXITO = "mensajeExito";
    private static final String AVISO_ERROR = "mensajeError";

    private final AlquilerService servicio = new AlquilerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        try {
            switch (accion == null || accion.isBlank() ? "listar" : accion) {

                case "listar" ->
                    listar(request, response);

                case "nuevo" ->
                    mostrarFormulario(request, response, new Alquiler(), Map.of());

                case "editar" ->
                    editar(request, response);

                default ->
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException ex) {
            falloDeBaseDeDatos(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        try {
            switch (accion == null ? "" : accion) {

                case "guardar" ->
                    guardar(request, response);

                case "actualizar" ->
                    actualizar(request, response);

                case "eliminar" ->
                    eliminar(request, response);

                default ->
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException ex) {
            falloDeBaseDeDatos(request, response, ex);
        }
    }

    // ver alquileres registrados (JOIN)
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        mostrarLista(request, response, servicio.listar());
    }

    // buscar el alquiler a editar
    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        int id = entero(request.getParameter("id"));

        Optional<Alquiler> alquiler = servicio.buscarPorId(id);

        if (alquiler.isEmpty()) {
            avisar(request, AVISO_ERROR, "El alquiler que intentas editar ya no existe.");
            redirigirAlListado(request, response);
            return;
        }

        mostrarFormulario(request, response, alquiler.get(), Map.of());
    }

    // registrar alquiler nuevo
    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Alquiler alquiler = leerFormulario(request);

        Map<String, String> errores = servicio.registrar(alquiler);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, alquiler, errores);
            return;
        }

        avisar(request, AVISO_EXITO, "Alquiler registrado correctamente. " + resumenImporte(alquiler));
        redirigirAlListado(request, response);
    }

    // actualizar un alquiler existente
    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Alquiler alquiler = leerFormulario(request);

        Map<String, String> errores = servicio.actualizar(alquiler);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, alquiler, errores);
            return;
        }

        avisar(request, AVISO_EXITO, "Alquiler actualizado correctamente. " + resumenImporte(alquiler));
        redirigirAlListado(request, response);
    }

    // eliminar alquiler
    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = entero(request.getParameter("id"));

        ResultadoEliminacion resultado = servicio.eliminar(id);

        switch (resultado) {

            case ELIMINADO ->
                avisar(request, AVISO_EXITO, "Alquiler eliminado correctamente.");

            case NO_EXISTE ->
                avisar(request, AVISO_ERROR, "El alquiler ya no existe.");
        }

        redirigirAlListado(request, response);
    }

    // el importe se calcula en el servidor con la tarifa que quedó guardada, no con lo que envía el navegador
    private static String resumenImporte(Alquiler alquiler) {
        long dias = AlquilerService.calcularDias(alquiler);

        return String.format(Locale.US, "%d %s × $%.2f = $%.2f.",
                dias,
                dias == 1 ? "día" : "días",
                alquiler.getTarifaDiariaAplicada(),
                AlquilerService.calcularTotal(alquiler));
    }

    // crear el objeto alquiler en base al formulario
    private Alquiler leerFormulario(HttpServletRequest request) {

        Alquiler alquiler = new Alquiler();

        alquiler.setId(entero(request.getParameter("id")));
        alquiler.setClienteId(entero(request.getParameter("clienteId")));
        alquiler.setVehiculoId(entero(request.getParameter("vehiculoId")));
        alquiler.setFechaInicio(fecha(request.getParameter("fechaInicio")));
        alquiler.setFechaFin(fecha(request.getParameter("fechaFin")));

        return alquiler;
    }

    private void mostrarLista(HttpServletRequest request, HttpServletResponse response,
            List<AlquilerDetalle> alquileres) throws ServletException, IOException {

        trasladarAvisos(request);

        request.setAttribute("alquileres", alquileres);

        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    // formulario: manda también los clientes y vehículos para los select
    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
            Alquiler alquiler, Map<String, String> errores)
            throws SQLException, ServletException, IOException {

        request.setAttribute("alquiler", alquiler);
        request.setAttribute("errores", errores);
        request.setAttribute("clientes", servicio.listarClientes());
        request.setAttribute("vehiculos", servicio.listarVehiculos());

        // Al editar, la vista necesita saber qué vehículo y tarifa tiene guardados
        // para mostrar el importe estimado correcto.
        if (alquiler.getId() > 0) {
            servicio.buscarPorId(alquiler.getId()).ifPresent(guardado -> {
                request.setAttribute("vehiculoGuardado", guardado.getVehiculoId());
                request.setAttribute("tarifaGuardada", guardado.getTarifaDiariaAplicada());
            });
        }

        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    // mensaje de error en la base de datos
    private void falloDeBaseDeDatos(HttpServletRequest request, HttpServletResponse response,
            SQLException ex) throws ServletException, IOException {

        getServletContext().log("No se pudo consultar MySQL en el módulo de alquileres.", ex);

        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

        request.setAttribute("error",
                "No pudimos conectar con la base de datos. Inténtalo de nuevo en unos momentos.");

        mostrarLista(request, response, List.of());
    }

    private void avisar(HttpServletRequest request, String tipo, String mensaje) {
        request.getSession().setAttribute(tipo, mensaje);
    }

    private void trasladarAvisos(HttpServletRequest request) {

        HttpSession sesion = request.getSession(false);

        if (sesion == null) {
            return;
        }

        for (String tipo : new String[]{AVISO_EXITO, AVISO_ERROR}) {

            Object aviso = sesion.getAttribute(tipo);

            if (aviso != null) {
                request.setAttribute(tipo, aviso);
                sesion.removeAttribute(tipo);
            }
        }
    }

    // regresa al listado
    private void redirigirAlListado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.sendRedirect(request.getContextPath() + "/alquileres");
    }

    // id a entero
    private static int entero(String texto) {

        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());

        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // texto yyyy-MM-dd (lo que manda <input type="date">) a LocalDate; null si no es válida
    private static LocalDate fecha(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            LocalDate fecha = LocalDate.parse(texto.trim());

            // MySQL DATE solo llega al año 9999
            return fecha.getYear() < 1000 || fecha.getYear() > 9999 ? null : fecha;

        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}