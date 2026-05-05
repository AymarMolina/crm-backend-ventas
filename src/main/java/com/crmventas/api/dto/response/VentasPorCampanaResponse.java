package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;
 
@Data
@Builder
public class VentasPorCampanaResponse {
    private String campana;
    private long   total;
}
 
 