package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
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
import com.logitrack.entities.Auditoria;
import com.logitrack.entities.ResumenPanel;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.TipoOperacion;
import com.logitrack.repositories.AuditoriaRepository;
import com.logitrack.repositories.ResumenPanelRepository;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class ResumenPanelReemplazoAuditoriaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;
    @Autowired ResumenPanelRepository resumenPanelRepository;
    @Autowired AuditoriaRepository auditoriaRepository;

    private Usuario admin;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-reemplazo-resumen");
    }

    @Test
    void segundoResumenValidoReemplazaLaUnicaFilaYDejaAuditoria() throws Exception {
        publicar("Primera narrativa válida que documenta la publicación inicial.");
        publicar("Segunda narrativa válida que debe reemplazar el contenido anterior.");

        LocalDate fecha = LocalDate.now(clock);
        ResumenPanel resumen = resumenPanelRepository.findByFecha(fecha).orElseThrow();
        assertEquals(1, resumenPanelRepository.findAll().stream()
                .filter(item -> fecha.equals(item.getFecha()))
                .count());

        mockMvc.perform(get("/panel/resumen").with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narrativa").value(
                        "Segunda narrativa válida que debe reemplazar el contenido anterior."));

        List<Auditoria> auditorias = auditoriaRepository.findAll().stream()
                .filter(item -> "ResumenPanel".equals(item.getEntidadAfectada()))
                .filter(item -> resumen.getId().equals(item.getEntidadId()))
                .toList();
        assertTrue(auditorias.stream().anyMatch(item -> item.getTipoOperacion() == TipoOperacion.INSERT));
        assertTrue(auditorias.stream().anyMatch(item -> item.getTipoOperacion() == TipoOperacion.UPDATE));
    }

    private void publicar(String narrativa) throws Exception {
        mockMvc.perform(post("/panel/resumen")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fecha", LocalDate.now(clock).toString(),
                                "narrativa", narrativa,
                                "alertas", List.of(),
                                "accionesSugeridas", List.of()))))
                .andExpect(status().isOk());
    }
}
