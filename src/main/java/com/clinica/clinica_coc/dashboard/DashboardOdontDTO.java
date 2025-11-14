package com.clinica.clinica_coc.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOdontDTO {

    private int turnosCompletados;
    private int turnosPendientesHoy;
    private String practicaMasRealizada;
}
