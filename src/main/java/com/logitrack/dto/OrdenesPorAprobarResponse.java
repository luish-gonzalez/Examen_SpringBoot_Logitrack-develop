package com.logitrack.dto;

import java.math.BigDecimal;

public record OrdenesPorAprobarResponse(
        long cantidad,
        BigDecimal montoTotal) {
}
