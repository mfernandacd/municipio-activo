package com.municipioactivo.controller;

import com.municipioactivo.dto.ReclamoResponseDTO;
import com.municipioactivo.service.ReclamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoController {

    @Autowired
    private ReclamoService reclamoService;

    /**
     * Endpoint para consultar el estado de un reclamo por su código de seguimiento.
     * Ruta: GET /api/reclamos/{codigo}
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<ReclamoResponseDTO> obtenerReclamoPorCodigo(@PathVariable String codigo) {
        
        // 1. Validación de parámetro vacío o nulo (HTTP 400 Bad Request)
        if (codigo == null || codigo.trim().isEmpty() || codigo.equalsIgnoreCase("null")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            // 2. Consulta en la base de datos a través del servicio
            Optional<ReclamoResponseDTO> reclamoOpt = reclamoService.buscarPorCodigo(codigo.trim());

            if (reclamoOpt.isPresent()) {
                // 3. Reclamo encontrado: Retorna JSON y HTTP 200 OK
                return ResponseEntity.ok(reclamoOpt.get());
            } else {
                // 4. No existe en MySQL: Retorna HTTP 404 Not Found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

        } catch (Exception e) {
            // Manejo general de errores de servidor (HTTP 500 Interno si ocurre una excepción inesperada)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}