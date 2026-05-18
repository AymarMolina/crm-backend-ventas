package com.crmventas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DocumentoRequestDto {
 
    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "DNI|RUC", message = "El tipo debe ser DNI o RUC")
    private String tipoDocumento;
 
    @NotBlank(message = "El número de documento es obligatorio")
    private String numero;
 
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento.toUpperCase(); }
 
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero.trim(); }
}
 
