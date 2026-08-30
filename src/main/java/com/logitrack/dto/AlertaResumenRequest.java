package com.logitrack.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logitrack.enums.SeveridadAlerta;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AlertaResumenRequest {

    @NotNull(message = "La severidad de la alerta es obligatoria.")
    private SeveridadAlerta severidad;

    @NotBlank(message = "El título de la alerta es obligatorio.")
    @Size(max = 150, message = "El título de la alerta no puede superar 150 caracteres.")
    private String titulo;

    @NotBlank(message = "El detalle de la alerta es obligatorio.")
    @Size(max = 500, message = "El detalle de la alerta no puede superar 500 caracteres.")
    private String detalle;

    private Long productoId;
    private Long ordenId;
    private Long bodegaId;

    @JsonIgnore
    private final Map<String, Object> propiedadesAdicionales = new LinkedHashMap<>();

    @JsonAnySetter
    void agregarPropiedadAdicional(String nombre, Object valor) {
        propiedadesAdicionales.put(nombre, valor);
    }

    @AssertTrue(message = "La alerta debe enlazar al menos un producto, orden o bodega.")
    @JsonIgnore
    public boolean tieneAlgunEnlace() {
        return productoId != null || ordenId != null || bodegaId != null;
    }

    @AssertTrue(message = "La alerta contiene propiedades no permitidas.")
    @JsonIgnore
    public boolean sinPropiedadesAdicionales() {
        return propiedadesAdicionales.isEmpty();
    }
}
