package com.municipio.serranoble.service.impl;

import com.municipio.serranoble.entity.Reclamo;
import com.municipio.serranoble.repository.ReclamoRepository;
import com.municipio.serranoble.service.ReclamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReclamoServiceImpl implements ReclamoService {

    private final ReclamoRepository reclamoRepository;

    @Autowired
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
}