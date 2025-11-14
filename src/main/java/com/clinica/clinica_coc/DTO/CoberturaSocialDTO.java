package com.clinica.clinica_coc.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoberturaSocialDTO {
    private Long id_cob_social;
    private String nombre_cobertura;
    private String estado_cobertura;
}
