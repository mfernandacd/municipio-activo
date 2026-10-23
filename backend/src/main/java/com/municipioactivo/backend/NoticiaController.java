package com.municipioactivo.backend;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    private static final String DEFAULT_IMAGE_PATH = "assets/logo/Logo.jpeg";

    private final NoticiaRepository repository;

    public NoticiaController(NoticiaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NoticiaResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(NoticiaResponse::from).toList();
    }

    @GetMapping("/{slug}")
    public NoticiaResponse find(@PathVariable String slug) {
        return NoticiaResponse.from(findEntity(slug));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public NoticiaResponse create(@Valid @RequestBody NoticiaRequest request) {
        Noticia noticia = new Noticia();
        noticia.setSlug(createSlug(request.title()));
        apply(noticia, request);
        if (request.featured()) {
            clearFeatured();
        } else if (repository.count() == 0) {
            noticia.setFeatured(true);
        }
        return NoticiaResponse.from(repository.save(noticia));
    }

    @PutMapping("/{slug}")
    @Transactional
    public NoticiaResponse update(@PathVariable String slug, @Valid @RequestBody NoticiaRequest request) {
        Noticia noticia = findEntity(slug);
        boolean wasFeatured = noticia.isFeatured();
        apply(noticia, request);
        if (request.featured()) {
            clearFeatured();
            noticia.setFeatured(true);
        } else if (wasFeatured) {
            noticia.setFeatured(false);
            if (repository.findAllByFeaturedTrue().isEmpty()) {
                noticia.setFeatured(true);
            }
        }
        return NoticiaResponse.from(repository.save(noticia));
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable String slug) {
        Noticia deleted = findEntity(slug);
        repository.delete(deleted);
        if (deleted.isFeatured()) {
            repository.findAllByOrderByCreatedAtDesc().stream().findFirst().ifPresent(item -> {
                item.setFeatured(true);
                repository.save(item);
            });
        }
    }

    private Noticia findEntity(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la noticia"));
    }

    private void clearFeatured() {
        repository.findAllByFeaturedTrue().forEach(item -> item.setFeatured(false));
    }

    private void apply(Noticia noticia, NoticiaRequest request) {
        noticia.setCategory(request.category().trim());
        noticia.setTitle(request.title().trim());
        noticia.setSummary(request.summary().trim());
        noticia.setContent(request.content().trim());
        noticia.setImage(normalizeImagePath(request.image()));
        noticia.setImageAlt(request.imageAlt() == null || request.imageAlt().isBlank()
                ? request.title().trim() : request.imageAlt().trim());
        noticia.setFeatured(request.featured());
    }

    private static String normalizeImagePath(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return DEFAULT_IMAGE_PATH;
        }

        String trimmedPath = imagePath.trim();
        String comparablePath = trimmedPath.replace('\\', '/');
        if ("assets/imagenes/Logo.jpeg".equalsIgnoreCase(comparablePath)
                || "assets/Logo/Logo.jpeg".equalsIgnoreCase(comparablePath)) {
            return DEFAULT_IMAGE_PATH;
        }
        return trimmedPath;
    }

    private String createSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        String base = normalized.isBlank() ? "noticia" : normalized;
        String slug;
        do {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        } while (repository.existsBySlug(slug));
        return slug;
    }

    public record NoticiaRequest(
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 300) String summary,
            @NotBlank @Size(max = 15000) String content,
            @Size(max = 1000) String image,
            @Size(max = 180) String imageAlt,
            boolean featured) {
    }

    public record NoticiaResponse(
            String id,
            String category,
            String title,
            String summary,
            String content,
            String image,
            String imageAlt,
            boolean featured) {
        static NoticiaResponse from(Noticia noticia) {
            return new NoticiaResponse(noticia.getSlug(), noticia.getCategory(), noticia.getTitle(),
                    noticia.getSummary(), noticia.getContent(), normalizeImagePath(noticia.getImage()),
                    noticia.getImageAlt(),
                    noticia.isFeatured());
        }
    }
}
