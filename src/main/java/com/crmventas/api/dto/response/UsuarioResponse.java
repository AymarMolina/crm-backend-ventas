package com.crmventas.api.dto.response;

import java.util.UUID;

public record UsuarioResponse(
    UUID id,
    String nombres,
    String apellidos,
    String email,
    String nombreSupervisor
) {}