package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
	void servesAllPagesLinkedFromTheServicesSection() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		for (String page : new String[] {
				"/reclamos-municipal.html", "/seguimiento.html", "/servicios.html"
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
		String body = "{\"nombre\":\"Usuario de prueba\",\"email\":\"" + email + "\",\"password\":\"Clave26\"}";
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/registro"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(201, response.statusCode());
		assertTrue(response.body().contains(email));
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
		var citizenRead = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos")).GET().build();
		var citizenUpdate = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos/1/estado"))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString("{\"estado\":\"RESUELTO\"}"))
				.build();
		assertEquals(403, citizenClient.send(citizenRead, HttpResponse.BodyHandlers.ofString()).statusCode());
		assertEquals(403, citizenClient.send(citizenUpdate, HttpResponse.BodyHandlers.ofString()).statusCode());

		HttpClient administrativeClient = authenticatedClient("administrativo");
		var administrativeRead = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/reclamos"))
				.GET()
				.build();
		var administrativeUpdate = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/reclamos/999999/estado"))
				.header("Content-Type", "application/json")
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
		assertEquals(403, citizenClient.send(createNews, HttpResponse.BodyHandlers.ofString()).statusCode());

		HttpClient administrativeClient = authenticatedClient("administrativo");
		assertEquals(201,
				administrativeClient.send(createNews, HttpResponse.BodyHandlers.ofString()).statusCode());
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

	private HttpClient authenticatedClient(String role) throws Exception {
		String email = "usuario-" + role + "-" + UUID.randomUUID() + "@example.com";
		usuarioRepository.save(new Usuario(
				email,
				passwordEncoder.encode("Clave26"),
				"Usuario de prueba",
				role,
				""));
		HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
		var loginRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(
						"{\"email\":\"" + email + "\",\"password\":\"Clave26\"}"))
				.build();
		assertEquals(200, client.send(loginRequest, HttpResponse.BodyHandlers.ofString()).statusCode());
		return client;
	}

}
