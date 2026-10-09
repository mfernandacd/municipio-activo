# Municipio Activo

Portal web municipal para registrarse, iniciar sesión y enviar reclamos.

## Abrir el sitio

[https://municipio-s25r.onrender.com/](https://municipio-s25r.onrender.com/)

Los visitantes solo necesitan esta dirección. No tienen que instalar programas, iniciar Apache ni configurar la base de datos.

## Cómo funciona

- **Sitio web y backend:** Render.
- **Base de datos MySQL:** Clever Cloud.
- **Conexión a la base:** el backend en Render usa las credenciales guardadas en las variables privadas del servicio. No se exponen a los visitantes ni deben guardarse en el repositorio.
- **Inicio de sesión:** la sesión se conserva entre páginas mediante una cookie segura.

## Límites del plan gratuito

Render Free duerme el sitio después de 15 minutos sin visitas. La primera visita posterior puede tardar aproximadamente un minuto mientras el servicio vuelve a arrancar. Las imágenes cargadas se guardan en almacenamiento temporal y se pueden borrar cuando el servicio se reinicia. El uso gratuito de Render no elimina posibles costos de la base MySQL en Clever Cloud.

## Cambios y nuevo despliegue

Render despliega el proyecto desde el repositorio conectado en GitHub y utiliza el [`Dockerfile`](Dockerfile) de la raíz. Para actualizar el sitio, guarda y sube los cambios a la rama que Render tiene configurada; Render inicia un nuevo despliegue. En la configuración privada de Render, mantén `SPRING_PROFILES_ACTIVE=cloud`, `SESSION_COOKIE_SECURE=true` y las variables `MYSQL_ADDON_HOST`, `MYSQL_ADDON_PORT`, `MYSQL_ADDON_DB`, `MYSQL_ADDON_USER` y `MYSQL_ADDON_PASSWORD`.

Nunca publiques la contraseña de MySQL en GitHub, en el frontend ni en este archivo. Si se comparte accidentalmente, cámbiala en Clever Cloud y actualiza el valor privado en Render.

## Desarrollo local (opcional)

Para probar el proyecto en Windows, ejecuta [`Iniciar Municipio.bat`](<Iniciar Municipio.bat>), introduce los datos de conexión MySQL y abre [http://localhost:8080/](http://localhost:8080/). Deja abierta la ventana del backend mientras trabajas. No uses `file://` ni la dirección de Apache/XAMPP: el inicio de sesión necesita el backend.

## Base de datos y roles

El registro crea cuentas de ciudadano; las contraseñas aceptan de 6 a 8 letras o números. Si todavía no están creadas las tablas, ejecuta una vez [`crear-esquema-clever-cloud.sql`](frontend/Sql/crear-esquema-clever-cloud.sql) desde la consola de la base. Para asignar permisos administrativos, primero registra las cuentas y luego usa [`asignar-roles-municipales.sql`](frontend/Sql/asignar-roles-municipales.sql).

## Pruebas

Desde PowerShell, en la carpeta del proyecto:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
```
