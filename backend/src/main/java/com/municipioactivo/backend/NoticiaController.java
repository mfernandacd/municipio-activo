package com.municipioactivo.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public NoticiaResponse create(@Valid @RequestBody NoticiaRequest request) {
        return createNoticia(request, null);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public NoticiaResponse createWithImage(
            @Valid @RequestPart("noticia") NoticiaRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        return createNoticia(request, imageFile);
    }

    @PutMapping(value = "/{slug}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public NoticiaResponse update(@PathVariable String slug, @Valid @RequestBody NoticiaRequest request) {
        return updateNoticia(slug, request, null);
    }

    @PutMapping(value = "/{slug}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public NoticiaResponse updateWithImage(
            @PathVariable String slug,
            @Valid @RequestPart("noticia") NoticiaRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        return updateNoticia(slug, request, imageFile);
    }

    private NoticiaResponse updateNoticia(String slug, NoticiaRequest request, MultipartFile imageFile) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan los datos de la noticia.");
        }
        Noticia noticia = findEntity(slug);
        boolean wasFeatured = noticia.isFeatured();
        Path uploadedFile = null;

        try {
            apply(noticia, request);
            if (imageFile != null && !imageFile.isEmpty()) {
                uploadedFile = storeImage(imageFile);
                noticia.setImage(toPublicImagePath(uploadedFile));
            }
            updateFeaturedStatus(noticia, request.featured(), wasFeatured);
            return NoticiaResponse.from(repository.save(noticia));
        } catch (RuntimeException exception) {
            deleteUploadedFile(uploadedFile, exception);
            throw exception;
        }
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

    private NoticiaResponse createNoticia(NoticiaRequest request, MultipartFile imageFile) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan los datos de la noticia.");
        }
        Path uploadedFile = null;
        try {
            Noticia noticia = new Noticia();
            noticia.setSlug(createSlug(request.title()));
            apply(noticia, request);
            if (imageFile != null && !imageFile.isEmpty()) {
                uploadedFile = storeImage(imageFile);
                noticia.setImage(toPublicImagePath(uploadedFile));
            }
            if (request.featured()) {
                clearFeatured();
            } else if (repository.count() == 0) {
                noticia.setFeatured(true);
            }
            return NoticiaResponse.from(repository.save(noticia));
        } catch (RuntimeException exception) {
            deleteUploadedFile(uploadedFile, exception);
            throw exception;
        }
    }

    private Path storeImage(MultipartFile imageFile) {
        String extension = ImageFileValidator.validateAndGetExtension(imageFile);
        Path uploadDirectory = Paths.get("uploads").toAbsolutePath().normalize();
        Path destination = uploadDirectory.resolve(UUID.randomUUID() + extension).normalize();
        if (!destination.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta de la imagen no es válida.");
        }

        try {
            Files.createDirectories(uploadDirectory);
            try (InputStream input = imageFile.getInputStream()) {
                Files.copy(input, destination);
            }
            return destination;
        } catch (IOException exception) {
            deleteUploadedFile(destination, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la imagen.", exception);
        } catch (RuntimeException exception) {
            deleteUploadedFile(destination, exception);
            throw exception;
        }
    }

    private void updateFeaturedStatus(Noticia noticia, boolean featured, boolean wasFeatured) {
        if (featured) {
            clearFeatured();
            noticia.setFeatured(true);
        } else if (wasFeatured) {
            noticia.setFeatured(false);
            if (repository.findAllByFeaturedTrue().isEmpty()) {
                noticia.setFeatured(true);
            }
        }
    }

    private String toPublicImagePath(Path uploadedFile) {
        return "/uploads/" + uploadedFile.getFileName();
    }

    private void deleteUploadedFile(Path uploadedFile, Exception exception) {
        if (uploadedFile == null) return;
        try {
            Files.deleteIfExists(uploadedFile);
        } catch (IOException cleanupException) {
            exception.addSuppressed(cleanupException);
        }
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
                    noticia.getImageAlt() == null || noticia.getImageAlt().isBlank()
                            ? noticia.getTitle() : noticia.getImageAlt(),
                    noticia.isFeatured());
        }
    }
}
