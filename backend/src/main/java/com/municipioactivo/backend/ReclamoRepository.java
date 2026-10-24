package com.municipioactivo.backend;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    List<Reclamo> findByEstado(String estado);

    Optional<Reclamo> findByCodigoSeguimientoIgnoreCase(String codigoSeguimiento);
}
