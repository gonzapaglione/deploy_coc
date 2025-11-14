package com.clinica.clinica_coc.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinica.clinica_coc.models.Paciente;

@Repository
public interface PacienteRepositorio extends JpaRepository<Paciente, Long> {
@Query("SELECT p FROM Paciente p WHERE p.persona.id_persona = :idPersona")
    Optional<Paciente> findByPersonaId(@Param("idPersona") Long idPersona);

    @Query("SELECT p FROM Paciente p WHERE p.persona.id_persona = :idPersona AND p.estado_paciente = 'Activo'")
    Optional<Paciente> findByIdPersonaActivo(@Param("idPersona") Long idPersona);

  @Query("SELECT DISTINCT t.paciente FROM Turno t WHERE t.odontologo.id = :idOdontologo")
    List<Paciente> findPacientesConTurnosPorOdontologo(@Param("idOdontologo") Long idOdontologo);
}