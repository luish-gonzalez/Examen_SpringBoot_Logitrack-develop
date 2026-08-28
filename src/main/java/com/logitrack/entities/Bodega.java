package com.logitrack.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;




@Entity
@Table(name = "bodegas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bodega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la bodega es obligatorio")
    @Size(max = 100, message = "El nombre de la bodega no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La ubicación de la bodega es obligatoria")
    @Size(max = 150, message = "La ubicación no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String ubicacion;

    @NotNull(message = "La capacidad de la bodega es obligatoria")
    @Min(value = 1, message = "La capacidad de la bodega debe ser mayor que cero")
    @Column(nullable = false)
    private Integer capacidad;

    @NotNull(message = "El encargado de la bodega es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "encargado_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario encargado;
}