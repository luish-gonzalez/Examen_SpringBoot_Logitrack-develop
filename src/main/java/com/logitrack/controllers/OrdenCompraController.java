package com.logitrack.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.logitrack.dto.OrdenCompraEstadoRequest;
import com.logitrack.dto.OrdenCompraRequest;
import com.logitrack.dto.OrdenCompraResponse;
import com.logitrack.enums.EstadoOrdenCompra;
import com.logitrack.services.OrdenCompraService;
import com.logitrack.services.PdfOrdenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ordenes")
@Tag(name = "Órdenes de compra IQ")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;
    private final PdfOrdenService pdfOrdenService;

    public OrdenCompraController(
                    OrdenCompraService ordenCompraService,
                    PdfOrdenService pdfOrdenService) {
        this.ordenCompraService = ordenCompraService;
        this.pdfOrdenService = pdfOrdenService;
    }

    @PostMapping
    @Operation(summary = "Crear orden BORRADOR", description = "ADMIN o AGENTE. 400 para contrato inválido; 401/403 para autenticación/autorización.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<OrdenCompraResponse> crear(
                    @Valid @RequestBody OrdenCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCompraService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar órdenes", description = "ADMIN o AGENTE; filtro opcional por estado. 401/403 si no está autorizado.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<List<OrdenCompraResponse>> listar(
                    @RequestParam(required = false) EstadoOrdenCompra estado) {
        return ResponseEntity.ok(ordenCompraService.listar(estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una orden", description = "ADMIN o AGENTE. 404 si no existe.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<OrdenCompraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.buscarPorId(id));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado", description = "Solo ADMIN. 400 para transición inválida; 401/403; 404 si no existe.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdenCompraResponse> cambiarEstado(
                    @PathVariable Long id,
                    @Valid @RequestBody OrdenCompraEstadoRequest request) {
        return ResponseEntity.ok(ordenCompraService.cambiarEstado(id, request));
    }

    @PostMapping("/{id}/pdf")
    @Operation(summary = "Generar PDF", description = "ADMIN o AGENTE. 404 si la orden no existe.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        return respuestaPdf(id, pdfOrdenService.generar(id));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descargar PDF", description = "ADMIN o AGENTE. application/pdf; 404 si no existe o aún no se generó.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<byte[]> obtenerPdf(@PathVariable Long id) {
        return respuestaPdf(id, pdfOrdenService.obtener(id));
    }

    private ResponseEntity<byte[]> respuestaPdf(Long ordenId, byte[] pdf) {
        return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                        "inline; filename=orden-compra-" + ordenId + ".pdf")
                        .body(pdf);
    }
}
