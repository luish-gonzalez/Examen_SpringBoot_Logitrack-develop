package com.logitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.logitrack.enums.EstadoOrdenCompra;

public record OrdenCompraResponse(
                Long id,
                Long productoId,
                Long proveedorId,
                Long bodegaDestinoId,
                Integer cantidad,
                BigDecimal precioUnitario,
                BigDecimal total,
                LocalDateTime fechaCreacion,
                EstadoOrdenCompra estado,
                Long creadoPorId) {
}
