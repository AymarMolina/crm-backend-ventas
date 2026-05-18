package com.crmventas.api.dto;

import java.math.BigDecimal;
 
public class ReporteSupervisorDTO {
 
    private String     campana;
    private Integer    mes;
    private Integer    anio;
 
    private String     supervisorId;
    private String     supervisor;
    private Integer    metaSupervisor;
    private BigDecimal comisionMaxSupervisor;
 
    private String     agenteId;
    private String     agente;
    private Integer    metaAgente;
    private BigDecimal comisionMaxAgente;
 
    private Long       ventasActivas;
    private BigDecimal montoTotal;
    private BigDecimal comisionGenerada;
    private BigDecimal pctAlcanceAgente;
 
    // ── Getters ──────────────────────────────────────────────────────────────
    public String     getCampana()               { return campana; }
    public Integer    getMes()                   { return mes; }
    public Integer    getAnio()                  { return anio; }
    public String     getSupervisorId()          { return supervisorId; }
    public String     getSupervisor()            { return supervisor; }
    public Integer    getMetaSupervisor()        { return metaSupervisor; }
    public BigDecimal getComisionMaxSupervisor() { return comisionMaxSupervisor; }
    public String     getAgenteId()              { return agenteId; }
    public String     getAgente()                { return agente; }
    public Integer    getMetaAgente()            { return metaAgente; }
    public BigDecimal getComisionMaxAgente()     { return comisionMaxAgente; }
    public Long       getVentasActivas()         { return ventasActivas; }
    public BigDecimal getMontoTotal()            { return montoTotal; }
    public BigDecimal getComisionGenerada()      { return comisionGenerada; }
    public BigDecimal getPctAlcanceAgente()      { return pctAlcanceAgente; }
 
    // ── Setters ──────────────────────────────────────────────────────────────
    public void setCampana(String v)                   { this.campana = v; }
    public void setMes(Integer v)                      { this.mes = v; }
    public void setAnio(Integer v)                     { this.anio = v; }
    public void setSupervisorId(String v)              { this.supervisorId = v; }
    public void setSupervisor(String v)                { this.supervisor = v; }
    public void setMetaSupervisor(Integer v)           { this.metaSupervisor = v; }
    public void setComisionMaxSupervisor(BigDecimal v) { this.comisionMaxSupervisor = v; }
    public void setAgenteId(String v)                  { this.agenteId = v; }
    public void setAgente(String v)                    { this.agente = v; }
    public void setMetaAgente(Integer v)               { this.metaAgente = v; }
    public void setComisionMaxAgente(BigDecimal v)     { this.comisionMaxAgente = v; }
    public void setVentasActivas(Long v)               { this.ventasActivas = v; }
    public void setMontoTotal(BigDecimal v)            { this.montoTotal = v; }
    public void setComisionGenerada(BigDecimal v)      { this.comisionGenerada = v; }
    public void setPctAlcanceAgente(BigDecimal v)      { this.pctAlcanceAgente = v; }
}
 
