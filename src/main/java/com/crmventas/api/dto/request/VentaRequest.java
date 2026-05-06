package com.crmventas.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VentaRequest {

    @NotNull
    private UUID campanaId;

    // Si se pasa clienteId, se usa la ficha; si no, se usan los campos sueltos
    private UUID clienteId;

    // Campos sueltos (requeridos cuando no hay clienteId)
    private String clienteNombre;
    private String clienteDoc;
    private String clienteTelefono;
        

    private LocalDate fechaVenta;

    private UUID productoId; 

    @DecimalMin("0.00")
    private BigDecimal monto;

    private String observaciones;
}
