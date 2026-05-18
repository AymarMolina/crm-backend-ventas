package com.crmventas.api.dto;
import java.math.BigDecimal;
import java.util.UUID;

public interface ReporteAsesorResumen {
    UUID       getUsuarioId();
    String     getAsesorNombre();
    Integer    getObjetivoVentas();
    BigDecimal getMontoComision();
}
