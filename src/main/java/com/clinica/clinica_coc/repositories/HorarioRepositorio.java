package com.clinica.clinica_coc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.clinica.clinica_coc.models.DiaSemana;
import com.clinica.clinica_coc.models.Horario;
import com.clinica.clinica_coc.models.Odontologo;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioRepositorio extends JpaRepository<Horario, Long> {
    @Query("SELECT h FROM Horario h WHERE h.odontologo.id_odontologo = :idOdontologo")
List<Horario> findHorariosPorOdontologo(@Param("idOdontologo") Long idOdontologo);

Optional<Horario> findFirstByOdontologoAndDiaSemanaAndHoraInicioLessThanEqualAndHoraFinGreaterThan(
            Odontologo odontologo,
            String diaSemana,
            LocalTime horaTurno,
            LocalTime horaTurnoAgain
    );

@Query("SELECT h FROM Horario h WHERE h.odontologo.id_odontologo = ?1 AND h.diaSemana = ?2")
    List<Horario> findByOdontologoIdOdontologoAndDiaSemana(Long idOdontologo, DiaSemana diaSemana);
}