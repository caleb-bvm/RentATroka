package com.mycompany.rentatroka.controlador;

import com.mycompany.rentatroka.modelo.Vehiculo;
import com.mycompany.rentatroka.servicio.VehiculoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/*
    Controlador del módulo de vehículos

    GET muestra pantallas y nunca modifica datos
    POST guarda, actualiza o elimina y responde con un redirect al listado.
 */

@WebServlet(name = "VehiculoServlet", urlPatterns = "/vehiculos")
public class VehiculoServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/vehiculos/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/vehiculos/formulario.jsp";
    private static final String AVISO_EXITO = "mensajeExito";
    private static final String AVISO_ERROR = "mensajeError";

    private final VehiculoService servicio = new VehiculoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        try {
            switch (accion == null || accion.isBlank() ? "listar" : accion) {
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
        try {
            switch (accion == null ? "" : accion) {
                case "guardar" -> guardar(request, response);
                case "actualizar" -> actualizar(request, response);
                case "eliminar" -> eliminar(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            falloDeBaseDeDatos(request, response, ex);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String busqueda = request.getParameter("q");
        mostrarLista(request, response, servicio.listar(busqueda), busqueda);
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Optional<Vehiculo> vehiculo = servicio.buscarPorId(entero(request.getParameter("id")));
        if (vehiculo.isEmpty()) {
            avisar(request, AVISO_ERROR, "El vehículo que intentas editar ya no existe.");
            redirigirAlListado(request, response);
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
        avisar(request, AVISO_EXITO, "Vehículo " + vehiculo.getPlaca() + " registrado correctamente.");
        redirigirAlListado(request, response);
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
        avisar(request, AVISO_EXITO, "Vehículo " + vehiculo.getPlaca() + " actualizado correctamente.");
        redirigirAlListado(request, response);
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = entero(request.getParameter("id"));
        String placa = servicio.buscarPorId(id).map(Vehiculo::getPlaca).orElse("");
        switch (servicio.eliminar(id)) {
            case ELIMINADO -> avisar(request, AVISO_EXITO, "Vehículo " + placa + " eliminado correctamente.");
            case CON_ALQUILERES -> avisar(request, AVISO_ERROR, "No se puede eliminar el vehículo " + placa
                    + " porque tiene alquileres registrados. Se conservan para no perder el historial.");
            case NO_EXISTE -> avisar(request, AVISO_ERROR, "El vehículo ya no existe.");
        }
        redirigirAlListado(request, response);
    }

    // Arma el bean con lo que llegó del formulario, la normalización y las reglas están en el servicio
    
    private Vehiculo leerFormulario(HttpServletRequest request) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(entero(request.getParameter("id")));
        vehiculo.setPlaca(request.getParameter("placa"));
        vehiculo.setMarca(request.getParameter("marca"));
        vehiculo.setModelo(request.getParameter("modelo"));
        vehiculo.setTarifaDiaria(VehiculoService.tarifaDesdeTexto(request.getParameter("tarifaDiaria")));
        return vehiculo;
    }

    private void mostrarLista(HttpServletRequest request, HttpServletResponse response,
            List<Vehiculo> vehiculos, String busqueda) throws ServletException, IOException {
        trasladarAvisos(request);
        request.setAttribute("vehiculos", vehiculos);
        request.setAttribute("busqueda", busqueda == null ? "" : busqueda.trim());
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

    // Guarda un aviso en la sesión para mostrarlo una sola vez después del redirect
    
    private void avisar(HttpServletRequest request, String tipo, String mensaje) {
        request.getSession().setAttribute(tipo, mensaje);
    }

    // Pasa los avisos pendientes de la sesión a la petición y los retira para que no se repitan
    
    private void trasladarAvisos(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return;
        }
        for (String tipo : new String[] {AVISO_EXITO, AVISO_ERROR}) {
            Object aviso = sesion.getAttribute(tipo);
            if (aviso != null) {
                request.setAttribute(tipo, aviso);
                sesion.removeAttribute(tipo);
            }
        }
    }

    private void redirigirAlListado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/vehiculos");
    }

    private static int entero(String texto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
