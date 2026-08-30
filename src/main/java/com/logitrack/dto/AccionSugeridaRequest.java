package com.logitrack.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logitrack.enums.TipoAccionSugerida;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccionSugeridaRequest {

    @NotNull(message = "El tipo de acción sugerida es obligatorio.")
    private TipoAccionSugerida tipo;

    @NotBlank(message = "La descripción de la acción sugerida es obligatoria.")
    @Size(max = 500, message = "La descripción de la acción sugerida no puede superar 500 caracteres.")
    private String descripcion;

    private Long ordenId;
    private Long productoId;
    private Long bodegaId;

    @JsonIgnore
    private final Map<String, Object> propiedadesAdicionales = new LinkedHashMap<>();

    @JsonAnySetter
    void agregarPropiedadAdicional(String nombre, Object valor) {
        propiedadesAdicionales.put(nombre, valor);
    }

    @AssertTrue(message = "La acción sugerida debe enlazar exactamente un recurso.")
    @JsonIgnore
    public boolean tieneExactamenteUnEnlace() {
        int enlaces = 0;
        if (ordenId != null) {
            enlaces++;
        }
        if (productoId != null) {
            enlaces++;
        }
        if (bodegaId != null) {
            enlaces++;
        }
        return enlaces == 1;
    }

    @AssertTrue(message = "La acción sugerida contiene propiedades no permitidas.")
    @JsonIgnore
    public boolean sinPropiedadesAdicionales() {
        return propiedadesAdicionales.isEmpty();
    }
}
