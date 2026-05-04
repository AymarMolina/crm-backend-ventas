package com.crmventas.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String rol;
    private String nombres;
    private Boolean debeCambiarPass;
}
