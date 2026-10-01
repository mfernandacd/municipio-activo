package com.municipioactivo.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReclamoService {

    private final ReclamoRepository reclamoRepository;
    private final GeneradorCodigoService generadorCodigoService;
    private final EmailService emailService;

    public ReclamoService(ReclamoRepository reclamoRepository,
                          GeneradorCodigoService generadorCodigoService,
                          EmailService emailService) {
        this.reclamoRepository = reclamoRepository;
        this.generadorCodigoService = generadorCodigoService;
        this.emailService = emailService;
    }

    @Transactional
    public Reclamo registrarReclamo(Reclamo reclamo) {
        // 1. Generar e ingresar el código único de seguimiento
        String codigoUnico = generadorCodigoService.generarCodigoUnico();
        reclamo.setCodigoSeguimiento(codigoUnico);

        // 2. Guardar en MySQL
        Reclamo reclamoGuardado = reclamoRepository.save(reclamo);

        // 3. Enviar correo de confirmación de forma asíncrona
        emailService.enviarMailConfirmacion(reclamoGuardado);

        return reclamoGuardado;
    }
}