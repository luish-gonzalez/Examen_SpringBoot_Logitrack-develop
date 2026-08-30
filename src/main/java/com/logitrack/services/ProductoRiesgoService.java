package com.logitrack.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logitrack.dto.ProductoRiesgoResponse;
import com.logitrack.dto.ProductoStockResponse;
import com.logitrack.dto.StockBodegaResponse;
import com.logitrack.entities.DetalleMovimiento;
import com.logitrack.entities.Producto;
import com.logitrack.enums.EstadoCobertura;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.repositories.DetalleMovimientoRepository;
import com.logitrack.repositories.ProductoRepository;

@Service
@Transactional(readOnly = true)
public class ProductoRiesgoService {

    private static final BigDecimal DIAS_DEL_PERIODO = BigDecimal.valueOf(30);
    private static final BigDecimal FACTOR_SEGURIDAD = new BigDecimal("1.5");

    private final ProductoRepository productoRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    private final StockDerivadoService stockDerivadoService;
    private final CalculadoraMetricasInventario calculadoraMetricasInventario;
    private final Clock clock;

    public ProductoRiesgoService(
            ProductoRepository productoRepository,
            DetalleMovimientoRepository detalleMovimientoRepository,
            StockDerivadoService stockDerivadoService,
            CalculadoraMetricasInventario calculadoraMetricasInventario,
            Clock clock) {
        this.productoRepository = productoRepository;
        this.detalleMovimientoRepository = detalleMovimientoRepository;
        this.stockDerivadoService = stockDerivadoService;
        this.calculadoraMetricasInventario = calculadoraMetricasInventario;
        this.clock = clock;
    }

    public List<ProductoRiesgoResponse> listarProductosEnRiesgo() {
        LocalDate hoy = LocalDate.now(clock);
        LocalDateTime inicio = hoy.minusDays(29).atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();

        return productoRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .filter(producto -> producto.getProveedorPrincipal() != null)
                .map(producto -> calcularRiesgo(producto, inicio, fin))
                .filter(ProductoRiesgoResponse.class::isInstance)
                .map(ProductoRiesgoResponse.class::cast)
                .toList();
    }

    private ProductoRiesgoResponse calcularRiesgo(
            Producto producto,
            LocalDateTime inicio,
            LocalDateTime fin) {
        BigDecimal consumo = calcularConsumoDiarioPromedio(
                producto.getId(), inicio, fin);
        ProductoStockResponse stock = stockDerivadoService
                .obtenerStockProducto(producto.getId());
        BigDecimal stockTotal = BigDecimal.valueOf(stock.getStockTotal());
        BigDecimal puntoReorden = consumo
                .multiply(BigDecimal.valueOf(
                        producto.getProveedorPrincipal().getDiasEntrega()))
                .multiply(FACTOR_SEGURIDAD);

        if (stockTotal.compareTo(puntoReorden) >= 0) {
            return null;
        }

        BigDecimal diasCobertura = calculadoraMetricasInventario
                .calcularDiasCobertura(stockTotal, consumo);
        EstadoCobertura estadoCobertura = calculadoraMetricasInventario
                .determinarEstadoCobertura(consumo);

        return new ProductoRiesgoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getProveedorPrincipal().getId(),
                stock.getStockTotal(),
                presentar(consumo),
                presentar(puntoReorden),
                diasCobertura == null ? null : presentar(diasCobertura),
                estadoCobertura,
                seleccionarBodegaDestino(stock.getStockPorBodega()),
                presentar(producto.getPrecio()));
    }

    private BigDecimal calcularConsumoDiarioPromedio(
            Long productoId,
            LocalDateTime inicio,
            LocalDateTime fin) {
        long unidadesSalida = detalleMovimientoRepository
                .findByProductoId(productoId)
                .stream()
                .filter(detalle -> esSalidaEnRango(detalle, inicio, fin))
                .mapToLong(DetalleMovimiento::getCantidad)
                .sum();

        return BigDecimal.valueOf(unidadesSalida)
                .divide(DIAS_DEL_PERIODO, MathContext.DECIMAL128);
    }

    private boolean esSalidaEnRango(
            DetalleMovimiento detalle,
            LocalDateTime inicio,
            LocalDateTime fin) {
        return detalle.getMovimiento().getTipo() == TipoMovimiento.SALIDA
                && !detalle.getMovimiento().getFecha().isBefore(inicio)
                && detalle.getMovimiento().getFecha().isBefore(fin);
    }

    private Long seleccionarBodegaDestino(
            List<StockBodegaResponse> stockPorBodega) {
        return stockPorBodega.stream()
                .min(Comparator
                        .comparing(StockBodegaResponse::getStock)
                        .thenComparing(StockBodegaResponse::getBodegaId))
                .map(StockBodegaResponse::getBodegaId)
                .orElse(null);
    }

    private BigDecimal presentar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
