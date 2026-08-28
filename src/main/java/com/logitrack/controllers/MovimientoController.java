package com.logitrack.controllers;

import com.logitrack.dto.MovimientoRequest;
import com.logitrack.dto.MovimientoResponse;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.services.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public ResponseEntity<List<MovimientoResponse>> listarTodos() {
        return ResponseEntity.ok(movimientoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(movimientoService.buscarPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoResponse>> buscarPorTipo(
            @PathVariable TipoMovimiento tipo) {

        return ResponseEntity.ok(movimientoService.buscarPorTipo(tipo));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MovimientoResponse>> buscarPorUsuario(
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(
                movimientoService.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/bodega-origen/{bodegaId}")
    public ResponseEntity<List<MovimientoResponse>> buscarPorBodegaOrigen(
            @PathVariable Long bodegaId) {

        return ResponseEntity.ok(
                movimientoService.buscarPorBodegaOrigen(bodegaId));
    }

    @GetMapping("/bodega-destino/{bodegaId}")
    public ResponseEntity<List<MovimientoResponse>> buscarPorBodegaDestino(
            @PathVariable Long bodegaId) {

        return ResponseEntity.ok(
                movimientoService.buscarPorBodegaDestino(bodegaId));
    }

    @PostMapping
    public ResponseEntity<MovimientoResponse> registrarMovimiento(
            @Valid @RequestBody MovimientoRequest request) {

        MovimientoResponse response = movimientoService.registrarMovimiento(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        movimientoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<MovimientoResponse>> buscarPorRangoFechas(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin) {

        return ResponseEntity.ok(
                movimientoService.buscarPorRangoFechas(
                        fechaInicio,
                        fechaFin));
    }

}