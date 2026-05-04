package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder
public class ClienteResponse {
    private UUID id;
    private String tipoDoc;
    private String nroDoc;
    private String nombre;
    private String apellidos;
    private String nombreCompleto;
    private String telefono;
    private String telefonoAlt;
    private String email;
    private String direccion;
    private String distrito;
    private Boolean activo;
    private OffsetDateTime creadoEn;
}
