package com.municipioactivo.backend;

import com.municipioactivo.backend.model.Reclamo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List; // <-- Agregar este import

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

    // Nuevo método para listar todos los reclamos registrados
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerTodos() {
        return reclamoRepository.findAll();
    }

    @Transactional
    public Reclamo registrarReclamo(Reclamo reclamo) {
        String codigoUnico = generadorCodigoService.generarCodigoUnico();
        reclamo.setCodigoSeguimiento(codigoUnico);

        Reclamo reclamoGuardado = reclamoRepository.save(reclamo);
        emailService.enviarMailConfirmacion(reclamoGuardado);

        return reclamoGuardado;
    }
}