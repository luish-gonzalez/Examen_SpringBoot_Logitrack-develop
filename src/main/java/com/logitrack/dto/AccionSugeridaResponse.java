package com.logitrack.dto;

import com.logitrack.enums.TipoAccionSugerida;

public record AccionSugeridaResponse(
                TipoAccionSugerida tipo,
                String descripcion,
                Long ordenId,
                Long productoId,
                Long bodegaId) {
}
