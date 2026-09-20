package com.municipioactivo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
// Punto de entrada que inicia la aplicación Spring Boot del backend.
public class BackendApplication {

	// Inicia el contexto de Spring y pone en marcha la API REST.
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
