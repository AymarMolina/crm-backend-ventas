package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;
 
@Data
@Builder
public class AlertaEquipoResponse {
    private UUID           ventaId;
    private String         codigoVenta;
    private String         clienteNombre;
    private String         alertaDetalle;
    private String         estado;
    private String         agenteNombre;    // quién registró la venta
    private OffsetDateTime actualizadoEn;
}