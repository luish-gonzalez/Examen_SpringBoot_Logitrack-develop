package com.logitrack.iq;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class OrdenCompraPdfIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired LogiTrackTestData datos;

    private Usuario admin;
    private Producto producto;
    private Bodega bodega;
    private long proveedorId;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-pdf");
        bodega = datos.crearBodega("Bodega PDF", admin);
        producto = datos.crearProducto("Producto PDF");
        proveedorId = datos.asignarProveedorPrincipal(producto, 10);
    }

    @Test
    void pdfBorradorSeGuardaContieneMarcaYSeInvalidaAlCambiarEstado() throws Exception {
        long ordenId = crearOrdenBorrador();

        mockMvc.perform(post("/ordenes/{id}/pdf", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        MvcResult descarga = mockMvc.perform(get("/ordenes/{id}/pdf", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        byte[] pdf = descarga.getResponse().getContentAsByteArray();
        assertTrue(pdf.length > 0);
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(documento);
            assertTrue(texto.contains("BORRADOR"));
        }

        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "APROBADA"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/ordenes/{id}/pdf", ordenId)
                        .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    private long crearOrdenBorrador() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "productoId", producto.getId(),
                "proveedorId", proveedorId,
                "bodegaDestinoId", bodega.getId(),
                "cantidad", 10,
                "precioUnitario", 4500.00));
        MvcResult resultado = mockMvc.perform(post("/ordenes")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .path("id")
                .asLong();
    }
}
