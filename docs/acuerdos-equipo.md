# Contrato para integrar los módulos

Nuestra parte entrega la base, la autenticación y ambos diagramas. Los otros integrantes desarrollan Clientes, Vehículos y Alquileres sobre esta estructura. Mantener estos nombres evita tener que adaptar tablas, rutas y atributos al integrar.

## Esquema y tipos

| Tabla | Columnas | Tipo Java sugerido |
| --- | --- | --- |
| clientes | id, nombre, documento, telefono | `int`, `String` |
| vehiculos | id, placa, marca, modelo, tarifa_diaria | `int`, `String`, `BigDecimal` |
| alquileres | id, cliente_id, vehiculo_id, fecha_inicio, fecha_fin, tarifa_diaria_aplicada | `int`, `LocalDate`, `BigDecimal` |

Los IDs los genera MySQL. Cada JavaBean usa atributos privados, constructor vacío y getters/setters; en Java se escribe, por ejemplo, `tarifaDiariaAplicada` y en SQL `tarifa_diaria_aplicada`. Cada alquiler tiene exactamente un cliente y un vehículo. Usuarios es una tabla de apoyo para la autenticación, sin CRUD administrativo dentro del alcance acordado.

## Rutas y archivos

| Módulo | Ruta del Servlet | Bean / DAO / Servlet | Carpeta de JSP |
| --- | --- | --- | --- |
| Clientes | `/clientes` | Cliente / ClienteDAO / ClienteServlet | `WEB-INF/views/clientes/` |
| Vehículos | `/vehiculos` | Vehiculo / VehiculoDAO / VehiculoServlet | `WEB-INF/views/vehiculos/` |
| Alquileres | `/alquileres` | Alquiler / AlquilerDAO / AlquilerServlet | `WEB-INF/views/alquileres/` |

Cada Servlet puede usar GET con `accion=listar`, `nuevo` o `editar`, y POST con `accion=guardar`, `actualizar` o `eliminar`. Una petición GET no modifica datos. Después de un POST exitoso, redirigir al listado para evitar duplicados al actualizar el navegador. Antes de eliminar, pedir confirmación en la vista.

Los DAO usan exclusivamente `ConexionBD.obtenerConexion()`, `PreparedStatement` y `try-with-resources`. El Servlet invoca al DAO o a un servicio del modelo; no contiene SQL ni genera HTML. Las JSP muestran datos con JSTL y escapan las entradas con `<c:out>`; no usan scriptlets Java ni etiquetas SQL.

## Elementos compartidos

- La identidad autenticada se encuentra en `sessionScope.usuarioSesion` y es un `Usuario` sin hash de contraseña.
- `AutenticacionFilter` protege automáticamente las rutas nuevas. No es necesario repetir la comprobación de sesión en cada Servlet.
- **Todos los formularios POST deben incluir:**

```jsp
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
```

- Usar la biblioteca `jakarta.tags.core`, `<c:forEach>` en listados y `<c:url>` para generar rutas que funcionen bajo `/RentATroka`.
- Incluir `WEB-INF/views/fragmentos/cabecera.jsp` en las pantallas internas y la hoja `assets/css/estilos.css`.
- Validar también en servidor: datos requeridos, longitudes máximas, formato, tarifa positiva y existencia de los IDs relacionados. Para dinero usar `BigDecimal`, sin confiar en el total enviado desde el navegador.
- Mostrar un mensaje al guardar, actualizar y eliminar. Para mensajes tras un redirect pueden guardar temporalmente el mensaje en sesión y retirarlo al mostrarlo.

## Reglas acordadas de alquiler

La fecha final es exclusiva: del 2 al 5 se cobran **3 días**. La fecha final debe ser posterior a la inicial. Esto permite que un vehículo que se devuelve el día 5 vuelva a alquilarse ese mismo día.

Al registrar, copiar la tarifa actual del vehículo a `tarifa_diaria_aplicada`. Esta tarifa es un dato histórico del alquiler; al editar fechas se conserva, y al cambiar de vehículo se toma la del vehículo nuevo. El total se calcula como días por tarifa, sin almacenarlo de nuevo. Los cambios de tarifa de un vehículo no alteran los alquileres anteriores.

Antes de guardar o actualizar, comprobar cruces del mismo vehículo. Al editar, excluir el ID del propio alquiler. Un conflicto cumple `fecha_inicio < nueva_fecha_fin AND fecha_fin > nueva_fecha_inicio`. La comprobación y la escritura deben hacerse en una misma transacción, bloqueando primero el vehículo con `SELECT ... FOR UPDATE` para evitar dos altas simultáneas. El CHECK de fechas no impide cruces entre registros.

Si un cliente o vehículo ya está relacionado con alquileres, MySQL impide borrarlo. El módulo debe explicar el motivo y permitir borrar registros sin relaciones. Los datos de prueba incluyen un cliente y un vehículo sin alquileres para demostrar el borrado.

Este SELECT es el punto de partida del listado combinado; debe integrarlo el responsable de alquileres en su DAO y mostrarlo en una JSP:

```sql
SELECT a.id, c.nombre AS cliente, v.placa, v.marca, v.modelo,
       a.fecha_inicio, a.fecha_fin, a.tarifa_diaria_aplicada,
       DATEDIFF(a.fecha_fin, a.fecha_inicio) AS dias,
       DATEDIFF(a.fecha_fin, a.fecha_inicio) * a.tarifa_diaria_aplicada AS total
FROM alquileres a
JOIN clientes c ON c.id = a.cliente_id
JOIN vehiculos v ON v.id = a.vehiculo_id
ORDER BY a.fecha_inicio DESC, a.id DESC;
```

## GitHub y entrega

Cada integrante debe subir sus propios cambios con su cuenta. La persona que hace la integración conserva ese historial. GitHub todavía debe configurarse y publicarse; no hay un repositorio remoto creado por esta base. Cada módulo debe entregarse ejecutable, con sus validaciones, mensajes y capturas, y su responsable debe poder explicar las tres capas del MVC.

