package com.logitrack.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ResumenPanelResponse(
                @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fecha,
                String narrativa,
                List<AlertaResumenResponse> alertas,
                List<AccionSugeridaResponse> accionesSugeridas) {
}
