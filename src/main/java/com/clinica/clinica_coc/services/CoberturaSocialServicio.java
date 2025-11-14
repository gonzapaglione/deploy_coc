package com.clinica.clinica_coc.services;

import com.clinica.clinica_coc.DTO.CoberturaSocialDTO;
import com.clinica.clinica_coc.models.CoberturaSocial;
import com.clinica.clinica_coc.repositories.CoberturaSocialRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CoberturaSocialServicio implements ICoberturaSocialServicio {

    @Autowired
    private CoberturaSocialRepositorio coberturaRepositorio;

    @Override
    public List<CoberturaSocial> listarCoberturas() {
        return coberturaRepositorio.findAll();
    }

    @Override
    public CoberturaSocial buscarPorId(Long id) {
        return coberturaRepositorio.findById(id).orElse(null);
    }

    @Override
    public List<CoberturaSocial> buscarPorIds(List<Long> ids) {
        return coberturaRepositorio.findAllById(ids);
    }

    @Override
    public CoberturaSocial guardarCobertura(CoberturaSocial cobertura) {
        return coberturaRepositorio.save(cobertura);
    }

    @Override
    public void eliminarCobertura(Long id) {
        if (coberturaRepositorio.existsById(id)) {
            coberturaRepositorio.deleteById(id);
        }
    }

    // Devolver DTOs con los nombres exactos esperados por el front
    public List<CoberturaSocialDTO> findAllDtos() {
        return coberturaRepositorio.findAll().stream()
                .map(c -> new CoberturaSocialDTO(c.getId_cob_social(), c.getNombre_cobertura(), c.getEstado_cobertura()))
                .collect(Collectors.toList());
    }

    public List<CoberturaSocialDTO> listarActivas() {
        List<CoberturaSocial> entidades = coberturaRepositorio.findByEstadoCobertura("Activo");

        return entidades.stream()
            .map(c -> new CoberturaSocialDTO(c.getId_cob_social(), c.getNombre_cobertura(), c.getEstado_cobertura()))
            .collect(Collectors.toList());
    }

    // Devolver DTO por id
    public CoberturaSocialDTO findDtoById(Long id) {
        CoberturaSocial c = buscarPorId(id);
        if (c == null)
            return null;
        return new CoberturaSocialDTO(c.getId_cob_social(), c.getNombre_cobertura(), c.getEstado_cobertura());
    }

    public CoberturaSocial crearCobertura(CoberturaSocial cobertura) {
        // 1. Controlar que no esté cargada ya
        Optional<CoberturaSocial> existente = coberturaRepositorio.findByNombreNativoConParam(cobertura.getNombre_cobertura());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("La cobertura '" + cobertura.getNombre_cobertura() + "' ya existe.");
        }
        // 2. Si no existe, la crea como "Activo"
        cobertura.setEstado_cobertura("Activo");
        return coberturaRepositorio.save(cobertura);
    }

    public CoberturaSocial actualizarCobertura(Long id, CoberturaSocial coberturaDetails) {
        CoberturaSocial cobertura = coberturaRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la cobertura con ID: " + id));

        // Verificar que el nuevo nombre no exista en OTRA cobertura
        Optional<CoberturaSocial> existenteConNuevoNombre = coberturaRepositorio.findByNombreNativoConParam(coberturaDetails.getNombre_cobertura());
        if (existenteConNuevoNombre.isPresent() && !existenteConNuevoNombre.get().getId_cob_social().equals(id)) {
            throw new IllegalArgumentException("El nombre '" + coberturaDetails.getNombre_cobertura() + "' ya está en uso por otra cobertura.");
        }

        cobertura.setNombre_cobertura(coberturaDetails.getNombre_cobertura());
        return coberturaRepositorio.save(cobertura);
    }

    public CoberturaSocial bajaLogica(Long id) {
        CoberturaSocial cobertura = coberturaRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la cobertura con ID: " + id));

        cobertura.setEstado_cobertura("Inactivo");
        return coberturaRepositorio.save(cobertura);
    }
    
    public CoberturaSocial reactivarCobertura(Long id) {
        CoberturaSocial cobertura = coberturaRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la cobertura con ID: " + id));
        
        cobertura.setEstado_cobertura("Activo");
        return coberturaRepositorio.save(cobertura);
    }
}
