package com.logitrack.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logitrack.entities.ResumenPanel;

public interface ResumenPanelRepository extends JpaRepository<ResumenPanel, Long> {

    Optional<ResumenPanel> findByFecha(LocalDate fecha);

    Optional<ResumenPanel> findTopByOrderByFechaDesc();
}
