package com.logitrack.controllers;

import com.logitrack.dto.ResumenGeneralDto;
import com.logitrack.services.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenGeneralDto> obtenerResumenGeneral() {

        ResumenGeneralDto resumen =
                reporteService.obtenerResumenGeneral();

        return ResponseEntity.ok(resumen);
    }

}
