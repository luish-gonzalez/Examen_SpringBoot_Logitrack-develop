package com.logitrack.dto;

import java.math.BigDecimal;

import com.logitrack.enums.EstadoCobertura;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRiesgoResponse {

    private Long productoId;
    private String nombreProducto;
    private Long proveedorId;
    private Long stockTotal;
    private BigDecimal consumoDiarioPromedio;
    private BigDecimal puntoReorden;
    private BigDecimal diasCobertura;
    private EstadoCobertura estadoCobertura;
    private Long bodegaDestinoId;
    private BigDecimal precioUnitarioSugerido;
}
