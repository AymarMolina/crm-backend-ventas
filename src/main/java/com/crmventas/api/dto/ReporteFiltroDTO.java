package com.crmventas.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ReporteFiltroDTO {
 
    private UUID campanaId;
 
    private LocalDate fechaDesde;
 
    private LocalDate fechaHasta;
 
    public UUID getCampanaId() { return campanaId; }
    public void setCampanaId(UUID campanaId) { this.campanaId = campanaId; }
 
    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }
 
    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }
}
 