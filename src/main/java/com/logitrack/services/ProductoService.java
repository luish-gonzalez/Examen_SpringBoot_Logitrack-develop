package com.logitrack.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.logitrack.dto.ProductoDto;
import com.logitrack.entities.Producto;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoDto> listarTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirADto)
                .toList();
    }

    public ProductoDto obtenerPorId(Long id) {
        Producto producto = buscarProductoPorId(id);
        return convertirADto(producto);
    }

    public ProductoDto crear(ProductoDto productoDto) {
        Producto producto = new Producto();

        producto.setNombre(productoDto.getNombre().trim());
        producto.setCategoria(productoDto.getCategoria().trim());
        producto.setPrecio(productoDto.getPrecio());

        Producto productoGuardado = productoRepository.save(producto);

        return convertirADto(productoGuardado);
    }

    public ProductoDto actualizar(Long id, ProductoDto productoDto) {
        Producto producto = buscarProductoPorId(id);

        producto.setNombre(productoDto.getNombre().trim());
        producto.setCategoria(productoDto.getCategoria().trim());
        producto.setPrecio(productoDto.getPrecio());

        Producto productoActualizado = productoRepository.save(producto);

        return convertirADto(productoActualizado);
    }

    public void eliminar(Long id) {
        Producto producto = buscarProductoPorId(id);
        productoRepository.delete(producto);
    }

    private Producto buscarProductoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id
                ));
    }

    private ProductoDto convertirADto(Producto producto) {
        return new ProductoDto(
                producto.getId(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getPrecio()
        );
    }
}