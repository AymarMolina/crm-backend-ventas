package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
 
@Data
@Builder
public class ResumenBackOfficeResponse {
    private int totalVentas;          // todas las ventas activas del sistema
    private int ventasObservadas;     // estado OBSERVADO
    private int ventasEnProceso;      // estado EN_PROCESO
    private int ventasCaidas;         // estado CAIDA
    private int alertasPendientes;    // tieneAlerta = true
    private int modificacionesHoy;    // ventas actualizadas hoy
}
