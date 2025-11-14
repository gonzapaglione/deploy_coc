package com.clinica.clinica_coc.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse {
    
    private Long id_turno;
    private PacienteResumidoDTO paciente;
    private OdontologoResumidoDTO odontologo;
    private CoberturaSocialDTO cobertura;
    private String motivoConsulta;
    private String estadoTurno;
    private LocalDate fecha;      
    private LocalTime horaInicio; 
    private LocalTime horaFin;    
    
    private String tratamiento;
    private String evolucion;
}