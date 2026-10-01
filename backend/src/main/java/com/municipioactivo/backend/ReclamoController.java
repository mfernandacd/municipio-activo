package com.municipioactivo.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reclamos")
@CrossOrigin(origins = "*")
public class reclamoController {

    private final ReclamoService reclamoService;

    public reclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping
    public ResponseEntity<Reclamo> crearReclamo(@RequestBody Reclamo reclamo) {
        Reclamo nuevoReclamo = reclamoService.registrarReclamo(reclamo);
        return new ResponseEntity<>(nuevoReclamo, HttpStatus.CREATED);
    }
}