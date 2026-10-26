<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="clientesUrl" value="/clientes" />
<c:url var="alquileresUrl" value="/alquileres" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Inicio | RentATroka</title>

    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>

    <jsp:include page="/WEB-INF/views/fragmentos/cabecera.jsp" />

    <main class="contenedor">

        <section class="bienvenida">
            <span class="etiqueta etiqueta-naranja">TU ESPACIO DE TRABAJO</span>

            <h1>
                Hola, <c:out value="${sessionScope.usuarioSesion.nombre}" />.
            </h1>

            <p>Bienvenido a la administración de RentATroka.</p>

            <span class="estado-sesion">
                <span aria-hidden="true"></span>
                Sesión iniciada
            </span>
        </section>

        <section class="modulos" aria-label="Módulos del sistema">

            <!-- Parte 1: Clientes -->
            <article class="tarjeta">
                <span class="numero-modulo" aria-hidden="true">01</span>

                <h2>Clientes</h2>

                <p>
                    La información de las personas que confían en nosotros.
                </p>

                <a class="boton boton-principal" href="${clientesUrl}">
                    Gestionar clientes
                </a>
            </article>

            <!-- Parte 2: Vehículos, pendiente -->
            <article class="tarjeta">
                <span class="numero-modulo" aria-hidden="true">02</span>

                <h2>Vehículos</h2>

                <p>
                    Las unidades y tarifas que acompañan cada viaje.
                </p>

                <span class="estado-modulo">Próximamente</span>
            </article>

                       <!-- Parte 3: Alquileres -->
            <article class="tarjeta">
                <span class="numero-modulo" aria-hidden="true">03</span>

                <h2>Alquileres</h2>

                <p>
                    Los clientes y vehículos conectados en cada alquiler.
                </p>

                <a class="boton boton-principal" href="${alquileresUrl}">
                    Gestionar alquileres
                </a>
            </article>

        </section>

    </main>

    <footer class="pie-pagina">
        RentATroka · Gestión de alquileres de vehículos
    </footer>

</body>
</html>