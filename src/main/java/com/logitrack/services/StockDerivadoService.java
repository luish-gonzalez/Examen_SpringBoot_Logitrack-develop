package com.logitrack.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logitrack.dto.ProductoStockResponse;
import com.logitrack.dto.StockBodegaResponse;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.DetalleMovimiento;
import com.logitrack.entities.Movimiento;
import com.logitrack.entities.Producto;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.DetalleMovimientoRepository;
import com.logitrack.repositories.ProductoRepository;

@Service
@Transactional(readOnly = true)
public class StockDerivadoService {

    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;

    public StockDerivadoService(
            ProductoRepository productoRepository,
            BodegaRepository bodegaRepository,
            DetalleMovimientoRepository detalleMovimientoRepository) {
        this.productoRepository = productoRepository;
        this.bodegaRepository = bodegaRepository;
        this.detalleMovimientoRepository = detalleMovimientoRepository;
    }

    public ProductoStockResponse obtenerStockProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + productoId));

        List<Bodega> bodegas = bodegaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        Map<Long, Long> saldos = calcularSaldos(productoId, bodegas);

        List<StockBodegaResponse> stockPorBodega = bodegas.stream()
                .map(bodega -> new StockBodegaResponse(
                        bodega.getId(),
                        bodega.getNombre(),
                        saldos.get(bodega.getId())))
                .toList();

        long stockTotal = saldos.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return new ProductoStockResponse(
                producto.getId(),
                producto.getNombre(),
                stockTotal,
                stockPorBodega);
    }

    public long obtenerStockEnBodega(Long productoId, Long bodegaId) {
        Map<Long, Long> saldos = calcularSaldos(
                productoId,
                bodegaRepository.findAll());
        return saldos.getOrDefault(bodegaId, 0L);
    }

    private Map<Long, Long> calcularSaldos(
            Long productoId,
            List<Bodega> bodegas) {
        Map<Long, Long> saldos = new LinkedHashMap<>();
        bodegas.forEach(bodega -> saldos.put(bodega.getId(), 0L));

        for (DetalleMovimiento detalle
                : detalleMovimientoRepository.findByProductoId(productoId)) {
            aplicarEfecto(saldos, detalle.getMovimiento(), detalle.getCantidad());
        }

        return saldos;
    }

    private void aplicarEfecto(
            Map<Long, Long> saldos,
            Movimiento movimiento,
            long cantidad) {
        switch (movimiento.getTipo()) {
            case ENTRADA ->
                    ajustar(saldos, movimiento.getBodegaDestino(), cantidad);
            case SALIDA ->
                    ajustar(saldos, movimiento.getBodegaOrigen(), -cantidad);
            case TRANSFERENCIA -> {
                ajustar(saldos, movimiento.getBodegaOrigen(), -cantidad);
                ajustar(saldos, movimiento.getBodegaDestino(), cantidad);
            }
        }
    }

    private void ajustar(
            Map<Long, Long> saldos,
            Bodega bodega,
            long diferencia) {
        if (bodega != null) {
            saldos.merge(bodega.getId(), diferencia, Long::sum);
        }
    }
}
