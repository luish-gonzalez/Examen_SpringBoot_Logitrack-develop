package com.logitrack.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoStockResponse {

    private Long productoId;
    private String nombreProducto;
    private Long stockTotal;
    private List<StockBodegaResponse> stockPorBodega;
}
