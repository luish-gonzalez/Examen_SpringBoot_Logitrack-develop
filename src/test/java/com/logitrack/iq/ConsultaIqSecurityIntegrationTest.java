package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.Rol;
import com.logitrack.repositories.UsuarioRepository;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(LogiTrackTestData.class)
@Transactional
class ConsultaIqSecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired LogiTrackTestData datos;
    @Autowired UsuarioRepository usuarioRepository;

    private Usuario admin;
    private Usuario agente;
    private Usuario empleado;
    private Producto producto;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-consultas-iq");
        agente = crearUsuario("agente-consultas-iq", Rol.AGENTE);
        empleado = crearUsuario("empleado-consultas-iq", Rol.EMPLEADO);
        producto = datos.crearProducto("Producto seguridad consultas");
    }

    @Test
    void stockRespetaMatrizDeConsultaIq() throws Exception {
        verificarMatriz("/productos/" + producto.getId() + "/stock");
    }

    @Test
    void riesgoRespetaMatrizDeConsultaIq() throws Exception {
        verificarMatriz("/productos/riesgo");
    }

    @Test
    void proveedoresRespetaMatrizDeConsultaIq() throws Exception {
        verificarMatriz("/proveedores");
    }

    @Test
    void kpisExigeRutaFuncionalAntesDePoderValidarSuMatriz() throws Exception {
        verificarMatriz("/kpis");
    }

    @Test
    void bodegasCriticasExigeRutaFuncionalAntesDePoderValidarSuMatriz() throws Exception {
        verificarMatriz("/bodegas/criticas");
    }

    private void verificarMatriz(String ruta) throws Exception {
        assertAll(
                () -> mockMvc.perform(get(ruta).with(user(admin.getUsername()).roles("ADMIN")))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get(ruta).with(user(agente.getUsername()).roles("AGENTE")))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get(ruta).with(user(empleado.getUsername()).roles("EMPLEADO")))
                        .andExpect(status().isForbidden()),
                () -> mockMvc.perform(get(ruta)).andExpect(status().isUnauthorized()));
    }

    private Usuario crearUsuario(String username, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de seguridad");
        usuario.setUsername(username);
        usuario.setPassword("password-test");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.saveAndFlush(usuario);
    }
}
