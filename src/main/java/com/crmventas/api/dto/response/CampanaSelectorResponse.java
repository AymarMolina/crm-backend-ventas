package com.crmventas.api.dto.response;

import java.util.UUID;

public record CampanaSelectorResponse(
    UUID   id,
    String nombre,       // "Campaña Migraciones — Mayo 2025"
    String lineaNombre,
    int    mes,
    int    anio
) {}
 
