package com.mycompany.rentatroka.controlador;

import com.mycompany.rentatroka.modelo.Usuario;
import com.mycompany.rentatroka.seguridad.TokenCsrf;
import com.mycompany.rentatroka.servicio.AutenticacionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private final AutenticacionService autenticacion = new AutenticacionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        if (sesion.getAttribute("usuarioSesion") != null) {
            response.sendRedirect(request.getContextPath() + "/inicio");
            return;
        }
        TokenCsrf.preparar(sesion);
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesionActual = request.getSession(false);
        if (sesionActual != null && sesionActual.getAttribute("usuarioSesion") != null) {
            response.sendRedirect(request.getContextPath() + "/inicio");
            return;
        }
        String usuario = request.getParameter("usuario");
        String claveRecibida = request.getParameter("clave");
        if (usuario == null || usuario.isBlank() || claveRecibida == null || claveRecibida.isEmpty()) {
            mostrarError(request, response, "Completa el usuario y la contraseña.", 400);
            return;
        }
        if (usuario.length() > 50 || claveRecibida.length() > 128) {
            mostrarError(request, response, "El usuario o la contraseña supera la longitud permitida.", 400);
            return;
        }
        char[] clave = claveRecibida.toCharArray();
        try {
            Optional<Usuario> autenticado = autenticacion.autenticar(usuario, clave);
            if (autenticado.isEmpty()) {
                mostrarError(request, response, "El usuario o la contraseña son incorrectos.", 401);
                return;
            }
            // Invalida la sesión anterior para impedir la fijación de sesión.
            if (sesionActual != null) {
                sesionActual.invalidate();
            }
            HttpSession sesion = request.getSession(true);
            sesion.setAttribute("usuarioSesion", autenticado.get());
            TokenCsrf.preparar(sesion);
            response.sendRedirect(request.getContextPath() + "/inicio");
        } catch (SQLException ex) {
            getServletContext().log("No se pudo consultar MySQL al iniciar sesión.", ex);
            mostrarError(request, response, "No pudimos conectar con el sistema. Inténtalo de nuevo en unos momentos.", 503);
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response,
            String mensaje, int estado) throws ServletException, IOException {
        response.setStatus(estado);
        request.setAttribute("error", mensaje);
        String usuario = request.getParameter("usuario");
        request.setAttribute("usuario", usuario != null && usuario.length() <= 50 ? usuario : "");
        TokenCsrf.preparar(request.getSession());
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
}

