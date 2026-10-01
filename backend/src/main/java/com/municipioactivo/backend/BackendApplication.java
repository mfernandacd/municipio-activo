package com.municipioactivo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // <-- Importación necesaria

@SpringBootApplication
@EnableAsync // <-- Habilita el envío de mails en segundo plano
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}