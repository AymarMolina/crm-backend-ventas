package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
 
@Data
@Builder
public class HistorialEstadoResponse {
    private String         estadoAnterior;
    private String         estadoNuevo;
    private String         cambiadoPor;     // nombre del usuario
    private String         rolEjecutor;
    private String         motivo;
    private OffsetDateTime cambiadoEn;
}
 