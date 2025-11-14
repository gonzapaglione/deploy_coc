package com.clinica.clinica_coc.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAdminDTO {
    private int totalUsuarios;
    private int totalPacientes;
    private int totalOdontologos;
    private String odontologoDestacado;
    private String pacienteDestacado;
    private int turnosAtendidos;
    private int turnosCancelados;
    private int turnosSinAsistir;
    private int porcentajeAtendidos;
    private int porcentajeCancelados;
    private int porcentajeSinAsistir;
}
