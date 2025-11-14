package com.clinica.clinica_coc.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BajaResponse {
    private String mensaje;
    private Object datos;
}
