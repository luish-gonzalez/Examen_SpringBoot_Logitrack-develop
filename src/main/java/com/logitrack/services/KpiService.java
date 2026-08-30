package com.logitrack.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logitrack.dto.KpiResponse;
import com.logitrack.dto.MovimientosAyerResponse;
import com.logitrack.dto.OcupacionBodegaResponse;
import com.logitrack.dto.OrdenesPorAprobarResponse;
import com.logitrack.entities.Bodega;
import com.logitrack.enums.EstadoOrdenCompra;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.exceptions.BusinessException;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.MovimientoRepository;
import com.logitrack.repositories.OrdenCompraRepository;
import com.logitrack.repositories.ProductoRepository;

@Service
@Transactional(readOnly = true)
public class KpiService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final MovimientoRepository movimientoRepository;
    private final StockDerivadoService stockDerivadoService;
    private final ProductoRiesgoService productoRiesgoService;
    private final Clock clock;

    public KpiService(
            BodegaRepository bodegaRepository,
            ProductoRepository productoRepository,
            OrdenCompraRepository ordenCompraRepository,
            MovimientoRepository movimientoRepository,
            StockDerivadoService stockDerivadoService,
            ProductoRiesgoService productoRiesgoService,
            Clock clock) {
        this.bodegaRepository = bodegaRepository;
        this.productoRepository = productoRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.movimientoRepository = movimientoRepository;
        this.stockDerivadoService = stockDerivadoService;
        this.productoRiesgoService = productoRiesgoService;
        this.clock = clock;
    }

    public KpiResponse obtenerKpis() {
        return new KpiResponse(
                OffsetDateTime.now(clock),
                obtenerOcupacionPorBodega(),
                contarProductosEnQuiebre(),
                productoRiesgoService.listarProductosEnRiesgo().size(),
                calcularOrdenesPorAprobar(),
                calcularMovimientosAyer());
    }

    public List<OcupacionBodegaResponse> obtenerOcupacionPorBodega() {
        return bodegaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::calcularOcupacion)
                .toList();
    }

    private OcupacionBodegaResponse calcularOcupacion(Bodega bodega) {
        if (bodega.getCapacidad() == null || bodega.getCapacidad() <= 0) {
            throw new BusinessException("La capacidad de la bodega debe ser mayor que cero.");
        }

        long unidadesAlmacenadas = productoRepository.findAll().stream()
                .mapToLong(producto -> stockDerivadoService.obtenerStockEnBodega(
                        producto.getId(), bodega.getId()))
                .sum();
        BigDecimal porcentaje = BigDecimal.valueOf(unidadesAlmacenadas)
                .multiply(CIEN)
                .divide(BigDecimal.valueOf(bodega.getCapacidad()), 2, RoundingMode.HALF_UP);

        return new OcupacionBodegaResponse(
                bodega.getId(),
                bodega.getNombre(),
                bodega.getCapacidad(),
                unidadesAlmacenadas,
                porcentaje);
    }

    private long contarProductosEnQuiebre() {
        return productoRepository.findAll().stream()
                .filter(producto -> stockDerivadoService
                        .obtenerStockProducto(producto.getId())
                        .getStockTotal() == 0L)
                .count();
    }

    private OrdenesPorAprobarResponse calcularOrdenesPorAprobar() {
        List<com.logitrack.entities.OrdenCompra> borradores = ordenCompraRepository
                .findByEstadoOrderByIdAsc(EstadoOrdenCompra.BORRADOR);
        BigDecimal montoTotal = borradores.stream()
                .map(com.logitrack.entities.OrdenCompra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return new OrdenesPorAprobarResponse(borradores.size(), montoTotal);
    }

    private MovimientosAyerResponse calcularMovimientosAyer() {
        LocalDate hoy = LocalDate.now(clock);
        LocalDateTime inicio = hoy.minusDays(1).atStartOfDay();
        LocalDateTime fin = hoy.atStartOfDay();

        long entrada = 0;
        long salida = 0;
        long transferencia = 0;
        for (com.logitrack.entities.Movimiento movimiento : movimientoRepository
                .findByFechaGreaterThanEqualAndFechaLessThan(inicio, fin)) {
            switch (movimiento.getTipo()) {
                case ENTRADA -> entrada++;
                case SALIDA -> salida++;
                case TRANSFERENCIA -> transferencia++;
            }
        }
        return new MovimientosAyerResponse(entrada, salida, transferencia);
    }
}
