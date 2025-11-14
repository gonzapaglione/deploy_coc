package com.clinica.clinica_coc.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPacienteDTO {
    private int totalTurnos;
    private String odontMasVisitado;
}
