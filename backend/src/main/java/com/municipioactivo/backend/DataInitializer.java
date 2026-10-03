package com.municipioactivo.backend;

import com.municipioactivo.backend.model.Reclamo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ReclamoRepository reclamoRepository;

    public DataInitializer(ReclamoRepository reclamoRepository) {
        this.reclamoRepository = reclamoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (reclamoRepository.count() == 0) {
            Reclamo reclamo1 = new Reclamo("Alumbrado roto", "Falta luz en la esquina", "vecino@mail.com", "REC-001");
            Reclamo reclamo2 = new Reclamo("Bache en la calle", "Bache profundo frente a la plaza", "vecino2@mail.com", "REC-002");

            reclamoRepository.save(reclamo1);
            reclamoRepository.save(reclamo2);
        }
    }
}