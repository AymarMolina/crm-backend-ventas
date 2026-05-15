package com.crmventas.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AgenteRendimientoResponsedash (
    UUID       agenteId,
    String     nombreCompleto,
    String     supervisorNombre,
 
    int        ventasActivas,
    int        objetivoVentas,
    double     pctAlcance,          // ventasActivas / objetivoVentas * 100
 
    BigDecimal comisionEstimada,    // suma de ventas.comision_generada del agente
    BigDecimal montoComisionMax,    // crm.objetivos.monto_comision
 
    int        caidas,
    int        alertas
) {}
 
 