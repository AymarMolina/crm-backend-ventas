package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;
 
@Data
@Builder
public class ActividadRecienteResponse {
    private UUID           ventaId;
    private String         codigoVenta;
    private String         clienteNombre;
    private String         estadoAnterior;
    private String         estadoNuevo;
    private String         cambiadoPor;
    private OffsetDateTime cambiadoEn;
}