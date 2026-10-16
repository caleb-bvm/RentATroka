<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="inicioUrl" value="/inicio" />
<c:url var="logoutUrl" value="/logout" />
<header class="cabecera">
    <a class="marca" href="${inicioUrl}" aria-label="RentATroka, inicio">
        <span class="marca-icono" aria-hidden="true">RT</span> RentATroka<span class="punto">.</span>
    </a>
    <div class="cuenta">
        <span class="nombre-cuenta"><c:out value="${sessionScope.usuarioSesion.nombre}" /></span>
        <form action="${logoutUrl}" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <button type="submit" class="boton boton-secundario">Cerrar sesión</button>
        </form>
    </div>
</header>

