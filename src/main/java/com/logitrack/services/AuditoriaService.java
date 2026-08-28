package com.logitrack.services;

import com.logitrack.dto.AuditoriaDto;
import com.logitrack.entities.Auditoria;
import com.logitrack.enums.TipoOperacion;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.AuditoriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public List<AuditoriaDto> listarTodas() {

        return auditoriaRepository.findAll()
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public AuditoriaDto buscarPorId(Long id) {

        Auditoria auditoria = auditoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Auditoría no encontrada con id: " + id));

        return convertirADto(auditoria);
    }

    public List<AuditoriaDto> buscarPorUsuario(String usuario) {

        return auditoriaRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public List<AuditoriaDto> buscarPorTipoOperacion(TipoOperacion tipoOperacion) {

        return auditoriaRepository.findByTipoOperacion(tipoOperacion)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public List<AuditoriaDto> buscarPorEntidad(String entidadAfectada) {

        return auditoriaRepository.findByEntidadAfectada(entidadAfectada)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public List<AuditoriaDto> buscarPorEntidadId(Long entidadId) {

        return auditoriaRepository.findByEntidadId(entidadId)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public List<AuditoriaDto> buscarPorRangoFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        return auditoriaRepository
                .findByFechaHoraBetween(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuditoriaDto guardar(AuditoriaDto dto) {

        Auditoria auditoria = convertirAEntidad(dto);

        auditoria = auditoriaRepository.save(auditoria);

        return convertirADto(auditoria);
    }

    @Transactional
    public void eliminar(Long id) {

        Auditoria auditoria = auditoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Auditoría no encontrada con id: " + id));

        auditoriaRepository.delete(auditoria);
    }

    @Transactional
public void registrar(
        TipoOperacion tipoOperacion,
        String usuario,
        String entidadAfectada,
        Long entidadId,
        String valoresAnteriores,
        String valoresNuevos) {

    Auditoria auditoria = new Auditoria();

    auditoria.setTipoOperacion(tipoOperacion);
    auditoria.setFechaHora(LocalDateTime.now());
    auditoria.setUsuario(usuario);
    auditoria.setEntidadAfectada(entidadAfectada);
    auditoria.setEntidadId(entidadId);
    auditoria.setValoresAnteriores(valoresAnteriores);
    auditoria.setValoresNuevos(valoresNuevos);

    auditoriaRepository.save(auditoria);
}

    private AuditoriaDto convertirADto(Auditoria auditoria) {

        return new AuditoriaDto(
                auditoria.getId(),
                auditoria.getTipoOperacion(),
                auditoria.getFechaHora(),
                auditoria.getUsuario(),
                auditoria.getEntidadAfectada(),
                auditoria.getEntidadId(),
                auditoria.getValoresAnteriores(),
                auditoria.getValoresNuevos()
        );
    }

    private Auditoria convertirAEntidad(AuditoriaDto dto) {

        Auditoria auditoria = new Auditoria();

        auditoria.setId(dto.getId());
        auditoria.setTipoOperacion(dto.getTipoOperacion());
        auditoria.setFechaHora(dto.getFechaHora());
        auditoria.setUsuario(dto.getUsuario());
        auditoria.setEntidadAfectada(dto.getEntidadAfectada());
        auditoria.setEntidadId(dto.getEntidadId());
        auditoria.setValoresAnteriores(dto.getValoresAnteriores());
        auditoria.setValoresNuevos(dto.getValoresNuevos());

        return auditoria;
    }
}