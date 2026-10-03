package com.municipioactivo.backend;

import com.municipioactivo.backend.model.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    // Método para verificar en la base de datos si el código generado ya existe
    boolean existsByCodigoSeguimiento(String codigoSeguimiento);
}