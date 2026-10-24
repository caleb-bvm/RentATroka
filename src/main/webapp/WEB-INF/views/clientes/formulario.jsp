<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="cssClientesUrl" value="/assets/css/clientes.css" />
<c:url var="clientesUrl" value="/clientes" />
<c:set var="edicion" value="${cliente.id > 0}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${edicion ? 'Editar cliente' : 'Nuevo cliente'}" /> | RentATroka</title>
    <link rel="stylesheet" href="${cssUrl}">
    <link rel="stylesheet" href="${cssClientesUrl}">
</head>
<body>
    <jsp:include page="/WEB-INF/views/fragmentos/cabecera.jsp" />
    <main class="contenedor modulo-clientes">
        <a class="enlace-volver" href="${clientesUrl}">← Clientes</a>
        <div class="encabezado-modulo">
            <div>
                <span class="etiqueta etiqueta-naranja">MÓDULO 01</span>
                <h1><c:out value="${edicion ? 'Editar cliente' : 'Nuevo cliente'}" /></h1>
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
            <form class="formulario" action="${clientesUrl}" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="accion" value="${edicion ? 'actualizar' : 'guardar'}">
                <c:if test="${edicion}">
                    <input type="hidden" name="id" value="${cliente.id}">
                </c:if>

                <div class="campo ${not empty errores.nombre ? 'campo-error' : ''}">
                    <label for="nombre">Nombre completo</label>
                    <input id="nombre" name="nombre" type="text"
                           value="<c:out value='${cliente.nombre}' />"
                           required maxlength="100" autocomplete="name"
                           aria-describedby="ayuda-nombre<c:if test='${not empty errores.nombre}'> error-nombre</c:if>"
                           <c:if test="${not empty errores.nombre}">aria-invalid="true"</c:if>>
                    <span class="ayuda" id="ayuda-nombre">Máximo 100 caracteres.</span>
                    <c:if test="${not empty errores.nombre}">
                        <span class="error-campo" id="error-nombre"><c:out value="${errores.nombre}" /></span>
                    </c:if>
                </div>

                <div class="campo ${not empty errores.documento ? 'campo-error' : ''}">
                    <label for="documento">Documento</label>
                    <input id="documento" name="documento" type="text"
                           value="<c:out value='${cliente.documento}' />"
                           required maxlength="30" autocomplete="off" spellcheck="false"
                           aria-describedby="ayuda-documento<c:if test='${not empty errores.documento}'> error-documento</c:if>"
                           <c:if test="${not empty errores.documento}">aria-invalid="true"</c:if>>
                    <span class="ayuda" id="ayuda-documento">Máximo 30 caracteres. Cada documento debe pertenecer a un solo cliente.</span>
                    <c:if test="${not empty errores.documento}">
                        <span class="error-campo" id="error-documento"><c:out value="${errores.documento}" /></span>
                    </c:if>
                </div>

                <div class="campo ${not empty errores.telefono ? 'campo-error' : ''}">
                    <label for="telefono">Teléfono</label>
                    <input id="telefono" name="telefono" type="tel"
                           value="<c:out value='${cliente.telefono}' />"
                           required maxlength="20" autocomplete="tel"
                           aria-describedby="ayuda-telefono<c:if test='${not empty errores.telefono}'> error-telefono</c:if>"
                           <c:if test="${not empty errores.telefono}">aria-invalid="true"</c:if>>
                    <span class="ayuda" id="ayuda-telefono">Máximo 20 caracteres.</span>
                    <c:if test="${not empty errores.telefono}">
                        <span class="error-campo" id="error-telefono"><c:out value="${errores.telefono}" /></span>
                    </c:if>
                </div>

                <div class="acciones-formulario">
                    <button type="submit" class="boton boton-principal">
                        <c:out value="${edicion ? 'Guardar cambios' : 'Registrar cliente'}" />
                    </button>
                    <a class="boton boton-secundario" href="${clientesUrl}">Cancelar</a>
                </div>
            </form>
        </section>
    </main>
    <footer class="pie-pagina">RentATroka · Gestión de alquileres de vehículos</footer>
</body>
</html>
