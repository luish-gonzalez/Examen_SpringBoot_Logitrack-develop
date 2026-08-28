package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.repositories.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistroPublicoSecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "AGENTE"})
    void registroPublicoNoCreaUsuariosPrivilegiados(String rol) throws Exception {
        long usuariosAntes = usuarioRepository.count();
        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "nombre", "Usuario privilegiado",
                "username", "publico-" + rol.toLowerCase(),
                "password", "password-test",
                "rol", rol));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());

        assertEquals(usuariosAntes, usuarioRepository.count());
    }
}
