package com.logitrack.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {

    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(
            max = 100,
            message = "El nombre del producto no puede superar los 100 caracteres"
    )
    private String nombre;

    @NotBlank(message = "La categoría del producto es obligatoria")
    @Size(
            max = 100,
            message = "La categoría no puede superar los 100 caracteres"
    )
    private String categoria;

    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(
            value = "0.01",
            inclusive = true,
            message = "El precio del producto debe ser mayor que cero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "El precio debe tener máximo 10 enteros y 2 decimales"
    )
    private BigDecimal precio;
}