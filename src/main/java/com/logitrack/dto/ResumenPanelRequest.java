package com.logitrack.dto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResumenPanelRequest {

    @NotNull(message = "La fecha del resumen es obligatoria.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotBlank(message = "La narrativa es obligatoria.")
    @Size(min = 20, max = 500,
                    message = "La narrativa debe tener entre 20 y 500 caracteres.")
    private String narrativa;

    @NotNull(message = "Las alertas son obligatorias.")
    @Valid
    private List<AlertaResumenRequest> alertas;

    @NotNull(message = "Las acciones sugeridas son obligatorias.")
    @Valid
    private List<AccionSugeridaRequest> accionesSugeridas;

    @JsonIgnore
    private final Map<String, Object> propiedadesAdicionales = new LinkedHashMap<>();

    @JsonAnySetter
    void agregarPropiedadAdicional(String nombre, Object valor) {
        propiedadesAdicionales.put(nombre, valor);
    }

    @AssertTrue(message = "El resumen contiene propiedades no permitidas.")
    @JsonIgnore
    public boolean sinPropiedadesAdicionales() {
        return propiedadesAdicionales.isEmpty();
    }
}
