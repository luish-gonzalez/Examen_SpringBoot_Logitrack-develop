package com.logitrack.iq;

import static com.logitrack.support.LogiTrackTestData.linea;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
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
class ProductoRiesgoIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;

    private Usuario admin;
    private LocalDateTime ahora;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-riesgo");
        ahora = LocalDateTime.now(clock);
    }

    @Test
    void stockIgualAlPuntoDeReordenNoApareceEnRiesgo() throws Exception {
        Bodega bodega = datos.crearBodega("Bodega igualdad", admin);
        Producto producto = datos.crearProducto("Producto igualdad");
        datos.asignarProveedorPrincipal(producto, 10);
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(10), admin, null, bodega,
                linea(producto, 90));
        datos.crearMovimiento(TipoMovimiento.SALIDA, ahora.minusDays(5), admin, bodega, null,
                linea(producto, 60));

        assertFalse(contieneProducto(consultarRiesgos(), producto.getId()));
    }

    @Test
    void productoSinProveedorPrincipalQuedaExcluido() throws Exception {
        Bodega bodega = datos.crearBodega("Bodega sin proveedor", admin);
        Producto producto = datos.crearProducto("Producto sin proveedor");
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(10), admin, null, bodega,
                linea(producto, 40));
        datos.crearMovimiento(TipoMovimiento.SALIDA, ahora.minusDays(5), admin, bodega, null,
                linea(producto, 30));

        assertFalse(contieneProducto(consultarRiesgos(), producto.getId()));
    }

    @Test
    void empateDeStockSugiereLaBodegaDeMenorId() throws Exception {
        Bodega primera = datos.crearBodega("Primera", admin);
        Bodega segunda = datos.crearBodega("Segunda", admin);
        Producto producto = datos.crearProducto("Producto con empate");
        datos.asignarProveedorPrincipal(producto, 20);
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(10), admin, null, primera,
                linea(producto, 20));
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(10), admin, null, segunda,
                linea(producto, 20));
        datos.crearMovimiento(TipoMovimiento.SALIDA, ahora.minusDays(5), admin, primera, null,
                linea(producto, 15));
        datos.crearMovimiento(TipoMovimiento.SALIDA, ahora.minusDays(4), admin, segunda, null,
                linea(producto, 15));

        JsonNode riesgo = buscarProducto(consultarRiesgos(), producto.getId());
        assertNotNull(riesgo);
        assertEquals(primera.getId().longValue(), riesgo.path("bodegaDestinoId").asLong());
    }

    private JsonNode consultarRiesgos() throws Exception {
        MvcResult resultado = mockMvc.perform(
                        get("/productos/riesgo")
                                .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private boolean contieneProducto(JsonNode riesgos, Long productoId) {
        return buscarProducto(riesgos, productoId) != null;
    }

    private JsonNode buscarProducto(JsonNode riesgos, Long productoId) {
        for (JsonNode riesgo : riesgos) {
            if (riesgo.path("productoId").asLong() == productoId) {
                return riesgo;
            }
        }
        return null;
    }
}
