package com.clinica.clinica_coc.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BajaPacienteResponse {

    private Long idPaciente;
    private String nombre;
    private String apellido;
    private String isActive; // "Activo" o "Inactivo"
    private String mensaje; // "Paciente dado de baja lógicamente"
}
