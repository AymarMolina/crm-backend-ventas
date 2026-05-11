package com.crmventas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaReporteDTO {
    private String     codigoVenta;
    private LocalDate  fechaVenta;
    private String     campanaNombre;
    private String     lineaNombre;
    private String     agenteNombre;
    private String     supervisorNombre;
    private String     clienteNombre;
    private String     clienteDoc;
    private String     clienteTelefono;
    private BigDecimal monto;
    private BigDecimal comisionPorcentaje;
    private BigDecimal comisionGenerada;
    private String     estadoCodigo;
    private Boolean    tieneAlerta;
    private String     alertaDetalle;
}