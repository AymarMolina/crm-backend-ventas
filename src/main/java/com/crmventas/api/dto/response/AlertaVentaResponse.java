package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;
 
@Data
@Builder
public class AlertaVentaResponse {
    private UUID          id;
    private String        codigoVenta;
    private String        clienteNombre;
    private String        alertaDetalle;
    private String        estado;
    private OffsetDateTime actualizadoEn;
    private OffsetDateTime alertaExpiraEn;
}
 