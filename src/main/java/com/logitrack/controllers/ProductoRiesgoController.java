package com.logitrack.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.logitrack.dto.ProductoRiesgoResponse;
import com.logitrack.services.ProductoRiesgoService;

@RestController
@RequestMapping("/productos")
@Tag(name = "Consulta IQ de productos")
public class ProductoRiesgoController {

    private final ProductoRiesgoService productoRiesgoService;

    public ProductoRiesgoController(ProductoRiesgoService productoRiesgoService) {
        this.productoRiesgoService = productoRiesgoService;
    }

    @GetMapping("/riesgo")
    @Operation(summary = "Consultar productos en riesgo", description = "ADMIN o AGENTE. 401 sin token; 403 sin rol.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<List<ProductoRiesgoResponse>> listarRiesgos() {
        return ResponseEntity.ok(
                productoRiesgoService.listarProductosEnRiesgo());
    }
}
