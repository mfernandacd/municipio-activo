package com.municipioactivo.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
// Carga datos de ejemplo cuando se inicia la aplicación en desarrollo local.
public class DataInitializer {

    // Crea reclamos de ejemplo en desarrollo, sin instalar cuentas con contraseñas conocidas.
    @Bean
    @Profile("local")
    CommandLineRunner loadInitialData(
            ReclamoRepository reclamoRepository) {
        return args -> {
            if (reclamoRepository.count() == 0) {
                reclamoRepository.save(new Reclamo(
                        "Luminarias",
                        "Av. San Martin 1234, Barrio Centro",
                        "https://maps.google.com/",
                        "La luminaria de la esquina permanece apagada durante la noche.",
                        ""));
                reclamoRepository.save(new Reclamo(
                        "Bacheo",
                        "Calle Belgrano 450, Barrio Norte",
                        "",
                        "Hay un bache grande que dificulta el paso de vehiculos y peatones.",
                        ""));
            }
        };
    }
}
