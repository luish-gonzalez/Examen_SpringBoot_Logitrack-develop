package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.EstadoOrdenCompra;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.enums.TipoOperacion;
import com.logitrack.repositories.AuditoriaRepository;
import com.logitrack.repositories.DetalleMovimientoRepository;
import com.logitrack.repositories.MovimientoRepository;
import com.logitrack.repositories.OrdenCompraRepository;
import com.logitrack.services.MovimientoService;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class OrdenCompraRecepcionAtomicidadAuditoriaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired LogiTrackTestData datos;
    @Autowired OrdenCompraRepository ordenCompraRepository;
    @Autowired MovimientoRepository movimientoRepository;
    @Autowired DetalleMovimientoRepository detalleMovimientoRepository;
    @Autowired AuditoriaRepository auditoriaRepository;
    @SpyBean MovimientoService movimientoService;

    private Usuario admin;
    private Producto producto;
    private Bodega bodega;
    private long proveedorId;

    @BeforeEach
    void preparar() {
        reset(movimientoService);
        admin = datos.crearAdmin("admin-atomicidad-recepcion");
        bodega = datos.crearBodega("Destino recepción", admin);
        producto = datos.crearProducto("Producto recepción");
        proveedorId = datos.asignarProveedorPrincipal(producto, 10);
    }

    @Test
    void falloDuranteEntradaAutomaticaRevierteEstadoYNoDejaPersistenciaParcial() throws Exception {
        long ordenId = crearYaprobarOrden();
        long movimientosAntes = movimientoRepository.count();
        long detallesAntes = detalleMovimientoRepository.count();
        doThrow(new IllegalStateException("Fallo controlado de recepción"))
                .when(movimientoService)
                .registrarEntradaAutomatica(any(Producto.class), anyInt(), any(Bodega.class), any(Usuario.class));

        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "RECIBIDA"))))
                .andExpect(status().isInternalServerError());

        assertEquals(EstadoOrdenCompra.APROBADA,
                ordenCompraRepository.findById(ordenId).orElseThrow().getEstado());
        assertEquals(movimientosAntes, movimientoRepository.count());
        assertEquals(detallesAntes, detalleMovimientoRepository.count());
    }

    @Test
    void recepcionExitosaCreaEntradaYCreaAuditoriaDeMovimientoYOrden() throws Exception {
        long ordenId = crearYaprobarOrden();

        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "RECIBIDA"))))
                .andExpect(status().isOk());

        assertEquals(EstadoOrdenCompra.RECIBIDA,
                ordenCompraRepository.findById(ordenId).orElseThrow().getEstado());
        assertTrue(movimientoRepository.findAll().stream().anyMatch(movimiento ->
                movimiento.getTipo() == TipoMovimiento.ENTRADA
                        && bodega.getId().equals(movimiento.getBodegaDestino().getId())
                        && movimiento.getDetalles().stream().anyMatch(detalle ->
                                producto.getId().equals(detalle.getProducto().getId())
                                        && detalle.getCantidad() == 10)));
        assertTrue(auditoriaRepository.findAll().stream().anyMatch(auditoria ->
                "Movimiento".equals(auditoria.getEntidadAfectada())
                        && auditoria.getTipoOperacion() == TipoOperacion.INSERT));
        assertTrue(auditoriaRepository.findAll().stream().anyMatch(auditoria ->
                "OrdenCompra".equals(auditoria.getEntidadAfectada())
                        && auditoria.getTipoOperacion() == TipoOperacion.UPDATE
                        && "Estado RECIBIDA".equals(auditoria.getValoresNuevos())));
    }

    private long crearYaprobarOrden() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/ordenes")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productoId", producto.getId(), "proveedorId", proveedorId,
                                "bodegaDestinoId", bodega.getId(), "cantidad", 10,
                                "precioUnitario", 4500.00))))
                .andExpect(status().isCreated())
                .andReturn();
        long ordenId = objectMapper.readTree(resultado.getResponse().getContentAsString()).path("id").asLong();
        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "APROBADA"))))
                .andExpect(status().isOk());
        return ordenId;
    }
}
