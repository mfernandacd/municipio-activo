package com.municipioactivo.backend;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import jakarta.validation.Valid;
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
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Reclamo> crearReclamo(
            @Valid @RequestPart("reclamo") Reclamo reclamo,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws IOException {
        if (imagen != null && !imagen.isEmpty()) {
            if (imagen.getContentType() == null || !imagen.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            Path uploadDirectory = Paths.get("uploads");
            Files.createDirectories(uploadDirectory);
            String extension = obtenerExtension(imagen.getOriginalFilename());
            String fileName = UUID.randomUUID() + extension;
            Files.copy(imagen.getInputStream(), uploadDirectory.resolve(fileName));
            reclamo.setImagenPath(uploadDirectory.resolve(fileName).toString());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(reclamoService.guardarReclamo(reclamo));
    }

    @GetMapping
    public List<Reclamo> obtenerReclamos() {
        return reclamoService.obtenerTodosLosReclamos();
    }

    @GetMapping("/estado/{estado}")
    public List<Reclamo> obtenerReclamosPorEstado(@PathVariable String estado) {
        return reclamoService.obtenerReclamosPorEstado(estado);
    }

    private String obtenerExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int extensionIndex = originalFilename.lastIndexOf('.');
        return extensionIndex >= 0 ? originalFilename.substring(extensionIndex).toLowerCase() : "";
    }
}