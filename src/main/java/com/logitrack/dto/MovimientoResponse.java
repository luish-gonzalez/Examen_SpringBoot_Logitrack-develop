package com.logitrack.dto;

import com.logitrack.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponse {

    private Long id;

    private LocalDateTime fecha;

    private TipoMovimiento tipo;

    private Long usuarioResponsableId;

    private String usuarioResponsableNombre;

    private Long bodegaOrigenId;

    private Long bodegaDestinoId;
    private List<DetalleMovimientoRequest> detalles;

}