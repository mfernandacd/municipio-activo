package com.municipioactivo.backend;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReclamoServiceImpl implements ReclamoService {

    private final ReclamoRepository reclamoRepository;

    public ReclamoServiceImpl(ReclamoRepository reclamoRepository) {
        this.reclamoRepository = reclamoRepository;
    }

    @Override
    @Transactional
    public Reclamo guardarReclamo(Reclamo reclamo) {
        return reclamoRepository.save(reclamo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerTodosLosReclamos() {
        return reclamoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerReclamosPendientes() {
        return reclamoRepository.findByEstado("PENDIENTE");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerReclamosPorEstado(String estado) {
        return reclamoRepository.findByEstado(estado.toUpperCase(Locale.ROOT));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reclamo> buscarPorId(Long id) {
        return reclamoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reclamo> buscarPorCodigoSeguimiento(String codigoSeguimiento) {
        return reclamoRepository.findByCodigoSeguimientoIgnoreCase(codigoSeguimiento);
    }
}
