package com.municipioactivo.backend;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticiaRepository extends JpaRepository<Noticia, Long> {
    Optional<Noticia> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Noticia> findAllByOrderByCreatedAtDesc();

    List<Noticia> findAllByFeaturedTrue();

    boolean existsByImageIn(Collection<String> images);
}
