package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
 
@Data
@Builder
public class VentaObservadaResponse {
    private UUID           id;
    private String         codigoVenta;
    private String         clienteNombre;
    private String         clienteDoc;
    private String         estadoCodigo;
    private String         estadoNombre;
    private String         alertaDetalle;
    private String         agenteNombre;
    private String         campanaNombre;
    private String         lineaNombre;
    private BigDecimal     monto;
    private LocalDate      fechaVenta;
    private OffsetDateTime actualizadoEn;
}