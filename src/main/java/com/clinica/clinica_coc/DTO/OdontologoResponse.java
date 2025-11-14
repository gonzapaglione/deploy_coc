package com.clinica.clinica_coc.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OdontologoResponse {
    private Long id_odontologo;
    private PersonaBasicDTO persona;
    private List<EspecialidadDTO> especialidades;
    private String estado_odont;
}
