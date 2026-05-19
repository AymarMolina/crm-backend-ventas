package com.crmventas.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ReporteFiltroDTO {
 
    private UUID campanaId;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    // ── Filtro por rol ──────────────────────────────────────────
    private UUID supervisorId;   // si es SUPERVISOR, filtra su equipo
    private UUID agenteId;       // si es AGENTE, filtra solo él

    public UUID getCampanaId() { return campanaId; }
    public void setCampanaId(UUID v) { this.campanaId = v; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate v) { this.fechaDesde = v; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate v) { this.fechaHasta = v; }

    public UUID getSupervisorId() { return supervisorId; }
    public void setSupervisorId(UUID v) { this.supervisorId = v; }

    public UUID getAgenteId() { return agenteId; }
    public void setAgenteId(UUID v) { this.agenteId = v; }
}
 