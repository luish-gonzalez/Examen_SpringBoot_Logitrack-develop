package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.Movimiento;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.repositories.MovimientoRepository;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class OrdenCompraIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired LogiTrackTestData datos;
    @Autowired MovimientoRepository movimientoRepository;

    private Usuario admin;
    private Producto producto;
    private Bodega bodega;
    private long proveedorId;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-orden");
        bodega = datos.crearBodega("Destino orden", admin);
        producto = datos.crearProducto("Producto orden");
        proveedorId = datos.asignarProveedorPrincipal(producto, 10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void cantidadNoPositivaRespondeBadRequest(int cantidad) throws Exception {
        mockMvc.perform(post("/ordenes")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ordenJson(cantidad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ordenCanceladaNoPuedeVolverAprobada() throws Exception {
        long ordenId = crearOrdenBorrador();

        cambiarEstado(ordenId, "CANCELADA", 200);
        cambiarEstado(ordenId, "APROBADA", 400);
    }

    @Test
    void recibirOrdenAprobadaCreaMovimientoEntrada() throws Exception {
        long ordenId = crearOrdenBorrador();
        cambiarEstado(ordenId, "APROBADA", 200);
        long movimientosAntes = movimientoRepository.count();

        cambiarEstado(ordenId, "RECIBIDA", 200);

        assertEquals(movimientosAntes + 1, movimientoRepository.count());
        Movimiento entrada = movimientoRepository.findAll().stream()
                .filter(m -> m.getTipo() == TipoMovimiento.ENTRADA)
                .filter(m -> bodega.getId().equals(m.getBodegaDestino().getId()))
                .filter(m -> m.getDetalles().stream().anyMatch(d ->
                        producto.getId().equals(d.getProducto().getId())
                                && d.getCantidad() == 10))
                .findFirst()
                .orElseThrow();
        assertTrue(entrada.getBodegaOrigen() == null);
    }

    private long crearOrdenBorrador() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/ordenes")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ordenJson(10)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .path("id")
                .asLong();
    }

    private void cambiarEstado(long ordenId, String estado, int statusEsperado) throws Exception {
        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", estado))))
                .andExpect(status().is(statusEsperado));
    }

    private String ordenJson(int cantidad) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "productoId", producto.getId(),
                "proveedorId", proveedorId,
                "bodegaDestinoId", bodega.getId(),
                "cantidad", cantidad,
                "precioUnitario", 4500.00));
    }
}
