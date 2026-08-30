package com.logitrack.dto;

import java.math.BigDecimal;

public record OcupacionBodegaResponse(
        Long bodegaId,
        String nombre,
        Integer capacidad,
        long unidadesAlmacenadas,
        BigDecimal porcentaje) {
}
