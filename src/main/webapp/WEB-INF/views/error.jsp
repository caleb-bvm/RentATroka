<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="inicioUrl" value="/inicio" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>No pudimos completar la solicitud | RentATroka</title>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
    <main class="contenedor contenedor-error">
        <span class="etiqueta etiqueta-naranja">RENTATROKA</span>
        <c:choose>
            <c:when test="${requestScope['jakarta.servlet.error.status_code'] eq 403}">
                <h1>El formulario venció</h1>
                <p>Vuelve al inicio y abre nuevamente el formulario para continuar.</p>
            </c:when>
            <c:when test="${requestScope['jakarta.servlet.error.status_code'] eq 404}">
                <h1>No encontramos esta página</h1>
                <p>Revisa la dirección o vuelve al inicio.</p>
            </c:when>
            <c:otherwise>
                <h1>No pudimos completar la solicitud</h1>
                <p>Inténtalo de nuevo en unos momentos.</p>
            </c:otherwise>
        </c:choose>
        <a class="boton boton-principal" href="${inicioUrl}">Volver al inicio</a>
    </main>
</body>
</html>

