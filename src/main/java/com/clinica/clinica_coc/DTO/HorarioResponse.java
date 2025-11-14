package com.clinica.clinica_coc.DTO;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HorarioResponse {
    private Long id_horario;
    private Long id_odontologo;
    private String diaSemana; // Lo enviamos como String
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionTurno;
}
