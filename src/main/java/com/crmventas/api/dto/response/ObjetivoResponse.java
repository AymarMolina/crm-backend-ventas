package com.crmventas.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ObjetivoResponse(
    Integer id,
    UUID campanaId,
    String campanaNombre,
    UUID usuarioId,
    String usuarioNombre,
    Integer objetivoVentas,
    BigDecimal montoComision,
    OffsetDateTime creadoEn
) {}