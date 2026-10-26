package com.mycompany.rentatroka.controlador;

import com.mycompany.rentatroka.modelo.Vehiculo;
import com.mycompany.rentatroka.servicio.VehiculoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Controlador del módulo de vehículos.
// GET muestra pantallas y nunca modifica datos; POST guarda, actualiza o elimina y redirige al listado.
@WebServlet(name = "VehiculoServlet", urlPatterns = "/vehiculos")
public class VehiculoServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/vehiculos/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/vehiculos/formulario.jsp";

    private final VehiculoService servicio = new VehiculoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null || accion.isBlank()) {
            accion = "listar";
        }

        try {
            switch (accion) {
                case "listar" -> listar(request, response);
                case "nuevo" -> mostrarFormulario(request, response, new Vehiculo(), Map.of());
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

    // El parámetro q es el texto del buscador
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String busqueda = request.getParameter("q");
        if (busqueda == null) {
            busqueda = "";
        }
        mostrarLista(request, response, servicio.listar(busqueda), busqueda.trim());
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = Utilidades.aEntero(request.getParameter("id"));
        Optional<Vehiculo> vehiculo = servicio.buscarPorId(id);

        if (vehiculo.isEmpty()) {
            Utilidades.avisar(request, Utilidades.AVISO_ERROR, "El vehículo que intentas editar ya no existe.");
            irAlListado(request, response);
            return;
        }
        mostrarFormulario(request, response, vehiculo.get(), Map.of());
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Vehiculo vehiculo = leerFormulario(request);
        Map<String, String> errores = servicio.registrar(vehiculo);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, vehiculo, errores);
            return;
        }
        Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                "Vehículo " + vehiculo.getPlaca() + " registrado correctamente.");
        irAlListado(request, response);
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Vehiculo vehiculo = leerFormulario(request);
        Map<String, String> errores = servicio.actualizar(vehiculo);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, vehiculo, errores);
            return;
        }
        Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                "Vehículo " + vehiculo.getPlaca() + " actualizado correctamente.");
        irAlListado(request, response);
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = Utilidades.aEntero(request.getParameter("id"));

        switch (servicio.eliminar(id)) {
            case ELIMINADO -> Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                    "Vehículo eliminado correctamente.");
            case NO_EXISTE -> Utilidades.avisar(request, Utilidades.AVISO_ERROR,
                    "El vehículo ya no existe.");
            case CON_ALQUILERES -> Utilidades.avisar(request, Utilidades.AVISO_ERROR,
                    "No se puede eliminar el vehículo porque tiene alquileres registrados. "
                    + "Se conservan para no perder el historial.");
        }
        irAlListado(request, response);
    }

    // Arma el vehículo con lo que llegó del formulario; las reglas están en el servicio
    private Vehiculo leerFormulario(HttpServletRequest request) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(Utilidades.aEntero(request.getParameter("id")));
        vehiculo.setPlaca(request.getParameter("placa"));
        vehiculo.setMarca(request.getParameter("marca"));
        vehiculo.setModelo(request.getParameter("modelo"));
        vehiculo.setTarifaDiaria(VehiculoService.tarifaDesdeTexto(request.getParameter("tarifaDiaria")));
        return vehiculo;
    }

    private void mostrarLista(HttpServletRequest request, HttpServletResponse response,
            List<Vehiculo> vehiculos, String busqueda) throws ServletException, IOException {
        Utilidades.pasarAvisosALaVista(request);
        request.setAttribute("vehiculos", vehiculos);
        request.setAttribute("busqueda", busqueda);
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
            Vehiculo vehiculo, Map<String, String> errores) throws ServletException, IOException {
        request.setAttribute("vehiculo", vehiculo);
        request.setAttribute("errores", errores);
        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    private void falloDeBaseDeDatos(HttpServletRequest request, HttpServletResponse response, SQLException ex)
            throws ServletException, IOException {
        getServletContext().log("No se pudo consultar MySQL en el módulo de vehículos.", ex);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.setAttribute("error", "No pudimos conectar con la base de datos. Inténtalo de nuevo en unos momentos.");
        mostrarLista(request, response, List.of(), "");
    }

    private void irAlListado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/vehiculos");
    }
}
