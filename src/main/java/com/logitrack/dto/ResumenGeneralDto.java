package com.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenGeneralDto {

    private Long totalProductos;

    private Long totalBodegas;

    private Long totalUsuarios;

    private Long totalMovimientos;

    private Long totalRegistrosInventario;

    private Integer stockTotal;

}