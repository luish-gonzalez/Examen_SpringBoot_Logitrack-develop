package com.logitrack.examen;

import com.logitrack.entities.Movimiento;
import com.logitrack.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamenMovimientoRepository
        extends JpaRepository<Movimiento, Long> {

    @Query("""
            SELECT DISTINCT movimiento
            FROM Movimiento movimiento
            LEFT JOIN movimiento.detalles detalle
            WHERE (
                :bodegaId IS NULL
                OR movimiento.bodegaOrigen.id = :bodegaId
                OR movimiento.bodegaDestino.id = :bodegaId
            )
            AND (
                :productoId IS NULL
                OR detalle.producto.id = :productoId
            )
            AND (
                :tipoMovimiento IS NULL
                OR movimiento.tipo = :tipoMovimiento
            )
            AND movimiento.fecha >= COALESCE(
                :fechaInicio,
                movimiento.fecha
            )
            AND movimiento.fecha <= COALESCE(
                :fechaFin,
                movimiento.fecha
            )
            ORDER BY movimiento.fecha DESC
            """)
    List<Movimiento> buscarMovimientosConFiltros(
            @Param("bodegaId") Long bodegaId,
            @Param("productoId") Long productoId,
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}