package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIqIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void exponeRutasIqYBearerJwt() throws Exception {
        JsonNode api = objectMapper.readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        for (String ruta : List.of("/kpis", "/productos/{id}/stock", "/productos/riesgo",
                "/bodegas/criticas", "/proveedores", "/ordenes", "/ordenes/{id}",
                "/ordenes/{id}/pdf", "/ordenes/{id}/estado", "/panel/resumen")) {
            assertTrue(api.path("paths").has(ruta), ruta);
        }
        JsonNode bearer = api.path("components").path("securitySchemes").path("bearerAuth");
        assertEquals("http", bearer.path("type").asText());
        assertEquals("bearer", bearer.path("scheme").asText());
        assertEquals("JWT", bearer.path("bearerFormat").asText());
    }
}
