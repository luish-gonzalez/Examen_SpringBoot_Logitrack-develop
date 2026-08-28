package com.logitrack.repositories;

import com.logitrack.entities.Auditoria;
import com.logitrack.enums.TipoOperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByUsuario(String usuario);

    List<Auditoria> findByTipoOperacion(TipoOperacion tipoOperacion);

    List<Auditoria> findByEntidadAfectada(String entidadAfectada);

    List<Auditoria> findByEntidadId(Long entidadId);

    List<Auditoria> findByFechaHoraBetween(LocalDateTime fechaInicio,
                                           LocalDateTime fechaFin);

    List<Auditoria> findByTipoOperacionAndFechaHoraBetween(
            TipoOperacion tipoOperacion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}
