package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CalculadoraMetricasInventarioTest {

    private static final String CLASE_CALCULADORA =
            "com.logitrack.services.CalculadoraMetricasInventario";

    @Test
    void consumoCeroProduceCoberturaNulaYEstadoSinConsumo() {
        Class<?> tipo = assertDoesNotThrow(
                () -> Class.forName(CLASE_CALCULADORA),
                "Debe existir la superficie de cálculo de métricas de inventario");
        Object calculadora = assertDoesNotThrow(
                () -> tipo.getDeclaredConstructor().newInstance(),
                "La calculadora debe poder probarse sin infraestructura externa");
        Method calcularDias = assertDoesNotThrow(
                () -> tipo.getMethod(
                        "calcularDiasCobertura",
                        BigDecimal.class,
                        BigDecimal.class));
        Method determinarEstado = assertDoesNotThrow(
                () -> tipo.getMethod(
                        "determinarEstadoCobertura",
                        BigDecimal.class));

        Object diasCobertura = assertDoesNotThrow(
                () -> calcularDias.invoke(
                        calculadora,
                        new BigDecimal("25"),
                        BigDecimal.ZERO));
        Object estadoCobertura = assertDoesNotThrow(
                () -> determinarEstado.invoke(calculadora, BigDecimal.ZERO));

        assertNull(diasCobertura);
        assertEquals("SIN_CONSUMO", estadoCobertura.toString());
    }
}
