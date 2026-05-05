package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
 
@Data
@Builder
public class EstadoConteoResponse {
    private String estado;
    private String codigo;
    private long   total;
}