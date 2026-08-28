package com.logitrack.controllers;

import com.logitrack.dto.AuditoriaDto;
import com.logitrack.enums.TipoOperacion;
import com.logitrack.services.AuditoriaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auditorias")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<AuditoriaDto>> listarTodas(Authentication authentication) {
        List<AuditoriaDto> auditorias = auditoriaService.listarTodas();

        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_EMPLEADO"))) {
            String usuarioActual = authentication.getName();
            auditorias = auditorias.stream()
                    .filter(auditoria -> usuarioActual.equals(auditoria.getUsuario()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(auditorias);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditoriaDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuario}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaDto>> buscarPorUsuario(
            @PathVariable String usuario) {

        return ResponseEntity.ok(
                auditoriaService.buscarPorUsuario(usuario));
    }

    @GetMapping("/tipo/{tipoOperacion}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaDto>> buscarPorTipoOperacion(
            @PathVariable TipoOperacion tipoOperacion) {

        return ResponseEntity.ok(
                auditoriaService.buscarPorTipoOperacion(tipoOperacion));
    }

    @GetMapping("/entidad/{entidadAfectada}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaDto>> buscarPorEntidad(
            @PathVariable String entidadAfectada) {

        return ResponseEntity.ok(
                auditoriaService.buscarPorEntidad(entidadAfectada));
    }

    @GetMapping("/registro/{entidadId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaDto>> buscarPorEntidadId(
            @PathVariable Long entidadId) {

        return ResponseEntity.ok(
                auditoriaService.buscarPorEntidadId(entidadId));
    }

    @GetMapping("/fechas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaDto>> buscarPorRangoFechas(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFin) {

        return ResponseEntity.ok(
                auditoriaService.buscarPorRangoFechas(
                        fechaInicio,
                        fechaFin));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditoriaDto> guardar(
            @Valid @RequestBody AuditoriaDto auditoriaDto) {

        AuditoriaDto respuesta = auditoriaService.guardar(auditoriaDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        auditoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}