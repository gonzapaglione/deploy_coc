package com.clinica.clinica_coc.services;

import com.clinica.clinica_coc.models.Especialidad;
import com.clinica.clinica_coc.repositories.EspecialidadRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import com.clinica.clinica_coc.DTO.EspecialidadDTO;

@Service
public class EspecialidadServicio {

    @Autowired
    private EspecialidadRepositorio especialidadRepositorio;

    public List<Especialidad> listarEspecialidades() {
        return especialidadRepositorio.findAll();
    }

    public List<Especialidad> listarActivas() {
        return especialidadRepositorio.findByEstadoEspecialidad("Activo");
    }

    public Especialidad buscarPorId(Long id) {
        return especialidadRepositorio.findById(id).orElse(null);
    }

    public List<Especialidad> buscarPorIds(List<Long> ids) {
        return especialidadRepositorio.findAllById(ids);
    }

    // devolver DTOs con los nombres exactos esperados por el front
    public List<EspecialidadDTO> findAllDtos() {
        return especialidadRepositorio.findAll().stream()
                .map(e -> new EspecialidadDTO(e.getId_especialidad(), e.getNombre(), e.getEstado_especialidad()))
                .collect(Collectors.toList());
    }

    // devolver DTO por id
    public EspecialidadDTO findDtoById(Long id) {
        Especialidad e = buscarPorId(id);
        if (e == null)
            return null;
        return new EspecialidadDTO(e.getId_especialidad(), e.getNombre(), e.getEstado_especialidad());
    }

    public Especialidad crearEspecialidad(Especialidad especialidad) {
        // 1. Controlar que no esté cargada ya
        Optional<Especialidad> existente = especialidadRepositorio.findByNombre(especialidad.getNombre());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("La especialidad '" + especialidad.getNombre() + "' ya existe.");
        }
        System.out.println("Especialidad recibida: "+ especialidad.getNombre() + " "+ especialidad.getEstado_especialidad());
        // 2. Si no existe, la crea como "Activo"
        especialidad.setEstado_especialidad("Activo");
        return especialidadRepositorio.save(especialidad);
    }

    /**
     * Realiza la baja lógica de una especialidad.
     */
    public Especialidad bajaLogica(Long id) {
        Especialidad especialidad = especialidadRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la especialidad con ID: " + id));
        
        especialidad.setEstado_especialidad("Inactivo");
        return especialidadRepositorio.save(especialidad);
    }
    
    /**
     * Reactiva una especialidad que estaba inactiva.
     */
    public Especialidad reactivarEspecialidad(Long id) {
        Especialidad especialidad = especialidadRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la especialidad con ID: " + id));
        
        especialidad.setEstado_especialidad("Activo");
        return especialidadRepositorio.save(especialidad);
    }

    /**
     * Actualiza el nombre de una especialidad.
     */
    public Especialidad actualizarEspecialidad(Long id, Especialidad especialidadDetails) {
        Especialidad especialidad = especialidadRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la especialidad con ID: " + id));

        // Verificar que el nuevo nombre no exista en OTRA especialidad
        Optional<Especialidad> existenteConNuevoNombre = especialidadRepositorio.findByNombre(especialidadDetails.getNombre());
        if (existenteConNuevoNombre.isPresent() && !existenteConNuevoNombre.get().getId_especialidad().equals(id)) {
            throw new IllegalArgumentException("El nombre '" + especialidadDetails.getNombre() + "' ya está en uso por otra especialidad.");
        }

        especialidad.setNombre(especialidadDetails.getNombre());
        return especialidadRepositorio.save(especialidad);
    }

}
