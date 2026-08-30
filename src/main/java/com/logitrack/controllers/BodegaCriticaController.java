package com.logitrack.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.logitrack.dto.OcupacionBodegaResponse;
import com.logitrack.services.KpiService;

@RestController
@RequestMapping("/bodegas")
@Tag(name = "Indicadores IQ")
public class BodegaCriticaController {

    private static final BigDecimal UMBRAL_CRITICO = BigDecimal.valueOf(90);

    private final KpiService kpiService;

    public BodegaCriticaController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @GetMapping("/criticas")
    @Operation(summary = "Consultar bodegas críticas", description = "ADMIN o AGENTE; ocupación >= 90 %. 401 sin token; 403 sin rol.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<List<OcupacionBodegaResponse>> listarCriticas() {
        List<OcupacionBodegaResponse> criticas = kpiService.obtenerOcupacionPorBodega()
                .stream()
                .filter(ocupacion -> ocupacion.porcentaje().compareTo(UMBRAL_CRITICO) >= 0)
                .toList();
        return ResponseEntity.ok(criticas);
    }
}
