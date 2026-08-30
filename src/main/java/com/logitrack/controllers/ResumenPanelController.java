package com.logitrack.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.logitrack.dto.ResumenPanelRequest;
import com.logitrack.dto.ResumenPanelResponse;
import com.logitrack.services.ResumenPanelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/panel/resumen")
@Tag(name = "Panel IQ")
public class ResumenPanelController {

    private final ResumenPanelService resumenPanelService;

    public ResumenPanelController(ResumenPanelService resumenPanelService) {
        this.resumenPanelService = resumenPanelService;
    }

    @PostMapping
    @Operation(summary = "Publicar resumen", description = "ADMIN o AGENTE. 400 para contrato inválido; 401/403 si no está autorizado.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<ResumenPanelResponse> publicar(
                    @Valid @RequestBody ResumenPanelRequest request) {
        return ResponseEntity.ok(resumenPanelService.publicar(request));
    }

    @GetMapping
    @Operation(summary = "Consultar último resumen", description = "ADMIN o AGENTE. 404 si no existe un resumen válido.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<ResumenPanelResponse> obtenerUltimo() {
        return ResponseEntity.ok(resumenPanelService.obtenerUltimo());
    }
}
