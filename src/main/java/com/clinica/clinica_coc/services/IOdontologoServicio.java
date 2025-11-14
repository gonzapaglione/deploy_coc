package com.clinica.clinica_coc.services;

import java.util.List;

import com.clinica.clinica_coc.DTO.PersonaRequest;
import com.clinica.clinica_coc.models.Odontologo;

public interface IOdontologoServicio {
    public List<Odontologo> listarOdontologos();

    public Odontologo buscarOdontologoPorId(Long id);

    public Odontologo guardarOdontologo(Odontologo odontologo);

    public void eliminarOdontologo(Odontologo odontologo);

    public Odontologo crearOdontologoConPersonaYRol(PersonaRequest personaRequest, List<Long> especialidadesIds);
}
