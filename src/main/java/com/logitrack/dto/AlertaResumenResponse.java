package com.logitrack.dto;

import com.logitrack.enums.SeveridadAlerta;

public record AlertaResumenResponse(
                SeveridadAlerta severidad,
                String titulo,
                String detalle,
                Long productoId,
                Long ordenId,
                Long bodegaId) {
}
