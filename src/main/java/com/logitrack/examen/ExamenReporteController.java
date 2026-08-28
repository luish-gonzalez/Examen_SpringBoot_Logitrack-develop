package com.logitrack.examen;

import com.logitrack.entities.Auditoria;
import com.logitrack.entities.Movimiento;
import com.logitrack.enums.TipoMovimiento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ExamenReporteController {

    private final ExamenReporteService examenReporteService;

    public ExamenReporteController(
            ExamenReporteService examenReporteService) {

        this.examenReporteService = examenReporteService;
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<Movimiento>> obtenerMovimientos(
            @RequestParam(required = false)
            Long bodegaId,

            @RequestParam(required = false)
            Long productoId,

            @RequestParam(required = false)
            TipoMovimiento tipoMovimiento,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFin) {

        List<Movimiento> movimientos =
                examenReporteService.obtenerMovimientos(
                        bodegaId,
                        productoId,
                        tipoMovimiento,
                        fechaInicio,
                        fechaFin
                );

        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<Auditoria>> obtenerAuditorias(
            @RequestParam(required = false)
            Long productoId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFin,

            @RequestParam(required = false)
            String campoModificado) {

        List<Auditoria> auditorias =
                examenReporteService.obtenerAuditorias(
                        productoId,
                        fechaInicio,
                        fechaFin,
                        campoModificado
                );

        return ResponseEntity.ok(auditorias);
    }
}