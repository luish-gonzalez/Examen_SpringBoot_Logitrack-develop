package com.logitrack.repositories;

import com.logitrack.entities.Movimiento;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByTipo(TipoMovimiento tipo);

    List<Movimiento> findByUsuarioResponsable(Usuario usuarioResponsable);

    List<Movimiento> findByBodegaOrigenId(Long bodegaOrigenId);

    List<Movimiento> findByBodegaDestinoId(Long bodegaDestinoId);

    List<Movimiento> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Movimiento> findByTipoAndFechaBetween(
            TipoMovimiento tipo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}