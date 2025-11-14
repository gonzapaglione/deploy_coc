package com.clinica.clinica_coc.services;

import com.clinica.clinica_coc.models.CoberturaSocial;
import java.util.List;

public interface ICoberturaSocialServicio {

    List<CoberturaSocial> listarCoberturas();

    CoberturaSocial buscarPorId(Long id);

    List<CoberturaSocial> buscarPorIds(List<Long> ids);

    CoberturaSocial guardarCobertura(CoberturaSocial cobertura);

    void eliminarCobertura(Long id);
}
