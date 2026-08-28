package com.logitrack.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logitrack.dto.BodegaDto;
import com.logitrack.services.BodegaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bodegas")
public class BodegaController {

    private final BodegaService bodegaService;

    public BodegaController(BodegaService bodegaService) {
        this.bodegaService = bodegaService;
    }

    @GetMapping
    public ResponseEntity<List<BodegaDto>> listarTodas() {
        List<BodegaDto> bodegas = bodegaService.listarTodas();
        return ResponseEntity.ok(bodegas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BodegaDto> obtenerPorId(@PathVariable Long id) {
        BodegaDto bodega = bodegaService.obtenerPorId(id);
        return ResponseEntity.ok(bodega);
    }

    @PostMapping
    public ResponseEntity<BodegaDto> crear(
            @Valid @RequestBody BodegaDto bodegaDto) {

        BodegaDto bodegaCreada = bodegaService.crear(bodegaDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bodegaCreada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BodegaDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody BodegaDto bodegaDto) {

        BodegaDto bodegaActualizada =
                bodegaService.actualizar(id, bodegaDto);

        return ResponseEntity.ok(bodegaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        bodegaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}