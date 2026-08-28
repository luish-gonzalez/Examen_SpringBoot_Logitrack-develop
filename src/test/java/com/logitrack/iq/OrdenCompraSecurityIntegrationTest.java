package com.logitrack.iq;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class OrdenCompraSecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired LogiTrackTestData datos;

    private Usuario admin;
    private Usuario agente;
    private Producto producto;
    private Bodega bodega;
    private long proveedorId;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-seguridad");
        agente = datos.crearEmpleado("agente-test");
        bodega = datos.crearBodega("Bodega seguridad", admin);
        producto = datos.crearProducto("Producto seguridad");
        proveedorId = datos.asignarProveedorPrincipal(producto, 10);
    }

    @Test
    void agenteNoPuedeAprobarOrden() throws Exception {
        long ordenId = crearOrdenComoAdmin();

        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .with(user("agente-test").roles("AGENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "APROBADA"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void agenteNoPuedeRegistrarMovimientoManual() throws Exception {
        String movimiento = objectMapper.writeValueAsString(Map.of(
                "tipo", "ENTRADA",
                "bodegaDestinoId", bodega.getId(),
                "detalles", List.of(Map.of(
                        "productoId", producto.getId(),
                        "cantidad", 1))));

        mockMvc.perform(post("/api/movimientos")
                        .with(user(agente.getUsername()).roles("AGENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimiento))
                .andExpect(status().isForbidden());
    }

    @Test
    void sesionAusenteRespondeUnauthorized() throws Exception {
        mockMvc.perform(patch("/ordenes/{id}/estado", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "APROBADA"))))
                .andExpect(status().isUnauthorized());
    }

    private long crearOrdenComoAdmin() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "productoId", producto.getId(),
                "proveedorId", proveedorId,
                "bodegaDestinoId", bodega.getId(),
                "cantidad", 10,
                "precioUnitario", 4500.00));
        MvcResult resultado = mockMvc.perform(post("/ordenes")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .path("id")
                .asLong();
    }
}
