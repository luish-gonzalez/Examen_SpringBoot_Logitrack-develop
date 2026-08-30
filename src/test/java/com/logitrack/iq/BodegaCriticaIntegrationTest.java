package com.logitrack.iq;

import static com.logitrack.support.LogiTrackTestData.linea;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class BodegaCriticaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;
    @Autowired BodegaRepository bodegaRepository;

    private Usuario admin;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-bodegas-criticas");
    }

    @Test
    void soloIncluyeOcupacionIgualOMayorAlNoventaPorCiento() throws Exception {
        Bodega menor = crearBodega("Menor a noventa", 1_000);
        Bodega exacta = crearBodega("Exactamente noventa", 100);
        Bodega mayor = crearBodega("Mayor a noventa", 100);
        Producto productoMenor = datos.crearProducto("Producto menor");
        Producto productoExacto = datos.crearProducto("Producto exacto");
        Producto productoMayor = datos.crearProducto("Producto mayor");
        LocalDateTime ahora = LocalDateTime.now(clock);

        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(1), admin, null, menor,
                linea(productoMenor, 899));
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(1), admin, null, exacta,
                linea(productoExacto, 90));
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(1), admin, null, mayor,
                linea(productoMayor, 91));

        MvcResult resultado = mockMvc.perform(get("/bodegas/criticas")
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode respuesta = objectMapper.readTree(resultado.getResponse().getContentAsString());

        assertFalse(contieneBodega(respuesta, menor.getId()));
        assertTrue(contieneBodega(respuesta, exacta.getId()));
        assertTrue(contieneBodega(respuesta, mayor.getId()));
    }

    private Bodega crearBodega(String nombre, int capacidad) {
        Bodega bodega = new Bodega();
        bodega.setNombre(nombre);
        bodega.setUbicacion("Bogotá");
        bodega.setCapacidad(capacidad);
        bodega.setEncargado(admin);
        return bodegaRepository.saveAndFlush(bodega);
    }

    private boolean contieneBodega(JsonNode respuesta, Long bodegaId) {
        for (JsonNode bodega : respuesta) {
            if (bodega.path("bodegaId").asLong() == bodegaId) {
                return true;
            }
        }
        return false;
    }
}
