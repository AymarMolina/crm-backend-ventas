package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
 
@Data
@Builder
public class ResumenSupervisorResponse {
    private int        totalAgentes;        // agentes bajo su cargo
    private int        ventasEquipo;        // ventas activas del equipo
    private int        objetivoEquipo;      // suma de objetivos del equipo
    private BigDecimal montoEquipo;         // monto total del equipo
    private BigDecimal comisionEquipo;      // comisión total generada
    private int        alertasEquipo;       // alertas activas del equipo
}
