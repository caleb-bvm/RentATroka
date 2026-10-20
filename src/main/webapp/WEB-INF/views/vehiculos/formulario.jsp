<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="cssVehiculosUrl" value="/assets/css/vehiculos.css" />
<c:url var="vehiculosUrl" value="/vehiculos" />
<c:set var="edicion" value="${vehiculo.id > 0}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${edicion ? 'Editar vehículo' : 'Nuevo vehículo'}" /> | RentATroka</title>
    <link rel="stylesheet" href="${cssUrl}">
    <link rel="stylesheet" href="${cssVehiculosUrl}">
</head>
<body>
    <jsp:include page="/WEB-INF/views/fragmentos/cabecera.jsp" />
    <main class="contenedor modulo-vehiculos">
        <a class="enlace-volver" href="${vehiculosUrl}">← Vehículos</a>
        <div class="encabezado-modulo">
            <div>
                <span class="etiqueta etiqueta-naranja">MÓDULO 02</span>
                <h1><c:out value="${edicion ? 'Editar vehículo' : 'Nuevo vehículo'}" /></h1>
                <span class="texto-secundario">Todos los campos son obligatorios.</span>
            </div>
        </div>

        <c:if test="${not empty errores.general}">
            <div class="aviso aviso-error" role="alert"><c:out value="${errores.general}" /></div>
        </c:if>
        <c:if test="${not empty errores and empty errores.general}">
            <div class="aviso aviso-error" role="alert">Revisa los campos marcados antes de guardar.</div>
        </c:if>

        <section class="panel panel-formulario">
            <form class="formulario" action="${vehiculosUrl}" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="accion" value="${edicion ? 'actualizar' : 'guardar'}">
                <c:if test="${edicion}">
                    <input type="hidden" name="id" value="${vehiculo.id}">
                </c:if>

                <div class="campo ${not empty errores.placa ? 'campo-error' : ''}">
                    <label for="placa">Placa</label>
                    <input id="placa" name="placa" type="text" value="<c:out value='${vehiculo.placa}' />"
                           required minlength="3" maxlength="15" pattern="[A-Za-z0-9\-]{3,15}"
                           title="Letras, números y guiones (de 3 a 15 caracteres)"
                           autocomplete="off" autocapitalize="characters" spellcheck="false"
                           aria-describedby="ayuda-placa<c:if test='${not empty errores.placa}'> error-placa</c:if>"
                           <c:if test="${not empty errores.placa}">aria-invalid="true"</c:if>>
                    <span class="ayuda" id="ayuda-placa">Ejemplo: P001-001. Se guarda en mayúsculas y no puede repetirse.</span>
                    <c:if test="${not empty errores.placa}">
                        <span class="error-campo" id="error-placa"><c:out value="${errores.placa}" /></span>
                    </c:if>
                </div>

                <div class="fila-campos">
                    <div class="campo ${not empty errores.marca ? 'campo-error' : ''}">
                        <label for="marca">Marca</label>
                        <input id="marca" name="marca" type="text" value="<c:out value='${vehiculo.marca}' />"
                               required maxlength="50" autocomplete="off"
                               <c:if test="${not empty errores.marca}">aria-invalid="true" aria-describedby="error-marca"</c:if>>
                        <c:if test="${not empty errores.marca}">
                            <span class="error-campo" id="error-marca"><c:out value="${errores.marca}" /></span>
                        </c:if>
                    </div>
                    <div class="campo ${not empty errores.modelo ? 'campo-error' : ''}">
                        <label for="modelo">Modelo</label>
                        <input id="modelo" name="modelo" type="text" value="<c:out value='${vehiculo.modelo}' />"
                               required maxlength="50" autocomplete="off"
                               <c:if test="${not empty errores.modelo}">aria-invalid="true" aria-describedby="error-modelo"</c:if>>
                        <c:if test="${not empty errores.modelo}">
                            <span class="error-campo" id="error-modelo"><c:out value="${errores.modelo}" /></span>
                        </c:if>
                    </div>
                </div>

                <div class="campo ${not empty errores.tarifaDiaria ? 'campo-error' : ''}">
                    <label for="tarifaDiaria">Tarifa diaria ($)</label>
                    <input id="tarifaDiaria" name="tarifaDiaria" type="number" inputmode="decimal"
                           value="<c:out value='${vehiculo.tarifaDiaria}' />"
                           required min="0.01" max="99999999.99" step="0.01"
                           aria-describedby="ayuda-tarifa<c:if test='${not empty errores.tarifaDiaria}'> error-tarifa</c:if>"
                           <c:if test="${not empty errores.tarifaDiaria}">aria-invalid="true"</c:if>>
                    <span class="ayuda" id="ayuda-tarifa">
                        <c:choose>
                            <c:when test="${edicion}">Los alquileres ya registrados conservan la tarifa con la que se crearon.</c:when>
                            <c:otherwise>Mayor que 0, con un máximo de 2 decimales.</c:otherwise>
                        </c:choose>
                    </span>
                    <c:if test="${not empty errores.tarifaDiaria}">
                        <span class="error-campo" id="error-tarifa"><c:out value="${errores.tarifaDiaria}" /></span>
                    </c:if>
                </div>

                <div class="acciones-formulario">
                    <button type="submit" class="boton boton-principal">
                        <c:out value="${edicion ? 'Guardar cambios' : 'Registrar vehículo'}" />
                    </button>
                    <a class="boton boton-secundario" href="${vehiculosUrl}">Cancelar</a>
                </div>
            </form>
        </section>
    </main>
    <footer class="pie-pagina">RentATroka · Gestión de alquileres de vehículos</footer>
</body>
</html>
