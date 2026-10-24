package com.mycompany.rentatroka.controlador;

import com.mycompany.rentatroka.modelo.Cliente;
import com.mycompany.rentatroka.servicio.ClienteService;
import com.mycompany.rentatroka.servicio.ClienteService.ResultadoEliminacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "ClienteServlet", urlPatterns = "/clientes")
public class ClienteServlet extends HttpServlet {

    private static final String VISTA_LISTA =
            "/WEB-INF/views/clientes/lista.jsp";

    private static final String VISTA_FORMULARIO =
            "/WEB-INF/views/clientes/formulario.jsp";

    private static final String AVISO_EXITO = "mensajeExito";
    private static final String AVISO_ERROR = "mensajeError";

    private final ClienteService servicio = new ClienteService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        try {
            switch (accion == null || accion.isBlank()
                    ? "listar" : accion) {

                case "listar" ->
                    listar(request, response);

                case "nuevo" ->
                    mostrarFormulario(
                            request, response, new Cliente(), Map.of());

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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
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

    // ver clientes registrados
    private void listar(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        mostrarLista(request, response, servicio.listar());
    }

    // buscar al cliente a editar
    private void editar(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        int id = entero(request.getParameter("id"));

        Optional<Cliente> cliente = servicio.buscarPorId(id);

        if (cliente.isEmpty()) {
            avisar(request, AVISO_ERROR,
                    "El cliente que intentas editar ya no existe.");

            redirigirAlListado(request, response);
            return;
        }

        mostrarFormulario(
                request, response, cliente.get(), Map.of());
    }

    // registrar cliente nuevo
    private void guardar(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Cliente cliente = leerFormulario(request);

        Map<String, String> errores = servicio.registrar(cliente);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            mostrarFormulario(
                    request, response, cliente, errores);
            return;
        }

        avisar(request, AVISO_EXITO,
                "Cliente " + cliente.getNombre()
                + " registrado correctamente.");

        redirigirAlListado(request, response);
    }

    // actualizar un cliente existente
    private void actualizar(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Cliente cliente = leerFormulario(request);

        Map<String, String> errores = servicio.actualizar(cliente);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            mostrarFormulario(
                    request, response, cliente, errores);
            return;
        }

        avisar(request, AVISO_EXITO,
                "Cliente " + cliente.getNombre()
                + " actualizado correctamente.");

        redirigirAlListado(request, response);
    }

    // eliminar cliente
    private void eliminar(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, IOException {

        int id = entero(request.getParameter("id"));

        String nombre = servicio.buscarPorId(id)
                .map(Cliente::getNombre)
                .orElse("");

        ResultadoEliminacion resultado = servicio.eliminar(id);

        switch (resultado) {

            case ELIMINADO ->
                avisar(request, AVISO_EXITO,
                        "Cliente " + nombre
                        + " eliminado correctamente.");

            case NO_EXISTE ->
                avisar(request, AVISO_ERROR,
                        "El cliente ya no existe.");

            case CON_ALQUILERES ->
                avisar(request, AVISO_ERROR,
                        "No se puede eliminar el cliente porque tiene "
                        + "alquileres asociados.");
        }

        redirigirAlListado(request, response);
    }

    // crear el objeto cliente en base al formulario
    private Cliente leerFormulario(HttpServletRequest request) {

        Cliente cliente = new Cliente();

        cliente.setId(entero(request.getParameter("id")));
        cliente.setNombre(request.getParameter("nombre"));
        cliente.setDocumento(request.getParameter("documento"));
        cliente.setTelefono(request.getParameter("telefono"));

        return cliente;
    }

    // prepara los datos para mostrarlos para la vista 
    private void mostrarLista(
            HttpServletRequest request,
            HttpServletResponse response,
            List<Cliente> clientes)
            throws ServletException, IOException {

        trasladarAvisos(request);

        request.setAttribute("clientes", clientes);

        request.getRequestDispatcher(VISTA_LISTA)
                .forward(request, response);
    }

    // formulario
    private void mostrarFormulario(
            HttpServletRequest request,
            HttpServletResponse response,
            Cliente cliente,
            Map<String, String> errores)
            throws ServletException, IOException {

        request.setAttribute("cliente", cliente);
        request.setAttribute("errores", errores);

        request.getRequestDispatcher(VISTA_FORMULARIO)
                .forward(request, response);
    }

    // mensaje de error en la base de datos
    private void falloDeBaseDeDatos(
            HttpServletRequest request,
            HttpServletResponse response,
            SQLException ex)
            throws ServletException, IOException {

        getServletContext().log(
                "No se pudo consultar MySQL en el módulo de clientes.",
                ex);

        response.setStatus(
                HttpServletResponse.SC_SERVICE_UNAVAILABLE);

        request.setAttribute(
                "error",
                "No pudimos conectar con la base de datos. "
                + "Inténtalo de nuevo en unos momentos.");

        mostrarLista(request, response, List.of());
    }

    private void avisar(
            HttpServletRequest request,
            String tipo,
            String mensaje) {

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
    private void redirigirAlListado(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath() + "/clientes");
    }

    // id a entero
    private static int entero(String texto) {

        try {
            return Integer.parseInt(
                    texto == null ? "" : texto.trim());

        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}