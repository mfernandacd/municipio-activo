package com.municipioactivo.backend;

import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.Random;

@Service
public class GeneradorCodigoService {

    private final ReclamoRepository reclamoRepository;
    private final Random random = new Random();
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Inyección de dependencias por constructor
    public GeneradorCodigoService(ReclamoRepository reclamoRepository) {
        this.reclamoRepository = reclamoRepository;
    }

    /**
     * Genera un código único en formato SER-YYYY-XXXX (ej: SER-2026-A8K2).
     * Garantiza unicidad consultando al repositorio.
     */
    public String generarCodigoUnico() {
        String codigo;
        int anioActual = Year.now().getValue();

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(CARACTERES.charAt(random.nextInt(CARACTERES.length())));
            }
            codigo = String.format("SER-%d-%s", anioActual, sb.toString());
        } while (reclamoRepository.existsByCodigoSeguimiento(codigo));

        return codigo;
    }
}