package com.clinica.clinica_coc.DTO;

import java.util.List;

public class AsignarPacienteRequest {

    private Long idPersona;
    private List<Long> coberturasIds;


    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public List<Long> getCoberturasIds() {
        return coberturasIds;
    }

    public void setCoberturasIds(List<Long> coberturasIds) {
        this.coberturasIds = coberturasIds;
    }
}