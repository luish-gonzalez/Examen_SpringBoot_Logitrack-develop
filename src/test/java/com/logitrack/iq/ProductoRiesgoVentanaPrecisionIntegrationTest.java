package com.logitrack.iq;

import static com.logitrack.support.LogiTrackTestData.linea;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class ProductoRiesgoVentanaPrecisionIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;

    private Usuario admin;
    private Bodega bodega;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-precision-riesgo");
        bodega = datos.crearBodega("Bodega precisión", admin);
    }

    @Test
    void salidaUnitariaCalculaUnTreintavoSinErrorNiRedondeoIntermedio() throws Exception {
        Producto producto = datos.crearProducto("Producto consumo unitario");
        datos.asignarProveedorPrincipal(producto, 10);
        LocalDateTime ahora = LocalDateTime.now(clock);
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(10), admin, null, bodega,
                linea(producto, 1));
        datos.crearMovimiento(TipoMovimiento.SALIDA, ahora, admin, bodega, null,
                linea(producto, 1));

        JsonNode riesgo = buscarProducto(consultarRiesgos(), producto.getId());

        assertEquals(new BigDecimal("0.03"), decimal(riesgo, "consumoDiarioPromedio"));
        // 1/30 × 10 × 1.5 = 0.50; redondear primero 1/30 a 0.03 daría 0.45.
        assertEquals(new BigDecimal("0.50"), decimal(riesgo, "puntoReorden"));
    }

    @Test
    void ventanaIncluyeHoyYHaceVeintinueveDiasPeroExcluyeHaceTreintaDias() throws Exception {
        Producto producto = datos.crearProducto("Producto límites ventana");
        datos.asignarProveedorPrincipal(producto, 10);
        LocalDate hoy = LocalDate.now(clock);

        datos.crearMovimiento(TipoMovimiento.ENTRADA, hoy.minusDays(20).atTime(8, 0), admin,
                null, bodega, linea(producto, 1_860));
        datos.crearMovimiento(TipoMovimiento.SALIDA, hoy.atTime(9, 0), admin, bodega, null,
                linea(producto, 30));
        datos.crearMovimiento(TipoMovimiento.SALIDA, hoy.minusDays(29).atTime(9, 0), admin,
                bodega, null, linea(producto, 30));
        datos.crearMovimiento(TipoMovimiento.SALIDA, hoy.minusDays(30).atTime(9, 0), admin,
                bodega, null, linea(producto, 900));
        datos.crearMovimiento(TipoMovimiento.SALIDA, hoy.minusDays(31).atTime(9, 0), admin,
                bodega, null, linea(producto, 900));

        JsonNode riesgo = buscarProducto(consultarRiesgos(), producto.getId());

        assertEquals(0, new BigDecimal("2.00").compareTo(decimal(riesgo, "consumoDiarioPromedio")));
        assertEquals(0, new BigDecimal("30.00").compareTo(decimal(riesgo, "puntoReorden")));
    }

    private JsonNode consultarRiesgos() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/productos/riesgo")
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private JsonNode buscarProducto(JsonNode riesgos, Long productoId) {
        for (JsonNode riesgo : riesgos) {
            if (riesgo.path("productoId").asLong() == productoId) {
                return riesgo;
            }
        }
        throw new AssertionError("El producto esperado debe aparecer en riesgo.");
    }

    private BigDecimal decimal(JsonNode nodo, String campo) {
        return nodo.path(campo).decimalValue();
    }
}
