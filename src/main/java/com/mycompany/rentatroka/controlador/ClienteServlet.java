package com.mycompany.rentatroka.controlador;

import com.mycompany.rentatroka.modelo.Cliente;
import com.mycompany.rentatroka.servicio.ClienteService;
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

// Controlador del módulo de clientes.
// GET muestra pantallas y nunca modifica datos; POST guarda, actualiza o elimina y redirige al listado.
@WebServlet(name = "ClienteServlet", urlPatterns = "/clientes")
public class ClienteServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/clientes/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/clientes/formulario.jsp";

    private final ClienteService servicio = new ClienteService();

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
                case "nuevo" -> mostrarFormulario(request, response, new Cliente(), Map.of());
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
        Optional<Cliente> cliente = servicio.buscarPorId(id);

        if (cliente.isEmpty()) {
            Utilidades.avisar(request, Utilidades.AVISO_ERROR, "El cliente que intentas editar ya no existe.");
            irAlListado(request, response);
            return;
        }
        mostrarFormulario(request, response, cliente.get(), Map.of());
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Cliente cliente = leerFormulario(request);
        Map<String, String> errores = servicio.registrar(cliente);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, cliente, errores);
            return;
        }
        Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                "Cliente " + cliente.getNombre() + " registrado correctamente.");
        irAlListado(request, response);
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Cliente cliente = leerFormulario(request);
        Map<String, String> errores = servicio.actualizar(cliente);

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, cliente, errores);
            return;
        }
        Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                "Cliente " + cliente.getNombre() + " actualizado correctamente.");
        irAlListado(request, response);
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = Utilidades.aEntero(request.getParameter("id"));

        switch (servicio.eliminar(id)) {
            case ELIMINADO -> Utilidades.avisar(request, Utilidades.AVISO_EXITO,
                    "Cliente eliminado correctamente.");
            case NO_EXISTE -> Utilidades.avisar(request, Utilidades.AVISO_ERROR,
                    "El cliente ya no existe.");
            case CON_ALQUILERES -> Utilidades.avisar(request, Utilidades.AVISO_ERROR,
                    "No se puede eliminar el cliente porque tiene alquileres asociados.");
        }
        irAlListado(request, response);
    }

    // Arma el cliente con lo que llegó del formulario
    private Cliente leerFormulario(HttpServletRequest request) {
        Cliente cliente = new Cliente();
        cliente.setId(Utilidades.aEntero(request.getParameter("id")));
        cliente.setNombre(request.getParameter("nombre"));
        cliente.setDocumento(request.getParameter("documento"));
        cliente.setTelefono(request.getParameter("telefono"));
        return cliente;
    }

    private void mostrarLista(HttpServletRequest request, HttpServletResponse response, List<Cliente> clientes)
            throws ServletException, IOException {
        Utilidades.pasarAvisosALaVista(request);
        request.setAttribute("clientes", clientes);
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
            Cliente cliente, Map<String, String> errores) throws ServletException, IOException {
        request.setAttribute("cliente", cliente);
        request.setAttribute("errores", errores);
        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    private void falloDeBaseDeDatos(HttpServletRequest request, HttpServletResponse response, SQLException ex)
            throws ServletException, IOException {
        getServletContext().log("No se pudo consultar MySQL en el módulo de clientes.", ex);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.setAttribute("error", "No pudimos conectar con la base de datos. Inténtalo de nuevo en unos momentos.");
        mostrarLista(request, response, List.of());
    }

    private void irAlListado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/clientes");
    }
}
