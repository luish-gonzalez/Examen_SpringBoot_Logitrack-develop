package com.logitrack.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.logitrack.dto.BodegaDto;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.Usuario;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.UsuarioRepository;

@Service
public class BodegaService {

    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;

    public BodegaService(
            BodegaRepository bodegaRepository,
            UsuarioRepository usuarioRepository) {

        this.bodegaRepository = bodegaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<BodegaDto> listarTodas() {
        return bodegaRepository.findAll()
                .stream()
                .map(this::convertirADto)
                .toList();
    }

    public BodegaDto obtenerPorId(Long id) {
        Bodega bodega = buscarBodegaPorId(id);
        return convertirADto(bodega);
    }

    public BodegaDto crear(BodegaDto bodegaDto) {
        Usuario encargado = buscarUsuarioPorId(bodegaDto.getEncargadoId());

        Bodega bodega = new Bodega();

        bodega.setNombre(bodegaDto.getNombre().trim());
        bodega.setUbicacion(bodegaDto.getUbicacion().trim());
        bodega.setCapacidad(bodegaDto.getCapacidad());
        bodega.setEncargado(encargado);

        Bodega bodegaGuardada = bodegaRepository.save(bodega);

        return convertirADto(bodegaGuardada);
    }

    public BodegaDto actualizar(Long id, BodegaDto bodegaDto) {
        Bodega bodega = buscarBodegaPorId(id);
        Usuario encargado = buscarUsuarioPorId(bodegaDto.getEncargadoId());

        bodega.setNombre(bodegaDto.getNombre().trim());
        bodega.setUbicacion(bodegaDto.getUbicacion().trim());
        bodega.setCapacidad(bodegaDto.getCapacidad());
        bodega.setEncargado(encargado);

        Bodega bodegaActualizada = bodegaRepository.save(bodega);

        return convertirADto(bodegaActualizada);
    }

    public void eliminar(Long id) {
        Bodega bodega = buscarBodegaPorId(id);
        bodegaRepository.delete(bodega);
    }

    private Bodega buscarBodegaPorId(Long id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bodega no encontrada con id: " + id
                ));
    }

    private Usuario buscarUsuarioPorId(Long encargadoId) {
        return usuarioRepository.findById(encargadoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario encargado no encontrado con id: "
                                + encargadoId
                ));
    }

    private BodegaDto convertirADto(Bodega bodega) {
        return new BodegaDto(
                bodega.getId(),
                bodega.getNombre(),
                bodega.getUbicacion(),
                bodega.getCapacidad(),
                bodega.getEncargado().getId()
        );
    }
}