package com.municipioactivo.backend;

import java.util.List;
import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reclamos")
// Expone los endpoints REST para crear y consultar reclamos municipales.
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    // Guarda un reclamo enviado como JSON directo desde el frontend web.
    public ResponseEntity<Reclamo> crearReclamoJson(
            @Valid @RequestBody ReclamoJsonRequest request) {
        String direccion = request.direccion();
        if (direccion == null || direccion.isBlank()) {
            direccion = "Ubicación informada en descripción";
        }
        String googleMaps = request.googleMapsUrl() != null ? request.googleMapsUrl() : "";
        String imagenPath = request.imagenPath() != null ? request.imagenPath() : "";

        Reclamo reclamo = new Reclamo(
                request.categoria(),
                direccion,
                googleMaps,
                request.descripcion(),
                imagenPath);

        return ResponseEntity.status(HttpStatus.CREATED).body(reclamoService.guardarReclamo(reclamo));
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

    @GetMapping("/{codigoSeguimiento}")
    public ResponseEntity<Reclamo> consultarPorCodigo(@PathVariable String codigoSeguimiento) {
        if (codigoSeguimiento == null || codigoSeguimiento.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return reclamoService.buscarPorCodigoSeguimiento(codigoSeguimiento.trim())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    // Devuelve los reclamos cuyo estado coincide con el parámetro recibido.
    public List<Reclamo> obtenerReclamosPorEstado(@PathVariable String estado) {
        return reclamoService.obtenerReclamosPorEstado(estado);
    }

    @GetMapping("/seguimiento/{codigoSeguimiento}")
    public SeguimientoResponse consultarSeguimiento(@PathVariable String codigoSeguimiento) {
        Reclamo reclamo = reclamoService.buscarPorCodigoSeguimiento(codigoSeguimiento.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró un reclamo con ese código de seguimiento"));
        return new SeguimientoResponse(
                reclamo.getCodigoSeguimiento(),
                reclamo.getCategoria(),
                reclamo.getEstado(),
                reclamo.getFechaCreacion());
    }

    // Actualiza el estado de un reclamo existente por su ID
    @PutMapping("/{id}/estado")
    public ResponseEntity<Reclamo> actualizarEstadoReclamo(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request) {
        String nuevoEstado = request.estado().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("PENDIENTE", "EN_PROCESO", "RESUELTO").contains(nuevoEstado)) {
            return ResponseEntity.badRequest().build();
        }

        // Buscamos el reclamo usando el servicio
        List<Reclamo> todos = reclamoService.obtenerTodosLosReclamos();
        Reclamo reclamoEncontrado = todos.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (reclamoEncontrado == null) {
            return ResponseEntity.notFound().build();
        }

        reclamoEncontrado.setEstado(nuevoEstado);
        Reclamo actualizado = reclamoService.guardarReclamo(reclamoEncontrado);
        return ResponseEntity.ok(actualizado);
    }

    public record EstadoRequest(@NotBlank String estado) {
    }

    public record SeguimientoResponse(
            String codigoSeguimiento,
            String categoria,
            String estado,
            LocalDateTime fechaCreacion) {
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

    public record ReclamoJsonRequest(
            @NotBlank(message = "La categoría es obligatoria")
            @Size(max = 50, message = "La categoría no debe superar los 50 caracteres")
            String categoria,

            @Size(max = 255, message = "La dirección no debe superar los 255 caracteres")
            String direccion,

            @Size(max = 500, message = "El enlace de Google Maps no debe superar los 500 caracteres")
            String googleMapsUrl,

            @NotBlank(message = "La descripción es obligatoria")
            @Size(min = 15, message = "La descripción debe tener al menos 15 caracteres")
            String descripcion,

            @Size(max = 255, message = "La ruta de imagen no debe superar los 255 caracteres")
            String imagenPath,

            String userName,
            String userEmail) {
    }
}
