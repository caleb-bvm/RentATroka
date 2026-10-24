# RentATroka

Sistema web académico de gestión de alquileres de vehículos. Arquitectura MVC con JavaBeans, DAO/JDBC, Servlets y JSP/JSTL; MySQL como base de datos y Apache Tomcat como servidor.

## Estado de nuestra parte

- [x] Proyecto Maven WAR preparado para Java 17 o superior y Tomcat 11.
- [x] Conexión centralizada a MySQL, esquema relacional y datos de prueba.
- [x] Login, logout, sesión de 30 minutos y protección de rutas internas.
- [x] Contraseñas almacenadas como hash, consultas preparadas y formularios protegidos.
- [x] Pantallas responsive de acceso e inicio; mensajes de error y confirmación.
- [x] Diagramas ER y MVC, con fuentes editables y explicación para la defensa.
- [ ] CRUD de clientes, vehículos y alquileres: correspondientes a los otros integrantes.
- [ ] Integración del filtro de vehículos y del listado de alquileres con JOIN.
- [ ] Documento final de 3-5 páginas con capturas de las funciones completas.
- [ ] Publicar en GitHub con aportes de los cuatro integrantes y ensayar la defensa.

## Ejecutar

Requisitos: JDK 17 o superior, Maven, Tomcat 11 y MySQL 8.4. El proyecto compila con `--release 17`. NetBeans incluye una distribución de Maven y puede ejecutar las mismas tareas desde **Clean and Build**.

1. Preparar MySQL con una de estas opciones:
   - **Docker Desktop:** desde la carpeta del proyecto ejecutar `docker compose -p rentatroka up -d --wait`. Crea una instancia local en el puerto 3306 e importa los dos scripts automáticamente en su primer arranque. Conserva los datos en un volumen.
   - **MySQL instalado:** ejecutar, en orden, `database/01-esquema.sql` y `database/02-datos-prueba.sql` desde MySQL Workbench o el cliente de MySQL. Usar una cuenta con acceso a la base `rentatroka`.
2. Copiar `src/main/resources/db.properties.example` como `src/main/resources/db.properties`. Para Docker, los valores de ejemplo ya coinciden; para MySQL instalado, adaptar usuario, contraseña y dirección. El archivo local está excluido de Git.
3. Ejecutar `mvn clean verify`. El resultado es `target/RentATroka.war`.
4. En NetBeans, seleccionar Tomcat 11 como servidor y ejecutar el proyecto. También se puede copiar el WAR a la carpeta `webapps` de Tomcat y arrancarlo.
5. Abrir `http://localhost:8080/RentATroka/` (ajustar el puerto si Tomcat usa otro).

Cuenta de demostración del sistema: **admin / Admin123!**. Es diferente de la cuenta de MySQL.

En Windows también se incluye `scripts/Iniciar-Local.ps1`: después de compilar, inicia Tomcat en una carpeta aislada `.local/tomcat`, usando `CATALINA_HOME`, sin cambiar la instalación de Tomcat. Abre `http://127.0.0.1:18080/RentATroka/login`; permite elegir otro puerto con `-Puerto`. Para detener ese servidor, ejecutar `scripts/Detener-Local.ps1`. Después de recompilar cambios, detenerlo e iniciarlo otra vez para desplegar el WAR actualizado.

También se puede configurar la conexión con variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD` al iniciar Tomcat; tienen prioridad sobre `db.properties`. La contraseña del contenedor puede personalizarse mediante `DB_PASSWORD` y `MYSQL_ROOT_PASSWORD` en un archivo `.env` local. Debe coincidir con la configuración JDBC. Los valores predeterminados de Compose son exclusivamente para desarrollo local.

Para detener la instancia de MySQL conservando sus datos: `docker compose -p rentatroka stop`. Los scripts no eliminan tablas ni registros; los scripts de inicio de Docker solo se importan automáticamente al crear un volumen nuevo.

## Estructura

```text
database/                      Creación de tablas y datos de prueba
docs/diagramas/                Diagramas ER y MVC (SVG, PNG y Mermaid)
docs/diseno-y-defensa.md        Justificación y explicación de nuestra parte
docs/acuerdos-equipo.md         Contrato para desarrollar los tres módulos
docs/verificacion.md            Resultados de pruebas y capturas de esta etapa
src/main/java/com/mycompany/rentatroka/
    config/                    ConexionBD
    modelo/                    JavaBeans
    dao/                       Consultas JDBC
    servicio/                  Reglas de negocio del modelo
    controlador/               Servlets HTTP
    filtro/                    Control de sesión y formularios
    seguridad/                 Hash de contraseñas y token de formulario
src/main/webapp/
    assets/css/                Estilos compartidos
    WEB-INF/views/             JSP protegidas, sin SQL ni scriptlets
src/test/java/                 Pruebas de claves y autenticación
```

## Verificar el acceso antes de integrar

- Entrar directamente a `/inicio` sin sesión: debe enviar a `/login`.
- Enviar campos vacíos o demasiado largos: debe indicar el error; el servidor también valida aunque se omita la validación del navegador.
- Probar una contraseña incorrecta: debe rechazarla sin revelar si existe el usuario.
- Ingresar con la cuenta de prueba: debe mostrar el inicio y el nombre de la cuenta.
- Cerrar sesión y volver a `/inicio`: debe pedir las credenciales otra vez.
- Enviar un formulario sin su token: debe rechazarlo y explicar cómo continuar.
- Detener MySQL e intentar ingresar: debe mostrar un mensaje comprensible, sin exponer datos internos.

## Referencias técnicas

- [Tomcat 11 y sus especificaciones Servlet/JSP](https://tomcat.apache.org/tomcat-11.0-doc/index.html).
- [Java mínimo de Tomcat 11](https://tomcat.apache.org/migration-11.0.html).
- [Jakarta Standard Tag Library 3.0](https://jakarta.ee/specifications/tags/3.0/).
- [MySQL Connector/J con Maven](https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html).

