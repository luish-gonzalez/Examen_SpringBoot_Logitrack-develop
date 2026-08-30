package com.logitrack.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.logitrack.dto.OrdenCompraEstadoRequest;
import com.logitrack.dto.OrdenCompraRequest;
import com.logitrack.dto.OrdenCompraResponse;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.OrdenCompra;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Proveedor;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.EstadoOrdenCompra;
import com.logitrack.enums.TipoOperacion;
import com.logitrack.exceptions.BusinessException;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.OrdenCompraRepository;
import com.logitrack.repositories.ProductoRepository;
import com.logitrack.repositories.ProveedorRepository;
import com.logitrack.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoService movimientoService;
    private final AuditoriaService auditoriaService;
    private final Clock clock;

    public OrdenCompraService(
                    OrdenCompraRepository ordenCompraRepository,
                    ProductoRepository productoRepository,
                    ProveedorRepository proveedorRepository,
                    BodegaRepository bodegaRepository,
                    UsuarioRepository usuarioRepository,
                    MovimientoService movimientoService,
                    AuditoriaService auditoriaService,
                    Clock clock) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.bodegaRepository = bodegaRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoService = movimientoService;
        this.auditoriaService = auditoriaService;
        this.clock = clock;
    }

    @Transactional
    public OrdenCompraResponse crear(OrdenCompraRequest request) {
        Producto producto = productoRepository.findById(request.productoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));
        Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado."));
        Bodega bodegaDestino = bodegaRepository.findById(request.bodegaDestinoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Bodega destino no encontrada."));
        Usuario creadoPor = obtenerUsuarioAutenticado();

        BigDecimal precioUnitario = request.precioUnitario().setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(request.cantidad()))
                        .setScale(2, RoundingMode.HALF_UP);

        OrdenCompra orden = new OrdenCompra();
        orden.setProducto(producto);
        orden.setProveedor(proveedor);
        orden.setBodegaDestino(bodegaDestino);
        orden.setCantidad(request.cantidad());
        orden.setPrecioUnitario(precioUnitario);
        orden.setTotal(total);
        orden.setFechaCreacion(LocalDateTime.now(clock));
        orden.setEstado(EstadoOrdenCompra.BORRADOR);
        orden.setCreadoPor(creadoPor);

        OrdenCompra guardada = ordenCompraRepository.save(orden);
        auditoriaService.registrar(
                        TipoOperacion.INSERT,
                        creadoPor.getUsername(),
                        "OrdenCompra",
                        guardada.getId(),
                        null,
                        "Orden de compra creada en BORRADOR");
        return convertirAResponse(guardada);
    }

    public List<OrdenCompraResponse> listar(EstadoOrdenCompra estado) {
        List<OrdenCompra> ordenes = estado == null
                        ? ordenCompraRepository.findAllByOrderByIdAsc()
                        : ordenCompraRepository.findByEstadoOrderByIdAsc(estado);
        return ordenes.stream().map(this::convertirAResponse).toList();
    }

    public OrdenCompraResponse buscarPorId(Long id) {
        return convertirAResponse(buscarEntidad(id));
    }

    @Transactional
    public OrdenCompraResponse cambiarEstado(Long id, OrdenCompraEstadoRequest request) {
        OrdenCompra orden = buscarEntidad(id);
        EstadoOrdenCompra estadoAnterior = orden.getEstado();
        EstadoOrdenCompra estadoNuevo = request.estado();

        if (!esTransicionValida(estadoAnterior, estadoNuevo)) {
            throw new BusinessException("La transición de " + estadoAnterior
                            + " a " + estadoNuevo + " no está permitida.");
        }

        Usuario usuarioResponsable = obtenerUsuarioAutenticado();
        if (estadoNuevo == EstadoOrdenCompra.RECIBIDA) {
            movimientoService.registrarEntradaAutomatica(
                            orden.getProducto(),
                            orden.getCantidad(),
                            orden.getBodegaDestino(),
                            usuarioResponsable);
        }

        orden.setEstado(estadoNuevo);
        orden.setPdf(null);
        orden.setFechaGeneracionPdf(null);
        OrdenCompra actualizada = ordenCompraRepository.save(orden);
        auditoriaService.registrar(
                        TipoOperacion.UPDATE,
                        usuarioResponsable.getUsername(),
                        "OrdenCompra",
                        actualizada.getId(),
                        "Estado " + estadoAnterior,
                        "Estado " + estadoNuevo);
        return convertirAResponse(actualizada);
    }

    private boolean esTransicionValida(
                    EstadoOrdenCompra estadoActual,
                    EstadoOrdenCompra estadoNuevo) {
        return switch (estadoActual) {
            case BORRADOR -> estadoNuevo == EstadoOrdenCompra.APROBADA
                            || estadoNuevo == EstadoOrdenCompra.CANCELADA;
            case APROBADA -> estadoNuevo == EstadoOrdenCompra.RECIBIDA
                            || estadoNuevo == EstadoOrdenCompra.CANCELADA;
            case RECIBIDA, CANCELADA -> false;
        };
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("No existe un usuario autenticado para crear la orden.");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Usuario autenticado no encontrado."));
    }

    private OrdenCompra buscarEntidad(Long id) {
        return ordenCompraRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Orden de compra no encontrada con id: " + id));
    }

    private OrdenCompraResponse convertirAResponse(OrdenCompra orden) {
        return new OrdenCompraResponse(
                        orden.getId(),
                        orden.getProducto().getId(),
                        orden.getProveedor().getId(),
                        orden.getBodegaDestino().getId(),
                        orden.getCantidad(),
                        orden.getPrecioUnitario(),
                        orden.getTotal(),
                        orden.getFechaCreacion(),
                        orden.getEstado(),
                        orden.getCreadoPor().getId());
    }
}
