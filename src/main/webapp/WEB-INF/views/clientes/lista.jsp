<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="cssClientesUrl" value="/assets/css/clientes.css" />
<c:url var="jsClientesUrl" value="/assets/js/clientes.js" />
<c:url var="inicioUrl" value="/inicio" />
<c:url var="clientesUrl" value="/clientes" />

<c:url var="nuevoUrl" value="/clientes">
    <c:param name="accion" value="nuevo" />
</c:url>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Clientes | RentATroka</title>

    <link rel="stylesheet" href="${cssUrl}">
    <link rel="stylesheet" href="${cssClientesUrl}">

    <script src="${jsClientesUrl}" defer></script>
</head>
<body>

    <jsp:include page="/WEB-INF/views/fragmentos/cabecera.jsp" />

    <main class="contenedor modulo-clientes">

        <a class="enlace-volver" href="${inicioUrl}">← Inicio</a>

        <div class="encabezado-modulo">
            <div>
                <span class="etiqueta etiqueta-naranja">MÓDULO 01</span>
                <h1>Clientes</h1>
                <span class="texto-secundario">
                    Administra la información de los clientes de RentATroka.
                </span>
            </div>

            <a class="boton boton-principal" href="${nuevoUrl}">
                Nuevo cliente
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

            <c:when test="${empty clientes}">
                <div class="panel vacio">
                    <strong>Todavía no hay clientes registrados</strong>
                    <span class="texto-secundario">
                        Registra el primer cliente para comenzar.
                    </span>
                </div>
            </c:when>

            <c:otherwise>
                <p class="contador">
                    <c:out value="${clientes.size()}" />
                    <c:choose>
                        <c:when test="${clientes.size() == 1}">
                            cliente registrado
                        </c:when>
                        <c:otherwise>
                            clientes registrados
                        </c:otherwise>
                    </c:choose>
                </p>

                <div class="panel tabla-contenedor">
                    <table class="tabla">
                        <caption class="solo-lectores">
                            Listado de clientes
                        </caption>

                        <thead>
                            <tr>
                                <th scope="col">Nombre</th>
                                <th scope="col">Documento</th>
                                <th scope="col">Teléfono</th>
                                <th scope="col">
                                    <span class="solo-lectores">Acciones</span>
                                </th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="cliente" items="${clientes}">
                                <c:url var="editarUrl" value="/clientes">
                                    <c:param name="accion" value="editar" />
                                    <c:param name="id" value="${cliente.id}" />
                                </c:url>

                                <tr>
                                    <td data-label="Nombre">
                                        <strong>
                                            <c:out value="${cliente.nombre}" />
                                        </strong>
                                    </td>

                                    <td data-label="Documento">
                                        <c:out value="${cliente.documento}" />
                                    </td>

                                    <td data-label="Teléfono">
                                        <c:out value="${cliente.telefono}" />
                                    </td>

                                    <td class="acciones-fila">
                                        <a class="boton boton-secundario boton-pequeno"
                                           href="${editarUrl}">
                                            Editar
                                        </a>

                                        <form action="${clientesUrl}"
                                              method="post"
                                              data-confirmar="¿Eliminar este cliente? Esta acción no se puede deshacer.">

                                            <input type="hidden"
                                                   name="csrfToken"
                                                   value="${sessionScope.csrfToken}">

                                            <input type="hidden"
                                                   name="accion"
                                                   value="eliminar">

                                            <input type="hidden"
                                                   name="id"
                                                   value="${cliente.id}">

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
            </c:otherwise>
        </c:choose>

    </main>

    <footer class="pie-pagina">
        RentATroka · Gestión de clientes
    </footer>

</body>
</html>