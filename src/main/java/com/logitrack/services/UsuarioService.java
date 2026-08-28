package com.logitrack.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.logitrack.entities.Usuario;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return buscarUsuarioPorId(id);
    }

    public Usuario obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con username: " + username
                ));
    }

    public Usuario cambiarEstado(Long id, Boolean activo) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setActivo(activo);

        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + id
                ));
    }
}