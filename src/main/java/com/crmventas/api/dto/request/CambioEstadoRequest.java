package com.crmventas.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CambioEstadoRequest {

    @NotBlank
    @Pattern(regexp = "ACTIVO|EN_PROCESO|OBSERVADO|CAIDA", message = "Estado inválido")
    private String estadoCodigo;

    private String motivo;
}
