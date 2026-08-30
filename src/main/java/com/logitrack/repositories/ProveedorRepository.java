package com.logitrack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logitrack.entities.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
}
