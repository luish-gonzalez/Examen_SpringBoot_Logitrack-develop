package com.logitrack.repositories;

import com.logitrack.entities.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByBodegaId(Long bodegaId);

    List<Inventario> findByProductoId(Long productoId);

    Optional<Inventario> findByBodegaIdAndProductoId(Long bodegaId, Long productoId);

    List<Inventario> findByStockLessThan(Integer stock);
}