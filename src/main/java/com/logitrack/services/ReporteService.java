package com.logitrack.services;

import com.logitrack.dto.ResumenGeneralDto;
import com.logitrack.entities.Inventario;
import com.logitrack.repositories.BodegaRepository;
import com.logitrack.repositories.InventarioRepository;
import com.logitrack.repositories.MovimientoRepository;
import com.logitrack.repositories.ProductoRepository;
import com.logitrack.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class ReporteService {

    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final InventarioRepository inventarioRepository;

    public ReporteService(
            ProductoRepository productoRepository,
            BodegaRepository bodegaRepository,
            UsuarioRepository usuarioRepository,
            MovimientoRepository movimientoRepository,
            InventarioRepository inventarioRepository) {

        this.productoRepository = productoRepository;
        this.bodegaRepository = bodegaRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    public ResumenGeneralDto obtenerResumenGeneral() {

        Long totalProductos = productoRepository.count();

        Long totalBodegas = bodegaRepository.count();

        Long totalUsuarios = usuarioRepository.count();

        Long totalMovimientos = movimientoRepository.count();

        Long totalRegistrosInventario = inventarioRepository.count();

        Integer stockTotal = inventarioRepository.findAll()
                .stream()
                .map(Inventario::getStock)
                .filter(stock -> stock != null)
                .reduce(0, Integer::sum);

        return new ResumenGeneralDto(
                totalProductos,
                totalBodegas,
                totalUsuarios,
                totalMovimientos,
                totalRegistrosInventario,
                stockTotal
        );
    }

}