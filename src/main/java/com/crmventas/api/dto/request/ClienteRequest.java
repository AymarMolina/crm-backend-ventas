package com.crmventas.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteRequest {

    @NotBlank
    @Pattern(regexp = "DNI|CE|RUC|PASAPORTE", message = "Tipo doc inválido")
    private String tipoDoc;

    @NotBlank @Size(max = 20)
    private String nroDoc;

    @NotBlank @Size(max = 100)
    private String nombre;

    @NotBlank @Size(max = 100)
    private String apellidos;

    @Size(max = 20)
    private String telefono;

    @Size(max = 20)
    private String telefonoAlt;

    @Email @Size(max = 180)
    private String email;

    private String direccion;

    @Size(max = 80)
    private String distrito;
}
