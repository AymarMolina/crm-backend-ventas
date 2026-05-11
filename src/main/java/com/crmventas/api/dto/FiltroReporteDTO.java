package com.crmventas.api.dto;

import lombok.Builder;
import lombok.Data;
 
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
 
@Data
@Builder
public class FiltroReporteDTO {
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private UUID      campanaId;
    private String    campanaNombre;   // nombre para mostrar en el reporte
    private UUID      agenteId;        // para reportes de ASESOR (un solo agente)
    private List<UUID> agenteIds;      // para SUPERVISOR (equipo completo)
    private UUID      supervisorId;    // para GERENTE filtrando por supervisor
}