package com.logitrack.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record KpiResponse(
        OffsetDateTime calculadoEn,
        List<OcupacionBodegaResponse> ocupacionPorBodega,
        long productosEnQuiebre,
        long productosEnRiesgo,
        OrdenesPorAprobarResponse ordenesPorAprobar,
        MovimientosAyerResponse movimientosAyer) {
}
