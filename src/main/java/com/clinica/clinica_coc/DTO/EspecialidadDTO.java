package com.clinica.clinica_coc.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadDTO {
    private Long idEspecialidad;
    private String nombreEspecialidad;
    private String estado_especialidad;
}
