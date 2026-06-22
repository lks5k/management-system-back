package com.imsas.erp.modules.auth;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class LoginResponse {

    
    String token;

    
    @Builder.Default
    String tipo = "Bearer";

    
    UUID id;

    
    String nombre;

    
    String email;

    
    String rol;
}
