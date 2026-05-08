package com.crmventas.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActualizarUsuarioRequest {

    @NotBlank
    private String nombres;

    @NotBlank
    private String apellidos;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String rolCodigo;
}