package com.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockBodegaResponse {

    private Long bodegaId;
    private String nombreBodega;
    private Long stock;
}
