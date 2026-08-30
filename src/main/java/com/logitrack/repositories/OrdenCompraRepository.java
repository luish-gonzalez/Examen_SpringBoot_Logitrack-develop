package com.logitrack.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logitrack.entities.OrdenCompra;
import com.logitrack.enums.EstadoOrdenCompra;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    List<OrdenCompra> findByEstadoOrderByIdAsc(EstadoOrdenCompra estado);

    List<OrdenCompra> findAllByOrderByIdAsc();
}
