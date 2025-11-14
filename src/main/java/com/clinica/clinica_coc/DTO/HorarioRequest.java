package com.clinica.clinica_coc.DTO;
import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;
import com.clinica.clinica_coc.models.DiaSemana;

@Data
@Builder
public class HorarioRequest {
    
    private Long id_odontologo; 
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionTurno;
}