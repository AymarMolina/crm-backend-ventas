package com.crmventas.api.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardGerenteResponse(
    CampanaResponse campana,
    int        ventasActivas,
    int        objetivoCampana,
    double     pctAlcance,
    int        agentesActivos,
    int        agentesTotal,
    int        alertasActivas,
    int        caidas,
    double     tasaCaida,
    BigDecimal comisionEstimada,
    Map<String, Integer>               distribucionEstados,
    List<AgenteRendimientoResponsedash> agentes,    // ← este cambio
    List<AlertaResponse>               alertasRecientes
) {}
 