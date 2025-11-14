package com.clinica.clinica_coc.DTO;

import lombok.Data;
import java.util.List;

@Data
public class AsignarOdontologoRequest {
    private Long idPersona;
    private List<Long> especialidadesIds;
}