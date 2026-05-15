package com.crmventas.api.dto.response;

import java.util.UUID;

public record CampanaResponse(
    UUID   id,
    String nombre,
    String lineaNombre,
    int    mes,
    int    anio,
    int    objetivoTotal,
    boolean activo
) {}