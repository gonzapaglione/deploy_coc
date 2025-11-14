package com.clinica.clinica_coc.auth;

import java.util.List; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    String token;

    private Long idUsuario;
    private String email;
    private List<String> permisos; 
    List<String> roles;
    String nombre;

}