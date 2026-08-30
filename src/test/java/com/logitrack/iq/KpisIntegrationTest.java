package com.logitrack.iq;

import static com.logitrack.support.LogiTrackTestData.linea;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class KpisIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;
    @Autowired BodegaRepository bodegaRepository;

    private Usuario admin;
    private Bodega bodegaExacta;
    private Bodega bodegaMenor;
    private Bodega bodegaMayor;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-kpis");
        bodegaExacta = crearBodega("Exacta", 100);
        bodegaMenor = crearBodega("Menor", 100);
        bodegaMayor = crearBodega("Mayor", 100);
    }

    @Test
    void devuelveEstructuraYMetricasCalculadasDesdeMovimientos() throws Exception {
        LocalDate hoy = LocalDate.now(clock);
        Producto productoExacto = datos.crearProducto("Producto ocupación exacta");
        Producto productoMenor = datos.crearProducto("Producto ocupación menor");
        Producto productoMayor = datos.crearProducto("Producto ocupación mayor");
        Producto productoRiesgo = datos.crearProducto("Producto en riesgo KPI");
        datos.crearProducto("Producto en quiebre KPI");
        long proveedorId = datos.asignarProveedorPrincipal(productoRiesgo, 10);

        datos.crearMovimiento(TipoMovimiento.ENTRADA, hoy.minusDays(2).atTime(9, 0), admin,
                null, bodegaExacta, linea(productoExacto, 100));
        datos.crearMovimiento(TipoMovimiento.SALIDA, hoy.minusDays(1).atTime(10, 0), admin,
                bodegaExacta, null, linea(productoExacto, 10));
        datos.crearMovimiento(TipoMovimiento.ENTRADA, hoy.minusDays(2).atTime(9, 0), admin,
                null, bodegaMenor, linea(productoMenor, 80));
        datos.crearMovimiento(TipoMovimiento.TRANSFERENCIA, hoy.minusDays(1).atTime(11, 0), admin,
                bodegaMenor, bodegaMayor, linea(productoMenor, 10));
        datos.crearMovimiento(TipoMovimiento.ENTRADA, hoy.minusDays(2).atTime(9, 0), admin,
                null, bodegaMayor, linea(productoMayor, 80));
        datos.crearMovimiento(TipoMovimiento.ENTRADA, hoy.minusDays(1).atTime(12, 0), admin,
                null, bodegaMayor, linea(productoMayor, 1));
        datos.crearMovimiento(TipoMovimiento.ENTRADA, hoy.minusDays(5).atTime(9, 0), admin,
                null, bodegaExacta, linea(productoRiesgo, 60));
        datos.crearMovimiento(TipoMovimiento.SALIDA, hoy.minusDays(4).atTime(9, 0), admin,
                bodegaExacta, null, linea(productoRiesgo, 60));

        long borrador = crearOrden(productoExacto, proveedorId, bodegaExacta, 2, "10.00");
        long aprobada = crearOrden(productoExacto, proveedorId, bodegaExacta, 3, "10.00");
        long cancelada = crearOrden(productoExacto, proveedorId, bodegaExacta, 4, "10.00");
        cambiarEstado(aprobada, "APROBADA");
        cambiarEstado(cancelada, "CANCELADA");

        MvcResult resultado = mockMvc.perform(get("/kpis")
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode cuerpo = objectMapper.readTree(resultado.getResponse().getContentAsString());

        assertTrue(cuerpo.hasNonNull("calculadoEn"));
        assertTrue(cuerpo.has("ocupacionPorBodega"));
        assertEquals(2, cuerpo.path("productosEnQuiebre").asInt());
        assertEquals(1, cuerpo.path("productosEnRiesgo").asInt());
        assertEquals(1, cuerpo.path("ordenesPorAprobar").path("cantidad").asInt());
        assertEquals(new BigDecimal("20.00"), cuerpo.path("ordenesPorAprobar")
                .path("montoTotal").decimalValue());
        assertEquals(1, cuerpo.path("movimientosAyer").path("entrada").asInt());
        assertEquals(1, cuerpo.path("movimientosAyer").path("salida").asInt());
        assertEquals(1, cuerpo.path("movimientosAyer").path("transferencia").asInt());
        assertEquals(new BigDecimal("90.00"), porcentaje(cuerpo, bodegaExacta.getId()));
        assertEquals(new BigDecimal("70.00"), porcentaje(cuerpo, bodegaMenor.getId()));
        assertEquals(new BigDecimal("91.00"), porcentaje(cuerpo, bodegaMayor.getId()));
        assertTrue(borrador > 0);
    }

    private Bodega crearBodega(String nombre, int capacidad) {
        Bodega bodega = new Bodega();
        bodega.setNombre(nombre);
        bodega.setUbicacion("Bogotá");
        bodega.setCapacidad(capacidad);
        bodega.setEncargado(admin);
        return bodegaRepository.saveAndFlush(bodega);
    }

    private long crearOrden(Producto producto, long proveedorId, Bodega destino,
            int cantidad, String precio) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/ordenes")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productoId", producto.getId(), "proveedorId", proveedorId,
                                "bodegaDestinoId", destino.getId(), "cantidad", cantidad,
                                "precioUnitario", precio))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).path("id").asLong();
    }

    private void cambiarEstado(long ordenId, String estado) throws Exception {
        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", estado))))
                .andExpect(status().isOk());
    }

    private BigDecimal porcentaje(JsonNode cuerpo, Long bodegaId) {
        for (JsonNode item : cuerpo.path("ocupacionPorBodega")) {
            if (item.path("bodegaId").asLong() == bodegaId) {
                return item.path("porcentaje").decimalValue();
            }
        }
        throw new AssertionError("Falta la ocupación de la bodega " + bodegaId);
    }
}
