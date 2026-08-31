package com.logitrack.iq;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class ResumenPanelIntegrationTest {

    private static final String NARRATIVA_VALIDA =
            "Resumen válido que debe conservarse ante cualquier publicación inválida.";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;

    private Usuario admin;
    private Producto producto;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-resumen");
        producto = datos.crearProducto("Producto resumen");
    }

    @Test
    void severidadInvalidaRespondeBadRequestYConservaResumenAnterior() throws Exception {
        publicarResumenValido();

        Map<String, Object> alerta = alerta("CRITICA", producto.getId());
        publicarInvalidoYComprobarAnterior(alerta);
    }

    @Test
    void identificadorInexistenteRespondeBadRequestYConservaResumenAnterior() throws Exception {
        publicarResumenValido();

        Map<String, Object> alerta = alerta("ALTA", 999_999L);
        publicarInvalidoYComprobarAnterior(alerta);
    }

    @Test
    void resumenConContenidoSerializadoMayorA255CaracteresSePersisteIntegro() throws Exception {
        String narrativaLarga = "N".repeat(420);
        Map<String, Object> cuerpo = resumen(List.of());
        cuerpo.put("narrativa", narrativaLarga);

        mockMvc.perform(post("/panel/resumen")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuerpo)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/panel/resumen")
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narrativa").value(narrativaLarga));
    }
    private void publicarResumenValido() throws Exception {
        mockMvc.perform(post("/panel/resumen")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resumen(List.of()))))
                .andExpect(status().isOk());
    }

    private void publicarInvalidoYComprobarAnterior(Map<String, Object> alerta) throws Exception {
        mockMvc.perform(post("/panel/resumen")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resumen(List.of(alerta)))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/panel/resumen")
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narrativa").value(NARRATIVA_VALIDA));
    }

    private Map<String, Object> resumen(List<Map<String, Object>> alertas) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("fecha", LocalDate.now(clock).toString());
        cuerpo.put("narrativa", NARRATIVA_VALIDA);
        cuerpo.put("alertas", alertas);
        cuerpo.put("accionesSugeridas", List.of());
        return cuerpo;
    }

    private Map<String, Object> alerta(String severidad, Long productoId) {
        Map<String, Object> alerta = new LinkedHashMap<>();
        alerta.put("severidad", severidad);
        alerta.put("titulo", "Producto en riesgo");
        alerta.put("detalle", "El producto requiere revisión de inventario.");
        alerta.put("productoId", productoId);
        alerta.put("ordenId", null);
        alerta.put("bodegaId", null);
        return alerta;
    }
}
