package com.logitrack.entities;

import com.logitrack.enums.TipoOperacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de operación es obligatorio.")
    @Enumerated(EnumType.STRING)
    private TipoOperacion tipoOperacion;

    @NotNull(message = "La fecha y hora son obligatorias.")
    private LocalDateTime fechaHora;

    @NotBlank(message = "El usuario es obligatorio.")
    @Column(nullable = false)
    private String usuario;

    @NotBlank(message = "La entidad afectada es obligatoria.")
    @Column(nullable = false)
    private String entidadAfectada;

    @NotNull(message = "El identificador de la entidad es obligatorio.")
    @Column(nullable = false)
    private Long entidadId;

    @Column(columnDefinition = "TEXT")
    private String valoresAnteriores;

    @Column(columnDefinition = "TEXT")
    private String valoresNuevos;
}