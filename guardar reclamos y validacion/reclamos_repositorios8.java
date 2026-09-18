package com.municipio.serranoble.repository;

import com.municipio.serranoble.entity.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    // Método para consultar reportes por estado (por ejemplo, PENDIENTE)
    List<Reclamo> findByEstado(String estado);
}