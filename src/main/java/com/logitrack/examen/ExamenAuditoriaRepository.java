package com.logitrack.examen;

import com.logitrack.entities.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamenAuditoriaRepository
        extends JpaRepository<Auditoria, Long> {

    @Query("""
            SELECT auditoria
            FROM Auditoria auditoria
            WHERE (
                :productoId IS NULL
                OR (
                    auditoria.entidadAfectada = 'Producto'
                    AND auditoria.entidadId = :productoId
                )
            )
            AND auditoria.fechaHora >= COALESCE(
                :fechaInicio,
                auditoria.fechaHora
            )
            AND auditoria.fechaHora <= COALESCE(
                :fechaFin,
                auditoria.fechaHora
            )
            AND (
                COALESCE(:campoModificado, '') = ''
                OR LOWER(COALESCE(auditoria.valoresAnteriores, ''))
                    LIKE LOWER(
                        CONCAT(
                            '%',
                            COALESCE(:campoModificado, ''),
                            '%'
                        )
                    )
                OR LOWER(COALESCE(auditoria.valoresNuevos, ''))
                    LIKE LOWER(
                        CONCAT(
                            '%',
                            COALESCE(:campoModificado, ''),
                            '%'
                        )
                    )
            )
            ORDER BY auditoria.fechaHora DESC
            """)
    List<Auditoria> buscarAuditoriasConFiltros(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("campoModificado") String campoModificado
    );
}