package com.logitrack.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre del producto no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La categoría del producto es obligatoria")
    @Size(max = 80, message = "La categoría no puede superar los 80 caracteres")
    @Column(nullable = false, length = 80)
    private String categoria;

    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(
            value = "0.01",
            inclusive = true,
            message = "El precio del producto debe ser mayor que cero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "El precio debe tener máximo 10 enteros y 2 decimales"
    )
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;
}