package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
 
@Data
@Builder
public class AgenteRendimientoResponse {
    private UUID       agenteId;
    private String     nombre;
    private String     email;
    private int        ventasActivas;
    private int        objetivo;
    private double     pctAlcance;          // 0–100
    private BigDecimal montoTotal;
    private BigDecimal comisionTotal;
    private int        alertas;
}