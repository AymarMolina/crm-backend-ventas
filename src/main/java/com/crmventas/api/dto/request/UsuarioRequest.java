package com.crmventas.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequest {
    @NotBlank @Size(max = 100)
    private String nombres;
    
    @NotBlank @Size(max = 100)
    private String apellidos;
    
    @Email @NotBlank
    private String email;
    
    @NotBlank @Size(min = 6)
    private String password;
    
    @NotBlank
    private String rolCodigo; // "GERENTE", "SUPERVISOR", "BACK_OFFICE", "AGENTE"
}
