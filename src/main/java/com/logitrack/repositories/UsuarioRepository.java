package com.logitrack.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logitrack.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}