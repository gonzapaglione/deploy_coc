package com.clinica.clinica_coc.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.clinica.clinica_coc.models.Turno;
import com.clinica.clinica_coc.dashboard.TurnosPorObraSocialDTO;

@Repository
public interface TurnoRepositorio extends JpaRepository<Turno, Long>, JpaSpecificationExecutor<Turno> {

       @Query("SELECT t FROM Turno t " +
                     "LEFT JOIN FETCH t.paciente p " +
                     "LEFT JOIN FETCH p.persona " +
                     "LEFT JOIN FETCH t.odontologo o " +
                     "LEFT JOIN FETCH o.persona " +
                     "WHERE p.id_paciente = :idPaciente")
       List<Turno> findByPacienteId(@Param("idPaciente") Long idPaciente);

       @Query("SELECT t FROM Turno t WHERE t.odontologo.id_odontologo = :idOdontologo")
       List<Turno> findByOdontologoId(@Param("idOdontologo") Long idOdontologo);

       List<Turno> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

       @Query("SELECT t FROM Turno t " +
                     "LEFT JOIN FETCH t.paciente p " +
                     "LEFT JOIN FETCH p.persona " +
                     "LEFT JOIN FETCH t.odontologo o " +
                     "LEFT JOIN FETCH o.persona " +
                     "WHERE t.odontologo.id_odontologo = :idOdontologo AND t.fechaHora BETWEEN :inicio AND :fin " +
                     "ORDER BY t.fechaHora ASC")
       List<Turno> findByOdontologoIdAndFechaHoraBetween(
                     @Param("idOdontologo") Long idOdontologo,
                     @Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT t FROM Turno t " +
                     "LEFT JOIN FETCH t.paciente p " +
                     "LEFT JOIN FETCH p.persona " +
                     "LEFT JOIN FETCH t.odontologo o " +
                     "LEFT JOIN FETCH o.persona " +
                     "WHERE t.id_turno = :idTurno")
       Optional<Turno> findByIdWithDetails(@Param("idTurno") Long idTurno);

       @Query("SELECT t FROM Turno t " +
                     "LEFT JOIN FETCH t.paciente p " +
                     "LEFT JOIN FETCH p.persona " +
                     "LEFT JOIN FETCH t.odontologo o " +
                     "LEFT JOIN FETCH o.persona " +
                     "WHERE t.estadoTurno = 'PROXIMO' " +
                     "ORDER BY t.fechaHora ASC")
       List<Turno> findProximosTurnos();

       @Query("SELECT t FROM Turno t " +
                     "LEFT JOIN FETCH t.paciente p " +
                     "LEFT JOIN FETCH p.persona " +
                     "LEFT JOIN FETCH t.odontologo o " +
                     "LEFT JOIN FETCH o.persona " +
                     "WHERE t.odontologo.id_odontologo = :odontologoId " +
                     "AND t.fechaHora BETWEEN :inicioRango AND :finRango ")
       List<Turno> findTurnosByMes(
                     @Param("odontologoId") Long odontologoId,
                     @Param("inicioRango") LocalDateTime inicioRango,
                     @Param("finRango") LocalDateTime finRango);

       @Query("SELECT COUNT(t) FROM Turno t "
                     + "WHERE t.odontologo.id_odontologo = :idOdontologo "
                     + "AND UPPER(t.estadoTurno) = UPPER(:estado)")
       long contarTurnosPorOdontologoYEstado(
                     @Param("idOdontologo") Long idOdontologo,
                     @Param("estado") String estado);

       @Query("SELECT COUNT(t) FROM Turno t "
                     + "WHERE t.paciente.id_paciente = :idPaciente "
                     + "AND UPPER(t.estadoTurno) = UPPER(:estado)")
       long contarTurnosPorPacienteYEstado(
                     @Param("idPaciente") Long idPaciente,
                     @Param("estado") String estado);

       @Query("SELECT COUNT(t) FROM Turno t "
                     + "WHERE UPPER(t.estadoTurno) = UPPER(:estado)")
       long countByEstado(@Param("estado") String estado);

       @Query("SELECT t.paciente.id_paciente, t.odontologo.id_odontologo, COUNT(t) FROM Turno t "
                     + "GROUP BY t.paciente.id_paciente, t.odontologo.id_odontologo")
       List<Object[]> contarTurnosPorPaciente();

       @Query("SELECT COUNT(t) FROM Turno t "
                     + "WHERE t.odontologo.id_odontologo = :idOdontologo "
                     + "AND UPPER(t.estadoTurno) = 'PENDIENTE' "
                     + "AND t.fechaHora BETWEEN :inicio AND :fin")
       long contarTurnosPendientesPorOdontologoYFecha(
                     @Param("idOdontologo") Long idOdontologo,
                     @Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT new com.clinica.clinica_coc.dashboard.TurnosPorObraSocialDTO("
                     + "COALESCE(c.nombre_cobertura, 'Sin cobertura'), COUNT(t)) "
                     + "FROM Turno t "
                     + "JOIN t.paciente p "
                     + "LEFT JOIN p.coberturas c "
                     + "GROUP BY COALESCE(c.nombre_cobertura, 'Sin cobertura') "
                     + "ORDER BY COALESCE(c.nombre_cobertura, 'Sin cobertura')")
       List<TurnosPorObraSocialDTO> contarTurnosPorCobertura();
}