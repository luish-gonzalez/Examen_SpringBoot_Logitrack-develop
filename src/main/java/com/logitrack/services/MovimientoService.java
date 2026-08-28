package com.logitrack.services;

import com.logitrack.dto.DetalleMovimientoRequest;
import com.logitrack.dto.MovimientoRequest;
import com.logitrack.dto.MovimientoResponse;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.DetalleMovimiento;
import com.logitrack.entities.Inventario;
import com.logitrack.entities.Movimiento;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.enums.TipoOperacion;
import com.logitrack.exceptions.BusinessException;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.DetalleMovimientoRepository;
import com.logitrack.repositories.InventarioRepository;
import com.logitrack.repositories.MovimientoRepository;
import com.logitrack.repositories.ProductoRepository;
import com.logitrack.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class MovimientoService {

        private final MovimientoRepository movimientoRepository;
        private final DetalleMovimientoRepository detalleMovimientoRepository;
        private final InventarioRepository inventarioRepository;
        private final ProductoRepository productoRepository;
        private final BodegaRepository bodegaRepository;
        private final UsuarioRepository usuarioRepository;
        private final AuditoriaService auditoriaService;

        public MovimientoService(
                        MovimientoRepository movimientoRepository,
                        DetalleMovimientoRepository detalleMovimientoRepository,
                        InventarioRepository inventarioRepository,
                        ProductoRepository productoRepository,
                        BodegaRepository bodegaRepository,
                        UsuarioRepository usuarioRepository,
                        AuditoriaService auditoriaService) {

                this.movimientoRepository = movimientoRepository;
                this.detalleMovimientoRepository = detalleMovimientoRepository;
                this.inventarioRepository = inventarioRepository;
                this.productoRepository = productoRepository;
                this.bodegaRepository = bodegaRepository;
                this.usuarioRepository = usuarioRepository;
                this.auditoriaService = auditoriaService;
        }

        public List<MovimientoResponse> listarTodos() {

                return movimientoRepository.findAll()
                                .stream()
                                .map(this::convertirAResponse)
                                .collect(Collectors.toList());
        }

        public MovimientoResponse buscarPorId(Long id) {

                Movimiento movimiento = movimientoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Movimiento no encontrado con id: " + id));

                return convertirAResponse(movimiento);
        }

        public List<MovimientoResponse> buscarPorTipo(TipoMovimiento tipo) {

                return movimientoRepository.findByTipo(tipo)
                                .stream()
                                .map(this::convertirAResponse)
                                .collect(Collectors.toList());
        }

        public List<MovimientoResponse> buscarPorUsuario(Long usuarioId) {

                Usuario usuario = usuarioRepository.findById(usuarioId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado con id: " + usuarioId));

                return movimientoRepository.findByUsuarioResponsable(usuario)
                                .stream()
                                .map(this::convertirAResponse)
                                .collect(Collectors.toList());
        }

        public List<MovimientoResponse> buscarPorBodegaOrigen(Long bodegaId) {

                return movimientoRepository.findByBodegaOrigenId(bodegaId)
                                .stream()
                                .map(this::convertirAResponse)
                                .collect(Collectors.toList());
        }

        public List<MovimientoResponse> buscarPorBodegaDestino(Long bodegaId) {

                return movimientoRepository.findByBodegaDestinoId(bodegaId)
                                .stream()
                                .map(this::convertirAResponse)
                                .collect(Collectors.toList());
        }

        private MovimientoResponse convertirAResponse(Movimiento movimiento) {

                List<DetalleMovimientoRequest> detalles = movimiento.getDetalles()
                                .stream()
                                .map(detalle -> new DetalleMovimientoRequest(
                                                detalle.getProducto().getId(),
                                                detalle.getCantidad()))
                                .collect(Collectors.toList());

                                return new MovimientoResponse(
                                        movimiento.getId(),
                                        movimiento.getFecha(),
                                        movimiento.getTipo(),
                                        movimiento.getUsuarioResponsable().getId(),
                                        movimiento.getUsuarioResponsable().getNombre(),
                                        movimiento.getBodegaOrigen() != null
                                                ? movimiento.getBodegaOrigen().getId()
                                                : null,
                                        movimiento.getBodegaDestino() != null
                                                ? movimiento.getBodegaDestino().getId()
                                                : null,
                                        detalles
                                );

        }

        @Transactional
        public MovimientoResponse registrarMovimiento(MovimientoRequest request) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                String username = authentication.getName();

                Usuario usuario = usuarioRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));

                Movimiento movimiento = new Movimiento();
                movimiento.setFecha(LocalDateTime.now());
                movimiento.setTipo(request.getTipo());
                movimiento.setUsuarioResponsable(usuario);

                Bodega bodegaOrigen = null;
                Bodega bodegaDestino = null;

                switch (request.getTipo()) {

                        case ENTRADA:

                                if (request.getBodegaDestinoId() == null) {
                                        throw new BusinessException(
                                                        "Una entrada requiere una bodega destino.");
                                }

                                bodegaDestino = bodegaRepository
                                                .findById(request.getBodegaDestinoId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Bodega destino no encontrada."));

                                movimiento.setBodegaDestino(bodegaDestino);
                                break;

                        case SALIDA:

                                if (request.getBodegaOrigenId() == null) {
                                        throw new BusinessException(
                                                        "Una salida requiere una bodega origen.");
                                }

                                bodegaOrigen = bodegaRepository
                                                .findById(request.getBodegaOrigenId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Bodega origen no encontrada."));

                                movimiento.setBodegaOrigen(bodegaOrigen);
                                break;

                        case TRANSFERENCIA:

                                if (request.getBodegaOrigenId() == null
                                                || request.getBodegaDestinoId() == null) {

                                        throw new BusinessException(
                                                        "Una transferencia requiere ambas bodegas.");
                                }

                                if (request.getBodegaOrigenId()
                                                .equals(request.getBodegaDestinoId())) {

                                        throw new BusinessException(
                                                        "La bodega origen y destino no pueden ser iguales.");
                                }

                                bodegaOrigen = bodegaRepository
                                                .findById(request.getBodegaOrigenId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Bodega origen no encontrada."));

                                bodegaDestino = bodegaRepository
                                                .findById(request.getBodegaDestinoId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Bodega destino no encontrada."));

                                movimiento.setBodegaOrigen(bodegaOrigen);
                                movimiento.setBodegaDestino(bodegaDestino);
                                break;
                }

                movimiento = movimientoRepository.save(movimiento);

                List<DetalleMovimiento> detalles = new ArrayList<>();

                for (DetalleMovimientoRequest detalleRequest : request.getDetalles()) {

                        if (detalleRequest.getCantidad() <= 0) {
                                throw new BusinessException(
                                                "La cantidad debe ser mayor que cero.");
                        }

                        Producto producto = productoRepository
                                        .findById(detalleRequest.getProductoId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Producto no encontrado."));

                        DetalleMovimiento detalle = new DetalleMovimiento();
                        detalle.setMovimiento(movimiento);
                        detalle.setProducto(producto);
                        detalle.setCantidad(detalleRequest.getCantidad());

                        actualizarInventario(
                                        request.getTipo(),
                                        producto,
                                        bodegaOrigen,
                                        bodegaDestino,
                                        detalleRequest.getCantidad());

                        detalles.add(detalle);
                }
                detalleMovimientoRepository.saveAll(detalles);

                auditoriaService.registrar(
                                TipoOperacion.INSERT,
                                usuario.getUsername(),
                                "Movimiento",
                                movimiento.getId(),
                                null,
                                "Movimiento " + movimiento.getTipo() + " registrado");

                return convertirAResponse(movimiento);
        }

        private void actualizarInventario(
                        TipoMovimiento tipo,
                        Producto producto,
                        Bodega bodegaOrigen,
                        Bodega bodegaDestino,
                        Integer cantidad) {

                switch (tipo) {

                        case ENTRADA:
                                aumentarStock(producto, bodegaDestino, cantidad);
                                break;

                        case SALIDA:
                                disminuirStock(producto, bodegaOrigen, cantidad);
                                break;

                        case TRANSFERENCIA:
                                disminuirStock(producto, bodegaOrigen, cantidad);
                                aumentarStock(producto, bodegaDestino, cantidad);
                                break;

                        default:
                                throw new BusinessException("Tipo de movimiento no válido.");
                }
        }

        private void aumentarStock(
                        Producto producto,
                        Bodega bodega,
                        Integer cantidad) {

                Inventario inventario = inventarioRepository
                                .findByBodegaIdAndProductoId(
                                                bodega.getId(),
                                                producto.getId())
                                .orElse(null);

                if (inventario == null) {

                        inventario = new Inventario();
                        inventario.setBodega(bodega);
                        inventario.setProducto(producto);
                        inventario.setStock(cantidad);

                } else {

                        inventario.setStock(
                                        inventario.getStock() + cantidad);
                }

                inventarioRepository.save(inventario);
        }

        private void disminuirStock(
                        Producto producto,
                        Bodega bodega,
                        Integer cantidad) {

                Inventario inventario = inventarioRepository
                                .findByBodegaIdAndProductoId(
                                                bodega.getId(),
                                                producto.getId())
                                .orElseThrow(() -> new BusinessException(
                                                "No existe inventario para el producto en la bodega."));

                if (inventario.getStock() < cantidad) {

                        throw new BusinessException(
                                        "Stock insuficiente para realizar el movimiento.");
                }

                inventario.setStock(
                                inventario.getStock() - cantidad);

                inventarioRepository.save(inventario);
        }

        @Transactional
        public void eliminar(Long id) {

                Movimiento movimiento = movimientoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Movimiento no encontrado con id: " + id));

                movimientoRepository.delete(movimiento);
        }

        public List<MovimientoResponse> buscarPorRangoFechas(
                        LocalDateTime fechaInicio,
                        LocalDateTime fechaFin) {

                return movimientoRepository
                                .findByFechaBetween(fechaInicio, fechaFin)
                                .stream()
                                .map(this::convertirAResponse)
                                .toList();
        }

}