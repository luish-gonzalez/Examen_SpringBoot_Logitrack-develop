package com.logitrack.dto;

import com.logitrack.enums.TipoMovimiento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequest {

    @NotNull(message = "El tipo de movimiento es obligatorio.")
    private TipoMovimiento tipo;

   /* @NotNull(message = "El usuario responsable es obligatorio.")
    private Long usuarioResponsableId;*/

    private Long bodegaOrigenId;

    private Long bodegaDestinoId;

    @NotEmpty(message = "Debe registrar al menos un detalle del movimiento.")
    private List<@Valid DetalleMovimientoRequest> detalles;

}
