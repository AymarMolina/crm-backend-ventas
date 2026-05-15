package com.crmventas.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertaResponse(
    UUID           ventaId,
    String         codigoVenta,
    String         alertaDetalle,
    String         estadoNombre,
    String         agenteNombre,
    OffsetDateTime actualizadoEn
) {}
 