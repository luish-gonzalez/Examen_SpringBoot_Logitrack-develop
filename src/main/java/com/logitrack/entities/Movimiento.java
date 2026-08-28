package com.logitrack.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.logitrack.enums.TipoMovimiento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha es obligatoria.")
    private LocalDateTime fecha;

    @NotNull(message = "El tipo de movimiento es obligatorio.")
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    @NotNull(message = "El usuario responsable es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "usuario_responsable_id", nullable = false)
    private Usuario usuarioResponsable;

    @ManyToOne
    @JoinColumn(name = "bodega_origen_id")
    private Bodega bodegaOrigen;

    @ManyToOne
    @JoinColumn(name = "bodega_destino_id")
    private Bodega bodegaDestino;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<@Valid DetalleMovimiento> detalles = new ArrayList<>();
}