package com.municipioactivo.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadInitialData(
            UsuarioRepository usuarioRepository,
            ReclamoRepository reclamoRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
                Usuario administrador = usuarioRepository.findByEmailIgnoreCase("admin@municipio.gob.ar")
                        .orElseGet(() -> new Usuario(
                                "admin@municipio.gob.ar",
                                "",
                                "Administrador Municipal"));
                administrador.setPasswordHash(passwordEncoder.encode("Municipio123!"));
                administrador.setNombre("Administrador Municipal");
                administrador.setActivo(true);
                usuarioRepository.save(administrador);

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
