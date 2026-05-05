package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
 
@Data
@Builder
public class ResumenAsesorResponse {
    private int     ventasActivas;
    private int     objetivo;
    private BigDecimal montoTotal;
    private BigDecimal comisionEstimada;
    private int     alertas;
}