package com.crmventas.api.dto.response;

 
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
 
@Data
@Builder
public class TendenciaDiaResponse {
    private String     fecha;   // "dd/MM"
    private BigDecimal monto;
}