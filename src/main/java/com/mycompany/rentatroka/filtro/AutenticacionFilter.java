package com.mycompany.rentatroka.filtro;

import com.mycompany.rentatroka.seguridad.TokenCsrf;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Protege también las rutas que incorporen los demás integrantes. */
public class AutenticacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest peticion = (HttpServletRequest) request;
        HttpServletResponse respuesta = (HttpServletResponse) response;
        String ruta = peticion.getServletPath();
        boolean recurso = ruta.startsWith("/assets/") || ruta.equals("/favicon.ico");
        respuesta.setHeader("X-Content-Type-Options", "nosniff");
        respuesta.setHeader("X-Frame-Options", "DENY");
        if (recurso) {
            chain.doFilter(request, response);
            return;
        }
        respuesta.setHeader("Cache-Control", "no-store");
        HttpSession sesion = peticion.getSession(false);
        if (!ruta.equals("/login") && (sesion == null || sesion.getAttribute("usuarioSesion") == null)) {
            respuesta.sendRedirect(peticion.getContextPath() + "/login");
            return;
        }
        if (sesion != null) {
            TokenCsrf.preparar(sesion);
        }
        chain.doFilter(request, response);
    }
}

