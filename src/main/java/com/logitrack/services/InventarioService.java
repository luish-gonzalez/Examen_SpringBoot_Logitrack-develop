package com.logitrack.services;

import com.logitrack.dto.InventarioDto;
import com.logitrack.entities.Bodega;
import com.logitrack.entities.Inventario;
import com.logitrack.entities.Producto;
import com.logitrack.exceptions.BusinessException;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.InventarioRepository;
import com.logitrack.repositories.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(
            InventarioRepository inventarioRepository,
            BodegaRepository bodegaRepository,
            ProductoRepository productoRepository) {

        this.inventarioRepository = inventarioRepository;
        this.bodegaRepository = bodegaRepository;
        this.productoRepository = productoRepository;
    }

    public List<InventarioDto> listarTodos() {
        return inventarioRepository.findAll()
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public InventarioDto buscarPorId(Long id) {

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventario no encontrado con id: " + id));

        return convertirADto(inventario);
    }

    public List<InventarioDto> buscarPorBodega(Long bodegaId) {

        return inventarioRepository.findByBodegaId(bodegaId)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public List<InventarioDto> buscarPorProducto(Long productoId) {

        return inventarioRepository.findByProductoId(productoId)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventarioDto guardar(InventarioDto dto) {

        if (dto.getStock() < 0) {
            throw new BusinessException("El stock no puede ser negativo.");
        }

        Bodega bodega = bodegaRepository.findById(dto.getBodegaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bodega no encontrada."));

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado."));

        Inventario inventario = new Inventario();
        inventario.setBodega(bodega);
        inventario.setProducto(producto);
        inventario.setStock(dto.getStock());

        return convertirADto(inventarioRepository.save(inventario));
    }

    @Transactional
    public InventarioDto actualizar(Long id, InventarioDto dto) {

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventario no encontrado con id: " + id));

        if (dto.getStock() < 0) {
            throw new BusinessException("El stock no puede ser negativo.");
        }

        Bodega bodega = bodegaRepository.findById(dto.getBodegaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bodega no encontrada."));

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado."));

        inventario.setBodega(bodega);
        inventario.setProducto(producto);
        inventario.setStock(dto.getStock());

        return convertirADto(inventarioRepository.save(inventario));
    }

    @Transactional
    public void eliminar(Long id) {

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventario no encontrado con id: " + id));

        inventarioRepository.delete(inventario);
    }

    private InventarioDto convertirADto(Inventario inventario) {

        return new InventarioDto(
                inventario.getId(),
                inventario.getBodega().getId(),
                inventario.getProducto().getId(),
                inventario.getStock()
        );
    }
}