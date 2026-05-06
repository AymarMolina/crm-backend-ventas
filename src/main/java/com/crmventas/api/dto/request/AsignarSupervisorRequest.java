package com.crmventas.api.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AsignarSupervisorRequest(
    @NotNull(message = "El ID del agente es obligatorio")
    UUID agenteId,
    
    @NotNull(message = "El ID del supervisor es obligatorio")
    UUID supervisorId
) {}
