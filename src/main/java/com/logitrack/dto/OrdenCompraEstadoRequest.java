package com.logitrack.dto;

import com.logitrack.enums.EstadoOrdenCompra;

import jakarta.validation.constraints.NotNull;

public record OrdenCompraEstadoRequest(
                @NotNull(message = "El estado es obligatorio.") EstadoOrdenCompra estado) {
}
