package com.logitrack.services;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.dto.AccionSugeridaRequest;
import com.logitrack.dto.AccionSugeridaResponse;
import com.logitrack.dto.AlertaResumenRequest;
import com.logitrack.dto.AlertaResumenResponse;
import com.logitrack.dto.ResumenPanelRequest;
import com.logitrack.dto.ResumenPanelResponse;
import com.logitrack.entities.ResumenPanel;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.TipoOperacion;
import com.logitrack.exceptions.BusinessException;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.OrdenCompraRepository;
import com.logitrack.repositories.ProductoRepository;
import com.logitrack.repositories.ResumenPanelRepository;
import com.logitrack.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class ResumenPanelService {

    private final ResumenPanelRepository resumenPanelRepository;
    private final ProductoRepository productoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ResumenPanelService(
                    ResumenPanelRepository resumenPanelRepository,
                    ProductoRepository productoRepository,
                    OrdenCompraRepository ordenCompraRepository,
                    BodegaRepository bodegaRepository,
                    UsuarioRepository usuarioRepository,
                    AuditoriaService auditoriaService,
                    ObjectMapper objectMapper,
                    Clock clock) {
        this.resumenPanelRepository = resumenPanelRepository;
        this.productoRepository = productoRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.bodegaRepository = bodegaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public ResumenPanelResponse publicar(ResumenPanelRequest request) {
        validarFecha(request);
        validarEnlaces(request);

        Usuario autor = obtenerUsuarioAutenticado();
        ResumenPanelResponse respuesta = convertirAResponse(request);
        String contenidoJson = serializar(respuesta);

        ResumenPanel resumen = resumenPanelRepository.findByFecha(request.getFecha())
                        .orElseGet(ResumenPanel::new);
        boolean esReemplazo = resumen.getId() != null;
        String contenidoAnterior = resumen.getContenidoJson();

        resumen.setFecha(request.getFecha());
        resumen.setContenidoJson(contenidoJson);
        resumen.setAutor(autor);
        ResumenPanel guardado = resumenPanelRepository.save(resumen);

        auditoriaService.registrar(
                        esReemplazo ? TipoOperacion.UPDATE : TipoOperacion.INSERT,
                        autor.getUsername(),
                        "ResumenPanel",
                        guardado.getId(),
                        contenidoAnterior,
                        contenidoJson);
        return respuesta;
    }

    public ResumenPanelResponse obtenerUltimo() {
        ResumenPanel resumen = resumenPanelRepository.findTopByOrderByFechaDesc()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "No existe un resumen de panel válido."));
        try {
            return objectMapper.readValue(resumen.getContenidoJson(), ResumenPanelResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("El resumen almacenado no tiene un formato válido.", exception);
        }
    }

    private void validarFecha(ResumenPanelRequest request) {
        LocalDate fechaActual = LocalDate.now(clock);
        if (!fechaActual.equals(request.getFecha())) {
            throw new BusinessException("La fecha del resumen debe corresponder al día actual en America/Bogota.");
        }
    }

    private void validarEnlaces(ResumenPanelRequest request) {
        request.getAlertas().forEach(this::validarAlerta);
        request.getAccionesSugeridas().forEach(this::validarAccion);
    }

    private void validarAlerta(AlertaResumenRequest alerta) {
        validarProducto(alerta.getProductoId());
        validarOrden(alerta.getOrdenId());
        validarBodega(alerta.getBodegaId());
    }

    private void validarAccion(AccionSugeridaRequest accion) {
        validarProducto(accion.getProductoId());
        validarOrden(accion.getOrdenId());
        validarBodega(accion.getBodegaId());
    }

    private void validarProducto(Long productoId) {
        if (productoId != null && !productoRepository.existsById(productoId)) {
            throw new BusinessException("Producto informado en el resumen no existe.");
        }
    }

    private void validarOrden(Long ordenId) {
        if (ordenId != null && !ordenCompraRepository.existsById(ordenId)) {
            throw new BusinessException("Orden informada en el resumen no existe.");
        }
    }

    private void validarBodega(Long bodegaId) {
        if (bodegaId != null && !bodegaRepository.existsById(bodegaId)) {
            throw new BusinessException("Bodega informada en el resumen no existe.");
        }
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("No existe un usuario autenticado para publicar el resumen.");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Usuario autenticado no encontrado."));
    }

    private String serializar(ResumenPanelResponse respuesta) {
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("No fue posible serializar el resumen del panel.");
        }
    }

    private ResumenPanelResponse convertirAResponse(ResumenPanelRequest request) {
        return new ResumenPanelResponse(
                        request.getFecha(),
                        request.getNarrativa(),
                        request.getAlertas().stream().map(alerta -> new AlertaResumenResponse(
                                        alerta.getSeveridad(), alerta.getTitulo(), alerta.getDetalle(),
                                        alerta.getProductoId(), alerta.getOrdenId(), alerta.getBodegaId())).toList(),
                        request.getAccionesSugeridas().stream().map(accion -> new AccionSugeridaResponse(
                                        accion.getTipo(), accion.getDescripcion(), accion.getOrdenId(),
                                        accion.getProductoId(), accion.getBodegaId())).toList());
    }
}
