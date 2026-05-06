package com.crmventas.api.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoRequest {

    private UUID campanaId;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    private String descripcion;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal precio;
}