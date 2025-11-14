package com.clinica.clinica_coc.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponse {

    private Long id_paciente;
    private PersonaBasicDTO persona;
    private String estado_paciente;
    private List<CoberturaSocialDTO> coberturas;
}
