package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "FRONTEND_ORIGINS=https://frontend.example.com")
@ActiveProfiles("test")
// Verifica que el contexto completo de Spring Boot pueda inicializarse correctamente.
class BackendApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ReclamoRepository reclamoRepository;

	@Autowired
	private NoticiaRepository noticiaRepository;

	@Test
	// Comprueba que la aplicación arranque sin errores de configuración.
	void contextLoads() {
	}

	@Test
	void servesLoginPageFromBackend() throws Exception {
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/login.html")).GET().build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("municipio-api-base-url"));
	}

	@Test
	void servesAllFrontendPagesAndSharedStaticResources() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		for (String page : new String[] {
				"/", "/index.html", "/noticias.html", "/noticia-detalle.html", "/login.html",
				"/administrar-noticias.html", "/reclamos-municipal.html", "/seguimiento.html",
				"/servicios.html", "/vista-reclamo.html", "/control_interactivo_en_interfaz.html",
				"/interfaz_de_consultas.html", "/Css/styles.css", "/Css/noticias.css", "/js/api.js",
				"/js/componentes-comunes.js", "/assets/logo/Logo.jpeg"
		}) {
			var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + page)).GET().build();
			var response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), page);
			if (page.equals("/seguimiento.html")) {
				assertTrue(response.body().contains("tracking-form"));
			}
		}
	}

	@Test
	void registersUserThroughHttp() throws Exception {
		String email = "usuario-" + UUID.randomUUID() + "@example.com";
		String body = "{\"nombre\":\"Usuario de prueba\",\"email\":\"" + email
				+ "\",\"password\":\"ClaveSegura26!\"}";
		HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
		CsrfInfo csrf = fetchCsrf(client);
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/registro"))
				.header("Content-Type", "application/json")
				.header(csrf.headerName(), csrf.token())
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(201, response.statusCode());
		assertTrue(response.body().contains(email));
	}

	@Test
	void rejectsUnsafePublicRequestsWithoutCsrfToken() throws Exception {
		String body = "{\"categoria\":\"Bacheo\",\"direccion\":\"Calle Belgrano 450\","
				+ "\"descripcion\":\"Hay un bache que dificulta el paso.\"}";
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();

		assertEquals(403, HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
	}

	@Test
	void doesNotAllowUnconfiguredRenderSubdomainsThroughCors() throws Exception {
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/session"))
				.header("Origin", "https://attacker.onrender.com")
				.header("Access-Control-Request-Method", "POST")
				.method("OPTIONS", HttpRequest.BodyPublishers.noBody())
				.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(403, response.statusCode());
		assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
	}

	@Test
	void allowsConfiguredFrontendToSendCsrfHeader() throws Exception {
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/registro"))
				.header("Origin", "https://frontend.example.com")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "content-type,x-csrf-token")
				.method("OPTIONS", HttpRequest.BodyPublishers.noBody())
				.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertEquals("https://frontend.example.com",
				response.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
		assertEquals("true", response.headers().firstValue("Access-Control-Allow-Credentials").orElseThrow());
		assertTrue(response.headers().firstValue("Access-Control-Allow-Headers")
				.orElse("").toLowerCase().contains("x-csrf-token"));
	}

	@Test
	void protectsClaimReadingAndUpdatesFromAnonymousRequests() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		var readRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos"))
				.GET()
				.build();
		var updateRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos/1/estado"))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString("{\"estado\":\"RESUELTO\"}"))
				.build();

		assertEquals(403, client.send(readRequest, HttpResponse.BodyHandlers.ofString()).statusCode());
		assertEquals(403, client.send(updateRequest, HttpResponse.BodyHandlers.ofString()).statusCode());
	}

	@Test
	void allowsOnlyAdministrativeUsersToReadAndUpdateClaims() throws Exception {
		HttpClient citizenClient = authenticatedClient("ciudadano");
		CsrfInfo citizenCsrf = fetchCsrf(citizenClient);
		String citizenCsrfHeader = citizenCsrf.headerName();
		String citizenCsrfToken = citizenCsrf.token();
		var citizenRead = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos")).GET().build();
		var citizenUpdate = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos/1/estado"))
				.header("Content-Type", "application/json")
				.header(citizenCsrfHeader, citizenCsrfToken)
				.PUT(HttpRequest.BodyPublishers.ofString("{\"estado\":\"RESUELTO\"}"))
				.build();
		assertEquals(403, citizenClient.send(citizenRead, HttpResponse.BodyHandlers.ofString()).statusCode());
		assertEquals(403, citizenClient.send(citizenUpdate, HttpResponse.BodyHandlers.ofString()).statusCode());

		HttpClient administrativeClient = authenticatedClient("administrativo");
		CsrfInfo administrativeCsrf = fetchCsrf(administrativeClient);
		var administrativeRead = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos"))
				.GET()
				.build();
		var administrativeUpdate = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/reclamos/999999/estado"))
				.header("Content-Type", "application/json")
				.header(administrativeCsrf.headerName(), administrativeCsrf.token())
				.PUT(HttpRequest.BodyPublishers.ofString("{\"estado\":\"RESUELTO\"}"))
				.build();
		assertEquals(200, administrativeClient.send(administrativeRead, HttpResponse.BodyHandlers.ofString()).statusCode());
		assertEquals(404, administrativeClient.send(administrativeUpdate, HttpResponse.BodyHandlers.ofString()).statusCode());
	}

	@Test
	void allowsPublicNewsReadsAndRestrictsNewsManagementToAdministrators() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		var publicRead = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/noticias"))
				.GET()
				.build();
		assertEquals(200, client.send(publicRead, HttpResponse.BodyHandlers.ofString()).statusCode());

		String newsBody = """
				{"category":"Gestión","title":"Noticia de prueba","summary":"Resumen de prueba",
				"content":"Contenido de prueba","image":"","imageAlt":"","featured":false}
				""";
		var createNews = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/noticias"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(newsBody))
				.build();
		assertEquals(403, client.send(createNews, HttpResponse.BodyHandlers.ofString()).statusCode());

		HttpClient citizenClient = authenticatedClient("ciudadano");
		CsrfInfo citizenCsrf = fetchCsrf(citizenClient);
		assertEquals(403, citizenClient.send(
				withCsrf(createNews, citizenCsrf.headerName(), citizenCsrf.token()),
				HttpResponse.BodyHandlers.ofString()).statusCode());

		HttpClient administrativeClient = authenticatedClient("administrativo");
		CsrfInfo administrativeCsrf = fetchCsrf(administrativeClient);
		var csrfHeader = administrativeCsrf.headerName();
		var csrfToken = administrativeCsrf.token();
		assertEquals(201,
				administrativeClient.send(withCsrf(createNews, csrfHeader, csrfToken),
						HttpResponse.BodyHandlers.ofString()).statusCode());

		String boundary = "MunicipioBoundary" + UUID.randomUUID();
		String multipartNews = "{\"category\":\"Actividades\",\"title\":\"Noticia multipart\","
				+ "\"summary\":\"Resumen de prueba\",\"content\":\"Contenido de prueba\","
				+ "\"image\":\"assets/logo/Logo.jpeg\",\"imageAlt\":\"\",\"featured\":false}";
		String multipartBody = "--" + boundary + "\r\n"
				+ "Content-Disposition: form-data; name=\"noticia\"; filename=\"noticia.json\"\r\n"
				+ "Content-Type: application/json\r\n\r\n"
				+ multipartNews + "\r\n--" + boundary + "--\r\n";
		var multipartCreateNews = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/noticias"))
				.header("Content-Type", "multipart/form-data; boundary=" + boundary)
				.header(csrfHeader, csrfToken)
				.POST(HttpRequest.BodyPublishers.ofString(multipartBody))
				.build();
		assertEquals(201,
				administrativeClient.send(multipartCreateNews, HttpResponse.BodyHandlers.ofString()).statusCode());
	}

	@Test
	void allowsClaimTrackingByCodeWithoutExposingPrivateClaimDetails() throws Exception {
		Reclamo reclamo = new Reclamo(
				"Bacheo", "Calle confidencial 123", "", "Descripción privada del reclamo.", "");
		reclamo.setCodigoSeguimiento("ABC123DEF456");
		reclamoRepository.save(reclamo);

		var request = HttpRequest.newBuilder(URI.create(
				"http://localhost:" + port + "/api/reclamos/seguimiento/abc123def456"))
				.GET()
				.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"codigoSeguimiento\":\"ABC123DEF456\""));
		assertTrue(response.body().contains("\"categoria\":\"Bacheo\""));
		assertTrue(response.body().contains("\"estado\":\"PENDIENTE\""));
		assertTrue(!response.body().contains("Calle confidencial"));
		assertTrue(!response.body().contains("Descripción privada"));
	}

	@Test
	void publicNewsImagesAreReadableButUnpublishedClaimImagesRequireAdminRole() throws Exception {
		String publicFileName = UUID.randomUUID() + ".jpg";
		String privateFileName = UUID.randomUUID() + ".jpg";
		Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
		Path publicFile = uploadRoot.resolve(publicFileName);
		Path privateFile = uploadRoot.resolve(privateFileName);
		Files.createDirectories(uploadRoot);
		byte[] jpegHeader = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00};
		Files.write(publicFile, jpegHeader);
		Files.write(privateFile, jpegHeader);

		Noticia noticia = new Noticia();
		noticia.setSlug("imagen-publica-" + UUID.randomUUID());
		noticia.setCategory("Actualidad");
		noticia.setTitle("Imagen pública");
		noticia.setSummary("Resumen");
		noticia.setContent("Contenido");
		noticia.setImage("/uploads/" + publicFileName);
		noticia.setImageAlt("Foto de noticia");
		noticiaRepository.save(noticia);

		try {
			HttpClient anonymousClient = HttpClient.newHttpClient();
			var publicImageRequest = HttpRequest.newBuilder(URI.create(
					"http://localhost:" + port + "/uploads/" + publicFileName)).GET().build();
			var privateImageRequest = HttpRequest.newBuilder(URI.create(
					"http://localhost:" + port + "/uploads/" + privateFileName)).GET().build();

			assertEquals(200, anonymousClient.send(publicImageRequest, HttpResponse.BodyHandlers.ofByteArray()).statusCode());
			assertEquals(404, anonymousClient.send(privateImageRequest, HttpResponse.BodyHandlers.ofByteArray()).statusCode());

			String administrativeEmail = "usuario-admin-imagen-" + UUID.randomUUID() + "@example.com";
			HttpClient administrativeClient = authenticatedClient("administrativo", administrativeEmail);
			assertEquals(200, administrativeClient.send(privateImageRequest, HttpResponse.BodyHandlers.ofByteArray()).statusCode());

			Usuario administrator = usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(administrativeEmail).orElseThrow();
			administrator.setActivo(false);
			usuarioRepository.save(administrator);
			assertEquals(404,
					administrativeClient.send(privateImageRequest, HttpResponse.BodyHandlers.ofByteArray()).statusCode());
		} finally {
			noticiaRepository.delete(noticia);
			deleteIfPresent(publicFile);
			deleteIfPresent(privateFile);
		}
	}

	private HttpClient authenticatedClient(String role) throws Exception {
		String email = "usuario-" + role + "-" + UUID.randomUUID() + "@example.com";
		return authenticatedClient(role, email);
	}

	private HttpClient authenticatedClient(String role, String email) throws Exception {
		usuarioRepository.save(new Usuario(
				email,
				passwordEncoder.encode("Clave26"),
				"Usuario de prueba",
				role,
				""));
		CookieManager cookieManager = new CookieManager();
		HttpClient client = HttpClient.newBuilder().cookieHandler(cookieManager).build();
		CsrfInfo csrf = fetchCsrf(client);
		String previousSessionId = sessionId(cookieManager);
		var loginRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
				.header("Content-Type", "application/json")
				.header(csrf.headerName(), csrf.token())
				.POST(HttpRequest.BodyPublishers.ofString(
						"{\"email\":\"" + email + "\",\"password\":\"Clave26\"}"))
				.build();
		assertEquals(200, client.send(loginRequest, HttpResponse.BodyHandlers.ofString()).statusCode());
		assertNotEquals(previousSessionId, sessionId(cookieManager), "Login must rotate the session identifier");
		return client;
	}

	private String sessionId(CookieManager cookieManager) {
		return cookieManager.getCookieStore().getCookies().stream()
				.filter(cookie -> "JSESSIONID".equalsIgnoreCase(cookie.getName()))
				.map(java.net.HttpCookie::getValue)
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected a JSESSIONID cookie"));
	}

	private CsrfInfo fetchCsrf(HttpClient client) throws Exception {
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/csrf")).GET().build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
		return new CsrfInfo(jsonValue(response.body(), "headerName"), jsonValue(response.body(), "token"));
	}

	private String jsonValue(String json, String key) {
		Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"")
				.matcher(json);
		if (!matcher.find()) throw new AssertionError("Missing JSON field: " + key);
		return matcher.group(1);
	}

	private record CsrfInfo(String headerName, String token) {}

    private void deleteIfPresent(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

	private HttpRequest withCsrf(HttpRequest original, String headerName, String token) {
		HttpRequest.Builder request = HttpRequest.newBuilder(original.uri());
		original.headers().map().forEach((name, values) -> values.forEach(value -> request.header(name, value)));
		request.header(headerName, token);
		request.method(original.method(), original.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody()));
		return request.build();
	}

}
