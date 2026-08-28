package com.logitrack.iq;

import static com.logitrack.support.LogiTrackTestData.linea;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.logitrack.entities.Bodega;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.repositories.MovimientoRepository;
import com.logitrack.support.FixedClockTestConfig;
import com.logitrack.support.LogiTrackTestData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockTestConfig.class, LogiTrackTestData.class})
@Transactional
class MovimientoStockValidationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Clock clock;
    @Autowired LogiTrackTestData datos;
    @Autowired MovimientoRepository movimientoRepository;

    private Usuario admin;
    private Bodega origen;
    private Bodega destino;
    private LocalDateTime ahora;

    @BeforeEach
    void preparar() {
        admin = datos.crearAdmin("admin-movimientos");
        origen = datos.crearBodega("Origen", admin);
        destino = datos.crearBodega("Destino", admin);
        ahora = LocalDateTime.now(clock);
    }

    @Test
    void salidaAceptaSaldoDerivadoAunqueInventarioAlmacenadoIndiqueCero() throws Exception {
        Producto producto = datos.crearProducto("Saldo derivado suficiente");
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(2), admin, null, origen,
                linea(producto, 10));
        datos.crearInventario(producto, origen, 0);

        mockMvc.perform(post("/api/movimientos")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson("SALIDA", origen.getId(), null,
                                new LineaHttp(producto.getId(), 5))))
                .andExpect(status().isCreated());
    }

    @Test
    void salidaRechazaSaldoDerivadoInsuficienteAunqueInventarioIndiqueCien() throws Exception {
        Producto producto = datos.crearProducto("Saldo derivado insuficiente");
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(2), admin, null, origen,
                linea(producto, 5));
        datos.crearInventario(producto, origen, 100);
        long movimientosAntes = movimientoRepository.count();

        mockMvc.perform(post("/api/movimientos")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson("SALIDA", origen.getId(), null,
                                new LineaHttp(producto.getId(), 6))))
                .andExpect(status().isBadRequest());

        assertEquals(movimientosAntes, movimientoRepository.count());
    }

    @Test
    void transferenciaRechazaSaldoDerivadoInsuficiente() throws Exception {
        Producto producto = datos.crearProducto("Transferencia insuficiente");
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(2), admin, null, origen,
                linea(producto, 5));
        datos.crearInventario(producto, origen, 100);

        mockMvc.perform(post("/api/movimientos")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson("TRANSFERENCIA", origen.getId(), destino.getId(),
                                new LineaHttp(producto.getId(), 6))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validaTodosLosDetallesAntesDePersistirElMovimiento() throws Exception {
        Producto suficiente = datos.crearProducto("Detalle suficiente");
        Producto insuficiente = datos.crearProducto("Detalle insuficiente");
        datos.crearMovimiento(TipoMovimiento.ENTRADA, ahora.minusDays(2), admin, null, origen,
                linea(suficiente, 10), linea(insuficiente, 2));
        datos.crearInventario(suficiente, origen, 100);
        datos.crearInventario(insuficiente, origen, 100);
        long movimientosAntes = movimientoRepository.count();

        mockMvc.perform(post("/api/movimientos")
                        .with(user(admin.getUsername()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson("SALIDA", origen.getId(), null,
                                new LineaHttp(suficiente.getId(), 5),
                                new LineaHttp(insuficiente.getId(), 3))))
                .andExpect(status().isBadRequest());

        assertEquals(movimientosAntes, movimientoRepository.count());
    }

    private String movimientoJson(
            String tipo,
            Long bodegaOrigenId,
            Long bodegaDestinoId,
            LineaHttp... lineas) throws Exception {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("tipo", tipo);
        cuerpo.put("bodegaOrigenId", bodegaOrigenId);
        cuerpo.put("bodegaDestinoId", bodegaDestinoId);
        List<Map<String, Object>> detalles = new ArrayList<>();
        for (LineaHttp linea : lineas) {
            detalles.add(Map.of(
                    "productoId", linea.productoId(),
                    "cantidad", linea.cantidad()));
        }
        cuerpo.put("detalles", detalles);
        return objectMapper.writeValueAsString(cuerpo);
    }

    private record LineaHttp(Long productoId, int cantidad) {
    }
}
