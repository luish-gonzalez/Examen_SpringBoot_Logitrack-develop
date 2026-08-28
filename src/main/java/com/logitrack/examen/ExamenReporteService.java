package com.logitrack.examen;

import com.logitrack.entities.Auditoria;
import com.logitrack.entities.Movimiento;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExamenReporteService {

    private final ExamenMovimientoRepository examenMovimientoRepository;
    private final ExamenAuditoriaRepository examenAuditoriaRepository;

    public ExamenReporteService(
            ExamenMovimientoRepository examenMovimientoRepository,
            ExamenAuditoriaRepository examenAuditoriaRepository) {

        this.examenMovimientoRepository = examenMovimientoRepository;
        this.examenAuditoriaRepository = examenAuditoriaRepository;
    }

    public List<Movimiento> obtenerMovimientos(
            Long bodegaId,
            Long productoId,
            TipoMovimiento tipoMovimiento,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        validarIdentificador(bodegaId, "El identificador de la bodega");
        validarIdentificador(productoId, "El identificador del producto");
        validarRangoFechas(fechaInicio, fechaFin);

        return examenMovimientoRepository.buscarMovimientosConFiltros(
                bodegaId,
                productoId,
                tipoMovimiento,
                fechaInicio,
                fechaFin
        );
    }

    public List<Auditoria> obtenerAuditorias(
            Long productoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String campoModificado) {

        validarIdentificador(productoId, "El identificador del producto");
        validarRangoFechas(fechaInicio, fechaFin);

        String campoNormalizado = normalizarCampoModificado(campoModificado);

        return examenAuditoriaRepository.buscarAuditoriasConFiltros(
                productoId,
                fechaInicio,
                fechaFin,
                campoNormalizado
        );
    }

    private void validarRangoFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        if (fechaInicio != null
                && fechaFin != null
                && fechaInicio.isAfter(fechaFin)) {

            throw new BusinessException(
                    "La fecha de inicio no puede ser posterior a la fecha final."
            );
        }
    }

    private void validarIdentificador(Long id, String nombreCampo) {

        if (id != null && id <= 0) {
            throw new BusinessException(
                    nombreCampo + " debe ser mayor que cero."
            );
        }
    }

    private String normalizarCampoModificado(String campoModificado) {

        if (campoModificado == null || campoModificado.isBlank()) {
            return null;
        }

        return campoModificado.trim();
    }
}