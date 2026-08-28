package com.logitrack.dto;

import java.time.LocalDateTime;

import com.logitrack.enums.TipoOperacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaDto {

    private Long id;

    private TipoOperacion tipoOperacion;

    private LocalDateTime fechaHora;

    private String usuario;

    private String entidadAfectada;

    private Long entidadId;

    private String valoresAnteriores;

    private String valoresNuevos;

}