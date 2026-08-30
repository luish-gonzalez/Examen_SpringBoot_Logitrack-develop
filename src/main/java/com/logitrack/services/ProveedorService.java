package com.logitrack.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logitrack.dto.ProveedorResponse;
import com.logitrack.repositories.ProveedorRepository;

@Service
@Transactional(readOnly = true)
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<ProveedorResponse> listarTodos() {
        return proveedorRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(proveedor -> new ProveedorResponse(
                        proveedor.getId(),
                        proveedor.getNombre(),
                        proveedor.getContacto(),
                        proveedor.getDiasEntrega()))
                .toList();
    }
}
