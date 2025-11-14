package com.clinica.clinica_coc.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.clinica.clinica_coc.models.Especialidad;

@Repository
public interface EspecialidadRepositorio extends JpaRepository<Especialidad, Long> {
    /**
     * Busca una especialidad por su nombre.
     * Se usará para verificar si ya existe antes de crear una nueva.
     */
    Optional<Especialidad> findByNombre(String nombre);
    
    /**
     * Opcional: Busca todas las especialidades por un estado específico.
     */
    @Query("SELECT e FROM Especialidad e WHERE e.estado_especialidad = :estado")
    List<Especialidad> findByEstadoEspecialidad(@Param("estado")  String estado);
}