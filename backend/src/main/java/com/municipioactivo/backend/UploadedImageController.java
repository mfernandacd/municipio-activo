package com.municipioactivo.backend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UploadedImageController {

    private final NoticiaRepository noticiaRepository;
    private final UsuarioRepository usuarioRepository;

    public UploadedImageController(NoticiaRepository noticiaRepository, UsuarioRepository usuarioRepository) {
        this.noticiaRepository = noticiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String fileName,
            Authentication authentication) {
        String extension = extension(fileName);
        Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
        Path imagePath = uploadRoot.resolve(fileName).normalize();
        if (!imagePath.startsWith(uploadRoot) || !Files.isRegularFile(imagePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String publicPath = "/uploads/" + fileName;
        boolean usedByPublicNews = noticiaRepository.existsByImageIn(
                List.of(publicPath, publicPath.substring(1)));
        boolean isAdministrator = authentication != null && authentication.isAuthenticated()
                && usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(authentication.getName())
                        .map(usuario -> "administrativo".equalsIgnoreCase(usuario.getRol()))
                        .orElse(false);
        if (!usedByPublicNews && !isAdministrator) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok()
                .contentType(mediaType(extension))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(usedByPublicNews ? CacheControl.maxAge(1, java.util.concurrent.TimeUnit.HOURS).cachePublic()
                        : CacheControl.noCache().cachePrivate())
                .body(new FileSystemResource(imagePath));
    }

    private String extension(String fileName) {
        int separator = fileName.lastIndexOf('.');
        if (separator < 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        String extension = fileName.substring(separator).toLowerCase(Locale.ROOT);
        String id = fileName.substring(0, separator);
        try {
            if (!UUID.fromString(id).toString().equalsIgnoreCase(id)) {
                throw new IllegalArgumentException("Invalid UUID form");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!List.of(".jpg", ".png", ".gif", ".webp").contains(extension)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return extension;
    }

    private MediaType mediaType(String extension) {
        return switch (extension) {
            case ".jpg" -> MediaType.IMAGE_JPEG;
            case ".png" -> MediaType.IMAGE_PNG;
            case ".gif" -> MediaType.IMAGE_GIF;
            case ".webp" -> MediaType.parseMediaType("image/webp");
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
    }
}
