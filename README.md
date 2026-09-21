# Municipio Activo

Sitio web institucional para la Municipalidad de Serranoble. Incluye información pública, noticias, servicios, accesibilidad y un formulario para registrar reclamos municipales.

## Funcionalidades

- Página institucional con navegación, noticias y trámites.
- Formulario de reclamos con categoría, dirección, ubicación de Google Maps, descripción e imagen opcional.
- Validación de campos y confirmación del envío desde el frontend.
- Persistencia de reclamos y usuarios en MySQL mediante Spring Data JPA.
- Inicio de sesión administrativo con contraseñas almacenadas como hashes BCrypt.
- Filtro de reclamos por estado mediante la API REST.
- Panel de accesibilidad para personalizar fuente, contraste, espaciado y orientación.

## Estructura del proyecto

```text
frontend/
	index.html                 Página principal
	login.html                 Inicio de sesión
	reclamos-municipal.html    Formulario de reclamos
	Css/                       Hojas de estilo
	js/                        Lógica del frontend
	assets/                    Imágenes y recursos gráficos
backend/
	src/main/java/...          Aplicación Spring Boot
	src/main/resources/        Configuración de la aplicación
	src/test/                  Pruebas del backend
```

## Archivos Java

Los archivos Java del backend se encuentran en `backend/src/main/java/com/municipioactivo/backend/`:

| Archivo | Responsabilidad |
| --- | --- |
| `BackendApplication.java` | Punto de entrada que inicia la aplicación Spring Boot. |
| `AuthController.java` | Recibe las credenciales del login, valida el usuario y crea la sesión HTTP. |
| `SecurityConfig.java` | Configura BCrypt, CORS y las reglas de acceso a los endpoints. |
| `DataInitializer.java` | Crea o actualiza el usuario administrador y carga reclamos de ejemplo al iniciar el backend. |
| `Usuario.java` | Define la entidad `Usuario` y su tabla `usuarios`; también contiene `UsuarioRepository`, que busca usuarios por correo. |
| `Reclamo.java` | Define la entidad `Reclamo` y su tabla `reclamos`; también contiene el repositorio y el servicio para guardar y consultar reclamos. |
| `ReclamoController.java` | Expone los endpoints para crear reclamos, subir imágenes y consultar reclamos por estado. |

La prueba `backend/src/test/java/com/municipioactivo/backend/BackendApplicationTests.java` verifica que el contexto completo de Spring Boot pueda iniciar correctamente.

## Requisitos

- JDK 21, incluyendo `javac`.
- XAMPP con los módulos **Apache** y **MySQL** disponibles.
- Base de datos `municipio-activo` creada en MySQL.
- Navegador web.

XAMPP proporciona Apache para servir el frontend, MySQL para la persistencia y phpMyAdmin para administrar la base de datos. La conexión del backend está definida en [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties): MySQL usa `localhost:3306`, el usuario de desarrollo es `root` y la contraseña está vacía. Si tu instalación de XAMPP usa otras credenciales, actualiza allí `spring.datasource.username` y `spring.datasource.password`.

La base de datos se administra desde [phpMyAdmin](http://localhost/phpmyadmin/index.php?route=/database/structure&db=municipio-activo). Si el enlace no abre, inicia Apache y MySQL desde el panel de control de XAMPP.

## Ejecución

1. Abre el panel de control de XAMPP e inicia **Apache** y **MySQL**.
2. Verifica en phpMyAdmin que exista la base de datos `municipio-activo`. Si aún no existe, créala antes de iniciar el backend.
3. Coloca o copia la carpeta `frontend/` dentro de la carpeta pública de Apache, normalmente `C:\xampp\htdocs\municipio-activo\frontend\`.
4. Desde `backend/`, inicia la API:

	 ```powershell
	 .\mvnw.cmd spring-boot:run
	 ```

	 La API queda disponible en `http://localhost:8080`.

5. Abre el frontend desde Apache en `http://localhost/municipio-activo/frontend/index.html` o `http://localhost/municipio-activo/frontend/login.html`

No abras las páginas directamente con `file://`: el frontend necesita ejecutarse mediante Apache para probar correctamente las llamadas a la API.

## Acceso de desarrollo

Al iniciar el backend, [DataInitializer.java](backend/src/main/java/com/municipioactivo/backend/DataInitializer.java) crea o actualiza una cuenta administrativa de desarrollo:

- Correo: `admin@municipio.gob.ar`
- Contraseña: `Municipio123!`

La contraseña no se guarda en texto plano: el backend genera un hash BCrypt y lo almacena en `usuarios.password_hash`.

## API principal

| Método | Endpoint | Uso |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Iniciar sesión y crear la sesión HTTP |
| `GET` | `/api/reclamos` | Obtener todos los reclamos |
| `GET` | `/api/reclamos/estado/{estado}` | Filtrar reclamos por estado |
| `POST` | `/api/reclamos` | Crear un reclamo usando `multipart/form-data` |

El endpoint de creación recibe una parte JSON llamada `reclamo` y, opcionalmente, una parte `imagen`. Las imágenes se guardan en la carpeta `uploads/` del backend.

## Pruebas

Desde `backend/` ejecuta:

```powershell
.\mvnw.cmd test
```
