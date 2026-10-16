package com.mycompany.rentatroka.filtro;

import com.mycompany.rentatroka.seguridad.TokenCsrf;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest peticion = (HttpServletRequest) request;
        HttpServletResponse respuesta = (HttpServletResponse) response;
        if ("POST".equalsIgnoreCase(peticion.getMethod())
                && !TokenCsrf.esValido(peticion.getSession(false), peticion.getParameter("csrfToken"))) {
            respuesta.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "El formulario venció. Recarga la página e inténtalo de nuevo.");
            return;
        }
        chain.doFilter(request, response);
    }
}

