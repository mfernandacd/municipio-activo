package com.municipioactivo.backend;

import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            @Valid @RequestBody ReclamoJsonRequest request,
            Authentication authentication) {
        if (request == null) return ResponseEntity.badRequest().build();

        String googleMaps = request.googleMapsUrl() != null ? request.googleMapsUrl() : "";
        Reclamo reclamo = new Reclamo(
                request.categoria(),
                request.direccion().trim(),
                request.barrio().trim(),
                request.zona().trim().toUpperCase(Locale.ROOT),
                googleMaps,
                request.descripcion(),
                null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reclamoService.registrarReclamo(reclamo, authentication.getName()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Guarda un reclamo y, si se recibe, valida y almacena su imagen adjunta.
    public ResponseEntity<Reclamo> crearReclamo(
            @Valid @RequestPart("reclamo") ReclamoRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication) throws IOException {
        if (request == null) return ResponseEntity.badRequest().build();

        Reclamo reclamo = new Reclamo(
                request.categoria(),
                request.direccion().trim(),
                request.barrio().trim(),
                request.zona().trim().toUpperCase(Locale.ROOT),
                request.googleMapsUrl(),
                request.descripcion(),
                "");
        Path uploadedFile = null;

        if (imagen != null && !imagen.isEmpty()) {
            String extension = ImageFileValidator.validateAndGetExtension(imagen);
            Path uploadDirectory = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDirectory);
            String fileName = UUID.randomUUID() + extension;
            uploadedFile = uploadDirectory.resolve(fileName).normalize();
            if (!uploadedFile.startsWith(uploadDirectory)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta de la imagen no es válida.");
            }
            try {
                Files.copy(imagen.getInputStream(), uploadedFile);
            } catch (IOException | RuntimeException exception) {
                eliminarArchivoSubido(uploadedFile, exception);
                throw exception;
            }
            reclamo.setImagenPath("/uploads/" + fileName);
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(reclamoService.registrarReclamo(reclamo, authentication.getName()));
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

    @GetMapping("/ubicacion")
    public List<Reclamo> obtenerReclamosPorUbicacion(
            @RequestParam String barrio,
            @RequestParam String zona) {
        if (barrio.isBlank() || zona.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El barrio y la zona son obligatorios.");
        }
        return reclamoService.obtenerPorBarrioYZona(barrio, zona);
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
        if (request == null || request.estado() == null) return ResponseEntity.badRequest().build();

        String nuevoEstado = request.estado().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("PENDIENTE", "EN_PROCESO", "RESUELTO").contains(nuevoEstado)) {
            return ResponseEntity.badRequest().build();
        }

        // Buscamos el reclamo usando el servicio
        return reclamoService.buscarPorId(id)
                .map(reclamo -> {
                    reclamo.setEstado(nuevoEstado);
                    return ResponseEntity.ok(reclamoService.guardarReclamo(reclamo));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record EstadoRequest(@NotBlank String estado) {
    }

    public record SeguimientoResponse(
            String codigoSeguimiento,
            String categoria,
            String estado,
            LocalDateTime fechaCreacion) {
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
            @NotBlank @Size(max = 100) String barrio,
            @NotBlank @Pattern(regexp = "(?i)^(NORTE|SUR|ESTE|OESTE|CENTRO|RURAL)$") String zona,
            @Size(max = 500) String googleMapsUrl,
            @NotBlank @Size(min = 15) String descripcion) {
    }

    public record ReclamoJsonRequest(
            @NotBlank(message = "La categoría es obligatoria")
            @Size(max = 50, message = "La categoría no debe superar los 50 caracteres")
            String categoria,

            @NotBlank(message = "La dirección es obligatoria")
            @Size(max = 255, message = "La dirección no debe superar los 255 caracteres")
            String direccion,

            @NotBlank(message = "El barrio es obligatorio")
            @Size(max = 100, message = "El barrio no debe superar los 100 caracteres")
            String barrio,

            @NotBlank(message = "La zona es obligatoria")
            @Pattern(regexp = "(?i)^(NORTE|SUR|ESTE|OESTE|CENTRO|RURAL)$", message = "La zona seleccionada no es válida")
            String zona,

            @Size(max = 500, message = "El enlace de Google Maps no debe superar los 500 caracteres")
            String googleMapsUrl,

            @NotBlank(message = "La descripción es obligatoria")
            @Size(min = 15, message = "La descripción debe tener al menos 15 caracteres")
            String descripcion) {
    }
}
