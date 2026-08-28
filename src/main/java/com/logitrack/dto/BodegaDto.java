package com.logitrack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodegaDto {

    private Long id;

    @NotBlank(message = "El nombre de la bodega es obligatorio")
    @Size(
            max = 100,
            message = "El nombre de la bodega no puede superar los 100 caracteres"
    )
    private String nombre;

    @NotBlank(message = "La ubicación de la bodega es obligatoria")
    @Size(
            max = 150,
            message = "La ubicación no puede superar los 150 caracteres"
    )
    private String ubicacion;

    @NotNull(message = "La capacidad de la bodega es obligatoria")
    @Min(
            value = 1,
            message = "La capacidad de la bodega debe ser mayor que cero"
    )
    private Integer capacidad;

    @NotNull(message = "El encargado de la bodega es obligatorio")
    private Long encargadoId;
}