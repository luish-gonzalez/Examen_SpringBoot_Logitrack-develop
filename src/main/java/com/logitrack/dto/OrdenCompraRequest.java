package com.logitrack.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrdenCompraRequest(
                @NotNull(message = "El producto es obligatorio.") Long productoId,
                @NotNull(message = "El proveedor es obligatorio.") Long proveedorId,
                @NotNull(message = "La bodega destino es obligatoria.") Long bodegaDestinoId,
                @NotNull(message = "La cantidad es obligatoria.")
                @Min(value = 1, message = "La cantidad debe ser mayor que cero.") Integer cantidad,
                @NotNull(message = "El precio unitario es obligatorio.")
                @Digits(integer = 10, fraction = 2,
                                message = "El precio unitario debe tener máximo 10 enteros y 2 decimales.") BigDecimal precioUnitario) {
}
