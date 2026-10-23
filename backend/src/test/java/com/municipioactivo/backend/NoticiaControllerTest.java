package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoticiaControllerTest {

    @Mock
    private NoticiaRepository repository;

    @InjectMocks
    private NoticiaController controller;

    @Test
    void createsFirstNewsAsFeaturedAndGeneratesSlug() {
        when(repository.count()).thenReturn(0L);
        when(repository.save(any(Noticia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoticiaController.NoticiaResponse result = controller.create(request(false));

        assertTrue(result.featured());
        assertTrue(result.id().startsWith("agenda-municipal-"));
        assertEquals("Agenda municipal", result.title());
        assertEquals("assets/logo/Logo.jpeg", result.image());
    }

    @Test
    void makesOnlyTheSelectedNewsFeatured() {
        Noticia existing = news("agenda-vieja", true);
        when(repository.findBySlug("agenda-vieja")).thenReturn(Optional.of(existing));
        when(repository.findAllByFeaturedTrue()).thenReturn(List.of(existing));
        when(repository.save(any(Noticia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoticiaController.NoticiaResponse result = controller.update("agenda-vieja", request(true));

        assertTrue(result.featured());
        verify(repository).findAllByFeaturedTrue();
    }

    @Test
    void deletingFeaturedNewsPromotesTheNewestRemainingNews() {
        Noticia featured = news("destacada", true);
        Noticia next = news("siguiente", false);
        when(repository.findBySlug("destacada")).thenReturn(Optional.of(featured));
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(next));

        controller.delete("destacada");

        assertTrue(next.isFeatured());
        verify(repository).delete(featured);
        verify(repository).save(next);
    }

    private static NoticiaController.NoticiaRequest request(boolean featured) {
        return new NoticiaController.NoticiaRequest(
                "Gestión",
                "Agenda municipal",
                "Resumen de la agenda.",
                "Contenido de prueba.",
                "",
                "",
                featured);
    }

    private static Noticia news(String slug, boolean featured) {
        Noticia noticia = new Noticia();
        noticia.setSlug(slug);
        noticia.setCategory("Gestión");
        noticia.setTitle("Agenda municipal");
        noticia.setSummary("Resumen");
        noticia.setContent("Contenido");
        noticia.setImage("assets/imagenes/Logo.jpeg");
        noticia.setImageAlt("Emblema");
        noticia.setFeatured(featured);
        return noticia;
    }
}
