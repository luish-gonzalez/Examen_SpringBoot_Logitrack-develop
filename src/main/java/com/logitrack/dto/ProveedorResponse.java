package com.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorResponse {

    private Long id;
    private String nombre;
    private String contacto;
    private Integer diasEntrega;
}
