package com.crmventas.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Boolean activo;
    private UUID campanaId;
    private String campanaNombre;
}