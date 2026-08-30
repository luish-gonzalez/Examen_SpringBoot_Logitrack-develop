package com.logitrack.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proveedor es obligatorio.")
    @Size(max = 100, message = "El nombre del proveedor no puede superar los 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "El contacto no puede superar los 255 caracteres.")
    @Column(length = 255)
    private String contacto;

    @NotNull(message = "Los días de entrega son obligatorios.")
    @Min(value = 1, message = "Los días de entrega deben ser al menos 1.")
    @Max(value = 90, message = "Los días de entrega no pueden superar 90.")
    @Column(nullable = false)
    private Integer diasEntrega;
}
