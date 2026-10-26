<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="cssAlquileresUrl" value="/assets/css/alquileres.css" />
<c:url var="jsAlquileresUrl" value="/assets/js/alquileres.js" />
<c:url var="alquileresUrl" value="/alquileres" />

<c:set var="edicion" value="${alquiler.id > 0}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>
        <c:out value="${edicion ? 'Editar alquiler' : 'Nuevo alquiler'}" />
        | RentATroka
    </title>

    <link rel="stylesheet" href="${cssUrl}">
    <link rel="stylesheet" href="${cssAlquileresUrl}">

    <script src="${jsAlquileresUrl}" defer></script>
</head>
<body>

    <jsp:include page="/WEB-INF/views/fragmentos/cabecera.jsp" />

    <main class="contenedor modulo-alquileres">

        <a class="enlace-volver" href="${alquileresUrl}">
            ← Volver a alquileres
        </a>

        <div class="encabezado-modulo">
            <div>
                <span class="etiqueta etiqueta-naranja">MÓDULO 03</span>

                <h1>
                    <c:out value="${edicion ? 'Editar alquiler' : 'Nuevo alquiler'}" />
                </h1>

                <span class="texto-secundario">
                    Elige el cliente, el vehículo y las fechas. Todos los campos son obligatorios.
                </span>
            </div>
        </div>

        <c:if test="${not empty errores.general}">
            <div class="aviso aviso-error" role="alert">
                <c:out value="${errores.general}" />
            </div>
        </c:if>

        <c:if test="${not empty errores and empty errores.general}">
            <div class="aviso aviso-error" role="alert">
                Revisa los campos marcados antes de guardar.
            </div>
        </c:if>

        <c:if test="${empty clientes or empty vehiculos}">
            <div class="aviso aviso-error" role="alert">
                Para crear un alquiler primero debe haber al menos un cliente y un vehículo registrados.
            </div>
        </c:if>

        <section class="panel panel-formulario">

            <form class="formulario" action="${alquileresUrl}" method="post">

                <input type="hidden"
                       name="csrfToken"
                       value="${sessionScope.csrfToken}">

                <input type="hidden"
                       name="accion"
                       value="${edicion ? 'actualizar' : 'guardar'}">

                <c:if test="${edicion}">
                    <input type="hidden" name="id" value="${alquiler.id}">
                </c:if>

                <div class="campo ${not empty errores.cliente ? 'campo-error' : ''}">
                    <label for="clienteId">Cliente</label>

                    <select id="clienteId"
                            name="clienteId"
                            required
                            <c:if test="${not empty errores.cliente}">
                                aria-invalid="true"
                                aria-describedby="error-cliente"
                            </c:if>>

                        <option value="">Selecciona un cliente</option>

                        <c:forEach var="cli" items="${clientes}">
                            <option value="${cli.id}"
                                    ${cli.id == alquiler.clienteId ? 'selected' : ''}>
                                <c:out value="${cli.nombre}" />
                                · <c:out value="${cli.documento}" />
                            </option>
                        </c:forEach>
                    </select>

                    <c:if test="${not empty errores.cliente}">
                        <span class="error-campo" id="error-cliente">
                            <c:out value="${errores.cliente}" />
                        </span>
                    </c:if>
                </div>

                <div class="campo ${not empty errores.vehiculo ? 'campo-error' : ''}">
                    <label for="vehiculoId">Vehículo</label>

                    <select id="vehiculoId"
                            name="vehiculoId"
                            required
                            <c:if test="${not empty errores.vehiculo}">
                                aria-invalid="true"
                                aria-describedby="error-vehiculo"
                            </c:if>>

                        <option value="">Selecciona un vehículo</option>

                        <c:forEach var="veh" items="${vehiculos}">
                            <option value="${veh.id}"
                                    data-tarifa="${veh.tarifaDiaria}"
                                    ${veh.id == alquiler.vehiculoId ? 'selected' : ''}>
                                <c:out value="${veh.placa}" />
                                · <c:out value="${veh.marca}" />
                                <c:out value="${veh.modelo}" />
                                · $<c:out value="${veh.tarifaDiaria}" />/día
                            </option>
                        </c:forEach>
                    </select>

                    <c:if test="${not empty errores.vehiculo}">
                        <span class="error-campo" id="error-vehiculo">
                            <c:out value="${errores.vehiculo}" />
                        </span>
                    </c:if>
                </div>

                <div class="fila-fechas">

                    <div class="campo ${not empty errores.fechaInicio ? 'campo-error' : ''}">
                        <label for="fechaInicio">Fecha de inicio</label>

                        <input id="fechaInicio"
                               name="fechaInicio"
                               type="date"
                               value="<c:out value='${alquiler.fechaInicio}' />"
                               required
                               <c:if test="${not empty errores.fechaInicio}">
                                   aria-invalid="true"
                                   aria-describedby="error-fechaInicio"
                               </c:if>>

                        <c:if test="${not empty errores.fechaInicio}">
                            <span class="error-campo" id="error-fechaInicio">
                                <c:out value="${errores.fechaInicio}" />
                            </span>
                        </c:if>
                    </div>

                    <div class="campo ${not empty errores.fechaFin ? 'campo-error' : ''}">
                        <label for="fechaFin">Fecha final (devolución)</label>

                        <input id="fechaFin"
                               name="fechaFin"
                               type="date"
                               value="<c:out value='${alquiler.fechaFin}' />"
                               required
                               <c:if test="${not empty errores.fechaFin}">
                                   aria-invalid="true"
                                   aria-describedby="error-fechaFin"
                               </c:if>>

                        <c:if test="${not empty errores.fechaFin}">
                            <span class="error-campo" id="error-fechaFin">
                                <c:out value="${errores.fechaFin}" />
                            </span>
                        </c:if>
                    </div>

                </div>

                <span class="ayuda">
                    La fecha final no se cobra: del 2 al 5 son 3 días. Un vehículo devuelto el día 5
                    puede alquilarse ese mismo día.
                </span>

                <div class="resumen-alquiler"
                     id="resumen-alquiler"
                     data-vehiculo-guardado="<c:out value='${vehiculoGuardado}' />"
                     data-tarifa-guardada="<c:out value='${tarifaGuardada}' />">

                    <div>
                        <span>Días</span>
                        <strong id="resumen-dias">—</strong>
                    </div>

                    <div>
                        <span>Tarifa por día</span>
                        <strong id="resumen-tarifa">—</strong>
                    </div>

                    <div>
                        <span>Importe estimado</span>
                        <strong id="resumen-total">—</strong>
                    </div>
                </div>

                <span class="ayuda">
                    El importe final lo calcula el servidor con la tarifa guardada. Si editas un alquiler
                    y no cambias el vehículo, se conserva la tarifa con la que se registró.
                </span>

                <div class="acciones-formulario">

                    <button type="submit" class="boton boton-principal">
                        <c:out value="${edicion ? 'Guardar cambios' : 'Registrar alquiler'}" />
                    </button>

                    <a href="${alquileresUrl}" class="boton boton-secundario">
                        Cancelar
                    </a>

                </div>

            </form>

        </section>

    </main>

    <footer class="pie-pagina">
        RentATroka · Gestión de alquileres
    </footer>

</body>
</html>