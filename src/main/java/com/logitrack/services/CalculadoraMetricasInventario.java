package com.logitrack.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.logitrack.enums.EstadoCobertura;

@Component
public class CalculadoraMetricasInventario {

    public BigDecimal calcularDiasCobertura(
            BigDecimal stockTotal,
            BigDecimal consumoDiarioPromedio) {
        Objects.requireNonNull(stockTotal, "El stock total es obligatorio.");
        validarConsumo(consumoDiarioPromedio);

        if (consumoDiarioPromedio.signum() == 0) {
            return null;
        }

        return stockTotal.divide(consumoDiarioPromedio, MathContext.DECIMAL128);
    }

    public EstadoCobertura determinarEstadoCobertura(
            BigDecimal consumoDiarioPromedio) {
        validarConsumo(consumoDiarioPromedio);
        return consumoDiarioPromedio.signum() == 0
                ? EstadoCobertura.SIN_CONSUMO
                : EstadoCobertura.CON_CONSUMO;
    }

    private void validarConsumo(BigDecimal consumoDiarioPromedio) {
        Objects.requireNonNull(
                consumoDiarioPromedio,
                "El consumo diario promedio es obligatorio.");
        if (consumoDiarioPromedio.signum() < 0) {
            throw new IllegalArgumentException(
                    "El consumo diario promedio no puede ser negativo.");
        }
    }
}
