<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- Fuerza el formato 1,234.50 sin importar el idioma del navegador --%>
<fmt:setLocale value="en_US" />

<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="cssAlquileresUrl" value="/assets/css/alquileres.css" />
<c:url var="jsAlquileresUrl" value="/assets/js/alquileres.js" />
<c:url var="inicioUrl" value="/inicio" />
<c:url var="alquileresUrl" value="/alquileres" />

<c:url var="nuevoUrl" value="/alquileres">
    <c:param name="accion" value="nuevo" />
</c:url>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Alquileres | RentATroka</title>

    <link rel="stylesheet" href="${cssUrl}">
    <link rel="stylesheet" href="${cssAlquileresUrl}">

    <script src="${jsAlquileresUrl}" defer></script>
</head>
<body>

    <jsp:include page="/WEB-INF/views/fragmentos/cabecera.jsp" />

    <main class="contenedor modulo-alquileres">

        <a class="enlace-volver" href="${inicioUrl}">← Inicio</a>

        <div class="encabezado-modulo">
            <div>
                <span class="etiqueta etiqueta-naranja">MÓDULO 03</span>
                <h1>Alquileres</h1>
                <span class="texto-secundario">
                    Cada alquiler conecta un cliente, un vehículo y un rango de fechas.
                </span>
            </div>

            <a class="boton boton-principal" href="${nuevoUrl}">
                Nuevo alquiler
            </a>
        </div>

        <c:if test="${not empty mensajeExito}">
            <div class="aviso aviso-exito" role="status">
                <c:out value="${mensajeExito}" />
            </div>
        </c:if>

        <c:if test="${not empty mensajeError}">
            <div class="aviso aviso-error" role="alert">
                <c:out value="${mensajeError}" />
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="aviso aviso-error" role="alert">
                <c:out value="${error}" />
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty error}">
                <%-- El mensaje anterior explica el problema de conexión. --%>
            </c:when>

            <c:when test="${empty alquileres}">
                <div class="panel vacio">
                    <strong>Todavía no hay alquileres registrados</strong>
                    <span class="texto-secundario">
                        Registra el primer alquiler para comenzar.
                    </span>
                </div>
            </c:when>

            <c:otherwise>
                <p class="contador">
                    <c:out value="${alquileres.size()}" />
                    <c:choose>
                        <c:when test="${alquileres.size() == 1}">
                            alquiler registrado
                        </c:when>
                        <c:otherwise>
                            alquileres registrados
                        </c:otherwise>
                    </c:choose>
                </p>

                <div class="panel tabla-contenedor">
                    <table class="tabla">
                        <caption class="solo-lectores">
                            Listado de alquileres
                        </caption>

                        <thead>
                            <tr>
                                <th scope="col">Cliente</th>
                                <th scope="col">Vehículo</th>
                                <th scope="col">Inicio</th>
                                <th scope="col">Fin</th>
                                <th scope="col" class="numero">Días</th>
                                <th scope="col" class="numero">Tarifa/día</th>
                                <th scope="col" class="numero">Total</th>
                                <th scope="col">
                                    <span class="solo-lectores">Acciones</span>
                                </th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="alq" items="${alquileres}">
                                <c:url var="editarUrl" value="/alquileres">
                                    <c:param name="accion" value="editar" />
                                    <c:param name="id" value="${alq.id}" />
                                </c:url>

                                <tr>
                                    <td data-label="Cliente">
                                        <strong>
                                            <c:out value="${alq.clienteNombre}" />
                                        </strong>
                                    </td>

                                    <td data-label="Vehículo">
                                        <strong><c:out value="${alq.placa}" /></strong>
                                        <span class="texto-secundario">
                                            <c:out value="${alq.marca}" />
                                            <c:out value="${alq.modelo}" />
                                        </span>
                                    </td>

                                    <td data-label="Inicio">
                                        <c:out value="${alq.inicioTexto}" />
                                    </td>

                                    <td data-label="Fin">
                                        <c:out value="${alq.finTexto}" />
                                    </td>

                                    <td class="numero" data-label="Días">
                                        <c:out value="${alq.dias}" />
                                    </td>

                                    <td class="numero" data-label="Tarifa/día">
                                        <fmt:formatNumber value="${alq.tarifaDiariaAplicada}"
                                                          pattern="$#,##0.00" />
                                    </td>

                                    <td class="numero" data-label="Total">
                                        <strong>
                                            <fmt:formatNumber value="${alq.total}"
                                                              pattern="$#,##0.00" />
                                        </strong>
                                    </td>

                                    <td class="acciones-fila">
                                        <a class="boton boton-secundario boton-pequeno"
                                           href="${editarUrl}">
                                            Editar
                                        </a>

                                        <form action="${alquileresUrl}"
                                              method="post"
                                              data-confirmar="¿Eliminar este alquiler? Esta acción no se puede deshacer.">

                                            <input type="hidden"
                                                   name="csrfToken"
                                                   value="${sessionScope.csrfToken}">

                                            <input type="hidden"
                                                   name="accion"
                                                   value="eliminar">

                                            <input type="hidden"
                                                   name="id"
                                                   value="${alq.id}">

                                            <button type="submit"
                                                    class="boton boton-peligro boton-pequeno">
                                                Eliminar
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <p class="nota-tabla">
                    La fecha final es el día de devolución y no se cobra: del 2 al 5 son 3 días.
                    La tarifa mostrada es la que se aplicó al registrar el alquiler.
                </p>
            </c:otherwise>
        </c:choose>

    </main>

    <footer class="pie-pagina">
        RentATroka · Gestión de alquileres
    </footer>

</body>
</html>