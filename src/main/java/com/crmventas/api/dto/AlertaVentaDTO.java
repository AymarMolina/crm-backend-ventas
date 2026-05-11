package com.crmventas.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertaVentaDTO(
    UUID id,
    String codigoVenta,
    String clienteNombre,
    String alertaDetalle,
    String estado,
    LocalDateTime actualizadoEn,
    LocalDateTime alertaExpiraEn
) {}
