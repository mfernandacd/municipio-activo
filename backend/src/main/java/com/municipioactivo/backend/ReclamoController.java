package com.municipioactivo.backend;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reclamos")
// Expone los endpoints REST para crear y consultar reclamos municipales.
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Guarda un reclamo y, si se recibe, valida y almacena su imagen adjunta.
    public ResponseEntity<Reclamo> crearReclamo(
            @Valid @RequestPart("reclamo") ReclamoRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws IOException {
        Reclamo reclamo = new Reclamo(
                request.categoria(),
                request.direccion(),
                request.googleMapsUrl(),
                request.descripcion(),
                "");
        Path uploadedFile = null;

        if (imagen != null && !imagen.isEmpty()) {
            String extension = obtenerExtension(imagen.getContentType());
            if (extension == null) {
                return ResponseEntity.badRequest().build();
            }

            Path uploadDirectory = Paths.get("uploads");
            Files.createDirectories(uploadDirectory);
            String fileName = UUID.randomUUID() + extension;
            uploadedFile = uploadDirectory.resolve(fileName);
            try {
                Files.copy(imagen.getInputStream(), uploadedFile);
            } catch (IOException | RuntimeException exception) {
                eliminarArchivoSubido(uploadedFile, exception);
                throw exception;
            }
            reclamo.setImagenPath(uploadedFile.toString());
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reclamoService.guardarReclamo(reclamo));
        } catch (RuntimeException exception) {
            if (uploadedFile != null) {
                eliminarArchivoSubido(uploadedFile, exception);
            }
            throw exception;
        }
    }

    @GetMapping
    // Devuelve todos los reclamos registrados.
    public List<Reclamo> obtenerReclamos() {
        return reclamoService.obtenerTodosLosReclamos();
    }

    @GetMapping("/estado/{estado}")
    // Devuelve los reclamos cuyo estado coincide con el parámetro recibido.
    public List<Reclamo> obtenerReclamosPorEstado(@PathVariable String estado) {
        return reclamoService.obtenerReclamosPorEstado(estado);
    }

    private String obtenerExtension(String contentType) {
        if (contentType == null) {
            return null;
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> null;
        };
    }

    private void eliminarArchivoSubido(Path file, Exception exception) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException cleanupException) {
            exception.addSuppressed(cleanupException);
        }
    }

    public record ReclamoRequest(
            @NotBlank @Size(max = 50) String categoria,
            @NotBlank @Size(max = 255) String direccion,
            @Size(max = 500) String googleMapsUrl,
            @NotBlank @Size(min = 15) String descripcion) {
    }
}