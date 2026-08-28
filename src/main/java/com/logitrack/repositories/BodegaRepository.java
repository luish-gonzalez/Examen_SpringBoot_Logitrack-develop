package com.logitrack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logitrack.entities.Bodega;

public interface BodegaRepository extends JpaRepository<Bodega, Long> {
}