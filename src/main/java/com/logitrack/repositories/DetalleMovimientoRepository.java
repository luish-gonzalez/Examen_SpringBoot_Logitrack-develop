package com.logitrack.repositories;

import com.logitrack.entities.DetalleMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {

    List<DetalleMovimiento> findByMovimientoId(Long movimientoId);

    List<DetalleMovimiento> findByProductoId(Long productoId);

}