package com.logitrack.support;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.logitrack.entities.Bodega;
import com.logitrack.entities.DetalleMovimiento;
import com.logitrack.entities.Inventario;
import com.logitrack.entities.Movimiento;
import com.logitrack.entities.Producto;
import com.logitrack.entities.Usuario;
import com.logitrack.enums.Rol;
import com.logitrack.enums.TipoMovimiento;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.InventarioRepository;
import com.logitrack.repositories.MovimientoRepository;
import com.logitrack.repositories.ProductoRepository;
import com.logitrack.repositories.UsuarioRepository;

import jakarta.persistence.EntityManager;

public class LogiTrackTestData {

    private final UsuarioRepository usuarioRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final InventarioRepository inventarioRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public LogiTrackTestData(
            UsuarioRepository usuarioRepository,
            BodegaRepository bodegaRepository,
            ProductoRepository productoRepository,
            MovimientoRepository movimientoRepository,
            InventarioRepository inventarioRepository,
            JdbcTemplate jdbcTemplate,
            EntityManager entityManager) {
        this.usuarioRepository = usuarioRepository;
        this.bodegaRepository = bodegaRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.inventarioRepository = inventarioRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    public Usuario crearAdmin(String username) {
        return crearUsuario(username, Rol.ADMIN);
    }

    public Usuario crearEmpleado(String username) {
        return crearUsuario(username, Rol.EMPLEADO);
    }

    private Usuario crearUsuario(String username, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de pruebas");
        usuario.setUsername(username);
        usuario.setPassword("password-test");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.saveAndFlush(usuario);
    }

    public Bodega crearBodega(String nombre, Usuario encargado) {
        Bodega bodega = new Bodega();
        bodega.setNombre(nombre);
        bodega.setUbicacion("Bogotá");
        bodega.setCapacidad(1_000);
        bodega.setEncargado(encargado);
        return bodegaRepository.saveAndFlush(bodega);
    }

    public Producto crearProducto(String nombre) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCategoria("Pruebas");
        producto.setPrecio(new BigDecimal("4500.00"));
        return productoRepository.saveAndFlush(producto);
    }

    public long asignarProveedorPrincipal(Producto producto, int diasEntrega) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO proveedores (nombre, contacto, dias_entrega) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "Proveedor " + producto.getNombre());
            statement.setString(2, "contacto@prueba.local");
            statement.setInt(3, diasEntrega);
            return statement;
        }, keyHolder);

        long proveedorId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        jdbcTemplate.update(
                "UPDATE productos SET proveedor_principal_id = ? WHERE id = ?",
                proveedorId,
                producto.getId());
        entityManager.flush();
        entityManager.refresh(producto);
        return proveedorId;
    }

    public Inventario crearInventario(Producto producto, Bodega bodega, int stock) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setBodega(bodega);
        inventario.setStock(stock);
        return inventarioRepository.saveAndFlush(inventario);
    }

    public Movimiento crearMovimiento(
            TipoMovimiento tipo,
            LocalDateTime fecha,
            Usuario responsable,
            Bodega origen,
            Bodega destino,
            Linea... lineas) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo);
        movimiento.setFecha(fecha);
        movimiento.setUsuarioResponsable(responsable);
        movimiento.setBodegaOrigen(origen);
        movimiento.setBodegaDestino(destino);
        movimiento.setDetalles(new ArrayList<>());

        Arrays.stream(lineas).forEach(linea -> {
            DetalleMovimiento detalle = new DetalleMovimiento();
            detalle.setMovimiento(movimiento);
            detalle.setProducto(linea.producto());
            detalle.setCantidad(linea.cantidad());
            movimiento.getDetalles().add(detalle);
        });

        return movimientoRepository.saveAndFlush(movimiento);
    }

    public static Linea linea(Producto producto, int cantidad) {
        return new Linea(producto, cantidad);
    }

    public record Linea(Producto producto, int cantidad) {
    }
}
