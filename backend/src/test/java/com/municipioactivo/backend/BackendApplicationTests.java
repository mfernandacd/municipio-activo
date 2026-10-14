package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// Verifica que el contexto completo de Spring Boot pueda inicializarse correctamente.
class BackendApplicationTests {

	@LocalServerPort
	private int port;

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

}
