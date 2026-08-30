package com.logitrack.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.logitrack.dto.KpiResponse;
import com.logitrack.services.KpiService;

@RestController
@RequestMapping("/kpis")
@Tag(name = "Indicadores IQ")
public class KpiController {

    private final KpiService kpiService;

    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @GetMapping
    @Operation(summary = "Consultar KPIs", description = "ADMIN o AGENTE. 401 sin token; 403 sin rol.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<KpiResponse> obtenerKpis() {
        return ResponseEntity.ok(kpiService.obtenerKpis());
    }
}
