package com.municipio.serranoble.controller;

import com.municipio.serranoble.entity.Reclamo;
import com.municipio.serranoble.service.ReclamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
@CrossOrigin(origins = "*") // Permite peticiones desde el frontend HTML
public class ReclamoController {

    private final ReclamoService reclamoService;

    @Autowired
    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping
    public ResponseEntity<Reclamo> crearReclamo(@RequestBody Reclamo reclamo) {
        Reclamo nuevoReclamo = reclamoService.guardarReclamo(reclamo);
        return new ResponseEntity<>(nuevoReclamo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Reclamo>> listarReclamos() {
        return ResponseEntity.ok(reclamoService.obtenerTodosLosReclamos());
    }
}