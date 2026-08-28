package com.logitrack.iq;

import static com.logitrack.support.LogiTrackTestData.linea;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;

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
class ProductoStockIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;

    @Test
    void calculaStockDesdeTodosLosDetallesYTiposSinUsarInventario() throws Exception {
        Usuario admin = datos.crearAdmin("admin-stock");
        Bodega central = datos.crearBodega("Central", admin);
        Bodega norte = datos.crearBodega("Norte", admin);
        Producto principal = datos.crearProducto("Producto principal");
        Producto segundoDetalle = datos.crearProducto("Segundo detalle");
        LocalDateTime ahora = LocalDateTime.now(clock);

        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(3), admin, null, central,
                linea(principal, 10), linea(segundoDetalle, 7));
        datos.crearMovimiento(TipoMovimiento.TRANSFERENCIA, ahora.minusDays(2), admin, central, norte,
                linea(principal, 4));
        datos.crearMovimiento(TipoMovimiento.SALIDA, ahora.minusDays(1), admin, central, null,
                linea(principal, 2));
        datos.crearInventario(principal, central, 999);
        datos.crearInventario(principal, norte, 0);

        JsonNode respuesta = consultarStock(principal, admin);
        assertEquals(8, respuesta.path("stockTotal").asInt());
        assertEquals(4, stockEnBodega(respuesta, central.getId()));
        assertEquals(4, stockEnBodega(respuesta, norte.getId()));
        assertEquals(7, consultarStock(segundoDetalle, admin).path("stockTotal").asInt());
    }

    private JsonNode consultarStock(Producto producto, Usuario admin) throws Exception {
        MvcResult resultado = mockMvc.perform(
                        get("/productos/{id}/stock", producto.getId())
                                .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private int stockEnBodega(JsonNode respuesta, Long bodegaId) {
        for (JsonNode item : respuesta.path("stockPorBodega")) {
            if (item.path("bodegaId").asLong() == bodegaId) {
                return item.path("stock").asInt();
            }
        }
        throw new AssertionError("No se encontró la bodega " + bodegaId);
    }
}
