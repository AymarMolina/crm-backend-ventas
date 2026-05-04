package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder
public class VentaResponse {
    private UUID id;
    private String codigoVenta;
    private LocalDate fechaVenta;
    private BigDecimal monto;
    private String observaciones;
    private Boolean tieneAlerta;
    private String alertaDetalle;
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;

    // Campana
    private UUID campanaId;
    private String campanaNombre;
    private String lineaNombre;

    // Agente
    private UUID agenteId;
    private String agenteNombre;

    // Estado
    private String estadoCodigo;
    private String estadoNombre;

    // Cliente resuelto (ficha o campos sueltos)
    private UUID clienteId;
    private String clienteNombre;
    private String clienteDoc;
    private String clienteTelefono;
    private String clienteEmail;
    private String clienteDistrito;
}
