package com.municipioactivo.backend;

import com.municipioactivo.backend.model.Reclamo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List; // <-- Agregar este import

@RestController
@RequestMapping("/api/reclamos")
@CrossOrigin(origins = "*")
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    // Endpoint GET habilitado para el navegador / frontend
    @GetMapping
    public ResponseEntity<List<Reclamo>> obtenerTodos() {
        List<Reclamo> reclamos = reclamoService.obtenerTodos();
        return ResponseEntity.ok(reclamos);
    }

    @PostMapping
    public ResponseEntity<Reclamo> crearReclamo(@RequestBody Reclamo reclamo) {
        Reclamo nuevoReclamo = reclamoService.registrarReclamo(reclamo);
        return new ResponseEntity<>(nuevoReclamo, HttpStatus.CREATED);
    }
}