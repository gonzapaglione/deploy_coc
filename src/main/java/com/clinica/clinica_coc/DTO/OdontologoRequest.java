package com.clinica.clinica_coc.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OdontologoRequest {
    private PersonaRequest persona;
    private List<Long> especialidadesIds;
    private String estado_odont;
}
