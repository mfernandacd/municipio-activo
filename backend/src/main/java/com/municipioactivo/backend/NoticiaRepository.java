package com.municipioactivo.backend;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface NoticiaRepository extends JpaRepository<Noticia, Long> {
    Optional<Noticia> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Noticia> findAllByOrderByCreatedAtDesc();

    List<Noticia> findAllByFeaturedTrue();
}
