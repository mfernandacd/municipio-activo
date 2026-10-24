# Municipio Activo

Portal web municipal para registrar y seguir reclamos, consultar noticias y administrar el contenido publicado. El backend Spring Boot sirve también los archivos estáticos del frontend.

La pantalla de reclamos muestra una vista previa de confirmación por correo; el proyecto no incluye todavía un servicio SMTP ni envía mensajes reales.

## Tecnologías y requisitos

- JDK 21.
- MySQL 8 compatible.
- Maven Wrapper incluido en `backend/`; no hace falta instalar Maven.
- Docker es opcional para ejecutar la imagen de despliegue.
- Las nuevas cuentas deben usar frases de contraseña de 12 a 72 bytes UTF-8; las cuentas previas mantienen acceso con su clave actual.

La aplicación usa Spring Boot, Spring Web MVC, Spring Security, Spring Data JPA, Jakarta Validation, MySQL Connector/J, HTML, Bootstrap, Tailwind CSS y JavaScript.

## Configurar una base nueva

1. Crea una base MySQL con `utf8mb4`:

   ```sql
   CREATE DATABASE municipio_activo
       CHARACTER SET utf8mb4
       COLLATE utf8mb4_unicode_ci;
   ```

2. Ejecuta [`frontend/Sql/schema.sql`](frontend/Sql/schema.sql) sobre esa base. El esquema crea las tablas vacías y no inserta cuentas ni reclamos.

3. Configura la conexión mediante variables de entorno. No guardes contraseñas en Git:

   ```text
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=municipio_activo
   DB_USER=usuario_app
   DB_PASSWORD=contraseña_local
   ```

   Si la página se sirve desde otro origen de desarrollo, añade su origen exacto en `FRONTEND_ORIGINS`, por ejemplo `http://localhost:5500`. No uses patrones comodín. En el despliegue habitual, frontend y API comparten origen y esta variable puede quedar vacía.

4. Para crear una cuenta administrativa, primero regístrala desde el portal y luego ejecuta [`frontend/Sql/asignar-rol-administrativo.sql`](frontend/Sql/asignar-rol-administrativo.sql) tras sustituir el correo de ejemplo.

[`frontend/Sql/noticias_iniciales.sql`](frontend/Sql/noticias_iniciales.sql) carga noticias públicas de ejemplo y se puede ejecutar después del esquema. [`frontend/Sql/migracion_tabla_imagenes.sql`](frontend/Sql/migracion_tabla_imagenes.sql) es una migración para bases antiguas; no se necesita en una base recién creada con `schema.sql`.

No se debe versionar un volcado de producción: contiene datos personales y hashes de contraseña. Si una copia anterior ya llegó a un remoto, hay que purgarla del historial y rotar cualquier credencial relacionada.

## Iniciar localmente

Desde PowerShell en la raíz del proyecto, configura las variables de la base local y ejecuta:

```powershell
$env:DB_HOST = 'localhost'
$env:DB_PORT = '3306'
$env:DB_NAME = 'municipio_activo'
$env:DB_USER = 'usuario_app'
$env:DB_PASSWORD = 'contraseña_local'
.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:run
```

Abre [http://localhost:8080/](http://localhost:8080/). También puedes iniciar [`Iniciar Municipio.bat`](<Iniciar Municipio.bat>), que solicita los datos MySQL y ejecuta el backend. No abras las páginas con `file://`: las rutas de API, la sesión y la protección CSRF requieren que el sitio se sirva por HTTP.

## API principal

- `POST /api/auth/registro`, `POST /api/auth/login`, `GET /api/auth/session`, `POST /api/auth/logout`.
- `GET /api/auth/csrf` entrega el token CSRF que usan las operaciones de escritura.
- `POST /api/reclamos` registra un reclamo; `GET /api/reclamos/seguimiento/{código}` devuelve su seguimiento público.
- `GET /api/reclamos` y `PUT /api/reclamos/{id}/estado` requieren rol administrativo.
- `GET /api/noticias` y `GET /api/noticias/{slug}` son públicos; crear, modificar y eliminar noticias requiere rol administrativo.

## Pruebas

Ejecuta la suite con H2 en memoria:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
```

Las pruebas no necesitan una instancia MySQL. Para validar conexión, credenciales, permisos y compatibilidad del esquema real, inicia la aplicación contra la base configurada.

## Despliegue

El [`Dockerfile`](Dockerfile) compila el backend y empaqueta los recursos de `frontend/`. En Render configura `SPRING_PROFILES_ACTIVE=cloud`, `PORT`, `MYSQL_ADDON_HOST`, `MYSQL_ADDON_PORT`, `MYSQL_ADDON_DB`, `MYSQL_ADDON_USER`, `MYSQL_ADDON_PASSWORD` y `SESSION_COOKIE_SECURE=true`. Mantén `FRONTEND_ORIGINS` vacío cuando el frontend se sirva desde el mismo origen; si está separado, configura solo el origen HTTPS exacto. Si frontend y API están en sitios distintos, configura además `SESSION_COOKIE_SAME_SITE=None`; en el despliegue habitual bajo el mismo sitio se conserva `lax`.

Las imágenes subidas se guardan en `uploads/`. El disco del contenedor puede ser temporal, por lo que una reinicialización puede borrar esos archivos; usa almacenamiento persistente si deben conservarse.
