package com.logitrack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDto {

    private Long id;

    @NotNull(message = "El identificador de la bodega es obligatorio.")
    private Long bodegaId;

    @NotNull(message = "El identificador del producto es obligatorio.")
    private Long productoId;

    @NotNull(message = "El stock es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;
}