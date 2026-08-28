package com.logitrack.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logitrack.dto.InventarioDto;
import com.logitrack.services.InventarioService;

@RestController
@RequestMapping("/api/inventarios")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<List<InventarioDto>> listarTodos() {
        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    @GetMapping("/bodega/{bodegaId}")
    public ResponseEntity<List<InventarioDto>> buscarPorBodega(@PathVariable Long bodegaId) {
        return ResponseEntity.ok(inventarioService.buscarPorBodega(bodegaId));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<InventarioDto>> buscarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.buscarPorProducto(productoId));
    }
}
