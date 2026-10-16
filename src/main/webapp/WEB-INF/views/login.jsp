<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="cssUrl" value="/assets/css/estilos.css" />
<c:url var="loginUrl" value="/login" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión | RentATroka</title>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body class="pagina-acceso">
    <main class="acceso">
        <section class="presentacion" aria-labelledby="titulo-presentacion">
            <a class="marca marca-clara" href="${loginUrl}" aria-label="RentATroka, inicio">
                <span class="marca-icono" aria-hidden="true">RT</span> RentATroka<span class="punto">.</span>
            </a>
            <div class="presentacion-contenido">
                <span class="etiqueta">GESTIÓN DE ALQUILERES</span>
                <h1 id="titulo-presentacion">Todo listo para<br> el próximo <span>viaje.</span></h1>
                <p>Un solo lugar para organizar tus clientes, vehículos y alquileres.</p>
                <div class="ruta-ilustrada" aria-hidden="true"><span class="ruta-inicio"></span><span class="ruta-final"></span></div>
            </div>
            <p class="pie-presentacion">Cada vehículo, cada cliente, cada kilómetro.</p>
        </section>
        <section class="formulario-acceso" aria-labelledby="titulo-acceso">
            <div class="formulario-contenido">
                <span class="etiqueta etiqueta-naranja">BIENVENIDO DE NUEVO</span>
                <h2 id="titulo-acceso">Inicia sesión</h2>
                <p class="texto-secundario">Ingresa tus credenciales para continuar.</p>
                <c:if test="${param.salida eq '1'}">
                    <div class="aviso aviso-exito" role="status">Cerraste tu sesión correctamente.</div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="aviso aviso-error" role="alert"><c:out value="${error}" /></div>
                </c:if>
                <form action="${loginUrl}" method="post" class="formulario">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <div class="campo">
                        <label for="usuario">Usuario</label>
                        <input id="usuario" name="usuario" type="text" value="<c:out value='${usuario}'/>"
                               required maxlength="50" autocomplete="username" autocapitalize="none" spellcheck="false">
                    </div>
                    <div class="campo">
                        <label for="clave">Contraseña</label>
                        <input id="clave" name="clave" type="password" required maxlength="128" autocomplete="current-password">
                    </div>
                    <button class="boton boton-principal" type="submit">Ingresar <span aria-hidden="true">→</span></button>
                </form>
                <p class="ayuda-acceso">Acceso exclusivo para el personal de RentATroka.</p>
            </div>
            <p class="pie-acceso">RentATroka · Administración de vehículos</p>
        </section>
    </main>
</body>
</html>

