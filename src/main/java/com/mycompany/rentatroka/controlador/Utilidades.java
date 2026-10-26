package com.mycompany.rentatroka.controlador;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// Métodos que usan los tres Servlets de los módulos (clientes, vehículos y alquileres)
public final class Utilidades {

    // Nombres de los avisos que muestran las vistas de listado
    public static final String AVISO_EXITO = "mensajeExito";
    public static final String AVISO_ERROR = "mensajeError";

    private Utilidades() {
    }

    // Convierte un parámetro a entero; si no es un número devuelve 0
    public static int aEntero(String texto) {
        if (texto == null) {
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // Guarda un aviso en la sesión para mostrarlo después del redirect
    public static void avisar(HttpServletRequest request, String tipo, String mensaje) {
        request.getSession().setAttribute(tipo, mensaje);
    }

    // Pasa los avisos de la sesión a la petición y los borra para que se muestren una sola vez
    public static void pasarAvisosALaVista(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return;
        }

        Object exito = sesion.getAttribute(AVISO_EXITO);
        if (exito != null) {
            request.setAttribute(AVISO_EXITO, exito);
            sesion.removeAttribute(AVISO_EXITO);
        }

        Object error = sesion.getAttribute(AVISO_ERROR);
        if (error != null) {
            request.setAttribute(AVISO_ERROR, error);
            sesion.removeAttribute(AVISO_ERROR);
        }
    }
}
