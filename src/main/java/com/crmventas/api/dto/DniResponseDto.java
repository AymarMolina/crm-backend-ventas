package com.crmventas.api.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
 
@JsonIgnoreProperties(ignoreUnknown = true)
public class DniResponseDto {
 
    @JsonProperty("dni")
    private String dni;
 
    @JsonProperty("nombres")
    private String nombres;
 
    @JsonProperty("apellidoPaterno")
    private String apellidoPaterno;
 
    @JsonProperty("apellidoMaterno")
    private String apellidoMaterno;
 
    @JsonProperty("codVerifica")
    private String codVerifica;
 
    // Nombre completo calculado
    public String getNombreCompleto() {
        return apellidoPaterno + " " + apellidoMaterno + ", " + nombres;
    }
 
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
 
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
 
    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
 
    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
 
    public String getCodVerifica() { return codVerifica; }
    public void setCodVerifica(String codVerifica) { this.codVerifica = codVerifica; }
}
 