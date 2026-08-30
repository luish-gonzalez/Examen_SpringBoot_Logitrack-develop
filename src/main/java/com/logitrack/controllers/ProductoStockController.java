package com.logitrack.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.logitrack.dto.ProductoStockResponse;
import com.logitrack.services.StockDerivadoService;

@RestController
@RequestMapping("/productos")
@Tag(name = "Consulta IQ de productos")
public class ProductoStockController {

    private final StockDerivadoService stockDerivadoService;

    public ProductoStockController(StockDerivadoService stockDerivadoService) {
        this.stockDerivadoService = stockDerivadoService;
    }

    @GetMapping("/{id}/stock")
    @Operation(summary = "Consultar stock derivado", description = "ADMIN o AGENTE. 401/403; 404 si no existe.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<ProductoStockResponse> obtenerStock(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                stockDerivadoService.obtenerStockProducto(id));
    }
}
