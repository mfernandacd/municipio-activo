package com.municipioactivo.backend;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReclamoServiceImpl implements ReclamoService {

    private final ReclamoRepository reclamoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReclamoServiceImpl(ReclamoRepository reclamoRepository, UsuarioRepository usuarioRepository) {
        this.reclamoRepository = reclamoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Reclamo guardarReclamo(Reclamo reclamo) {
        return reclamoRepository.save(reclamo);
    }

    @Override
    @Transactional
    public Reclamo registrarReclamo(Reclamo reclamo, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "La cuenta no está activa. Iniciá sesión nuevamente."));
        reclamo.setUsuario(usuario);
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
    public List<Reclamo> obtenerPorBarrioYZona(String barrio, String zona) {
        return reclamoRepository.findByBarrioIgnoreCaseAndZonaIgnoreCaseOrderByFechaCreacionDesc(
                barrio.trim(), zona.trim());
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
