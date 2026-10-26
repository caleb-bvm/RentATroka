<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="cssVehiculosUrl" value="/assets/css/vehiculos.css" />
<c:url var="jsConfirmarUrl" value="/assets/js/confirmar.js" />
<c:url var="inicioUrl" value="/inicio" />
<c:url var="vehiculosUrl" value="/vehiculos" />
<c:url var="nuevoUrl" value="/vehiculos">
    <c:param name="accion" value="nuevo" />
</c:url>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Vehículos | RentATroka</title>
    <link rel="stylesheet" href="${cssUrl}">
    <link rel="stylesheet" href="${cssVehiculosUrl}">
    <script src="${jsConfirmarUrl}" defer></script>
</head>
<body>
    <jsp:include page="/WEB-INF/views/fragmentos/cabecera.jsp" />
    <main class="contenedor modulo-vehiculos">
        <a class="enlace-volver" href="${inicioUrl}">← Inicio</a>
        <div class="encabezado-modulo">
            <div>
                <span class="etiqueta etiqueta-naranja">MÓDULO 02</span>
                <h1>Vehículos</h1>
                <span class="texto-secundario">Las unidades y tarifas que acompañan cada viaje.</span>
            </div>
            <a class="boton boton-principal" href="${nuevoUrl}">Nuevo vehículo</a>
        </div>

        <c:if test="${not empty mensajeExito}">
            <div class="aviso aviso-exito" role="status"><c:out value="${mensajeExito}" /></div>
        </c:if>
        <c:if test="${not empty mensajeError}">
            <div class="aviso aviso-error" role="alert"><c:out value="${mensajeError}" /></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="aviso aviso-error" role="alert"><c:out value="${error}" /></div>
        </c:if>

        <form class="buscador" action="${vehiculosUrl}" method="get" role="search">
            <input type="search" name="q" value="<c:out value='${busqueda}' />" maxlength="50"
                   placeholder="Buscar por placa, marca o modelo" aria-label="Buscar vehículos">
            <button type="submit" class="boton boton-principal">Buscar</button>
            <c:if test="${not empty busqueda}">
                <a class="boton boton-secundario" href="${vehiculosUrl}">Ver todos</a>
            </c:if>
        </form>

        <c:choose>
            <c:when test="${not empty error}">
                <%-- Si no tiene datos que mostrar - el aviso de arriba ya explica el problema. --%>
            </c:when>
            <c:when test="${empty vehiculos}">
                <div class="panel vacio">
                    <c:choose>
                        <c:when test="${not empty busqueda}">
                            <strong>Sin resultados</strong>
                            <span class="texto-secundario">No encontramos vehículos que coincidan con «<c:out value="${busqueda}" />».</span>
                        </c:when>
                        <c:otherwise>
                            <strong>Todavía no hay vehículos</strong>
                            <span class="texto-secundario">Registra el primero para poder usarlo en un alquiler.</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:when>
            <c:otherwise>
                <p class="contador">${vehiculos.size()} ${vehiculos.size() == 1 ? 'vehículo' : 'vehículos'}</p>
                <div class="panel tabla-contenedor">
                    <table class="tabla">
                        <caption class="solo-lectores">Listado de vehículos</caption>
                        <thead>
                            <tr>
                                <th scope="col">Placa</th>
                                <th scope="col">Marca</th>
                                <th scope="col">Modelo</th>
                                <th scope="col" class="numero">Tarifa diaria</th>
                                <th scope="col"><span class="solo-lectores">Acciones</span></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="vehiculo" items="${vehiculos}">
                                <c:url var="editarUrl" value="/vehiculos">
                                    <c:param name="accion" value="editar" />
                                    <c:param name="id" value="${vehiculo.id}" />
                                </c:url>
                                <tr>
                                    <td data-label="Placa"><strong><c:out value="${vehiculo.placa}" /></strong></td>
                                    <td data-label="Marca"><c:out value="${vehiculo.marca}" /></td>
                                    <td data-label="Modelo"><c:out value="${vehiculo.modelo}" /></td>
                                    <td data-label="Tarifa diaria" class="numero">$<c:out value="${vehiculo.tarifaDiaria}" /></td>
                                    <td class="acciones-fila">
                                        <a class="boton boton-secundario boton-pequeno" href="${editarUrl}">Editar</a>
                                        <form action="${vehiculosUrl}" method="post"
                                              data-confirmar="¿Eliminar el vehículo <c:out value='${vehiculo.placa}' />? Esta acción no se puede deshacer.">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="accion" value="eliminar">
                                            <input type="hidden" name="id" value="${vehiculo.id}">
                                            <button type="submit" class="boton boton-peligro boton-pequeno">Eliminar</button>
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
    <footer class="pie-pagina">RentATroka · Gestión de alquileres de vehículos</footer>
</body>
</html>
