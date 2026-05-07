package com.crmventas.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CrearObjetivoRequest(

    @NotNull(message = "La campaña es obligatoria")
    UUID campanaId,

    @NotNull(message = "El usuario es obligatorio")
    UUID usuarioId,

    @NotNull(message = "El objetivo de ventas es obligatorio")
    @Positive(message = "El objetivo debe ser mayor a 0")
    Integer objetivoVentas,

    BigDecimal montoComision
) {}