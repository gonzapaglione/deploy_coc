package com.clinica.clinica_coc.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinica.clinica_coc.models.Turno;

@Repository
public interface DashboardRepositorio extends org.springframework.data.repository.Repository<Turno, Long> {

  // Cantidad de personas activas
  @Query("SELECT COUNT(p) FROM Persona p WHERE UPPER(p.isActive) = 'ACTIVO'")
  int contarPersonasActivas();

  // Cantidad de pacientes activos
  @Query("SELECT COUNT(p) FROM Paciente p WHERE UPPER(p.estado_paciente) = 'ACTIVO'")
  int contarPacientesActivos();

  // Cantidad de odontólogos activos
  @Query("SELECT COUNT(o) FROM Odontologo o WHERE UPPER(o.estado_odont) = 'ACTIVO'")
  int contarOdontologosActivos();

  @Query("""
      SELECT t.odontologo.id_odontologo, COUNT(t)
      FROM Turno t
      WHERE UPPER(t.estadoTurno) = 'ATENDIDO'
      GROUP BY t.odontologo.id_odontologo
      ORDER BY COUNT(t) DESC
      """)
  List<Object[]> odontologoConMasTurnos();

  @Query("""
      SELECT t.paciente.id_paciente, COUNT(t)
      FROM Turno t
      WHERE UPPER(t.estadoTurno) = 'ATENDIDO'
      GROUP BY t.paciente.id_paciente
      ORDER BY COUNT(t) DESC
      """)
  List<Object[]> pacienteConMasTurnos();

  @Query("SELECT COUNT(t) FROM Turno t WHERE UPPER(t.estadoTurno) = UPPER(:estado)")
  int contarTurnosPorEstado(@Param("estado") String estado);

  @Query("SELECT COUNT(t) FROM Turno t WHERE t.odontologo.id_odontologo = :id AND UPPER(t.estadoTurno) = UPPER(:estado)")
  long contarTurnosPorOdontologoYEstado(@Param("id") Long idOdontologo, @Param("estado") String estado);

  @Query("""
      SELECT COUNT(t)
      FROM Turno t
      WHERE t.odontologo.id_odontologo = :id
        AND UPPER(t.estadoTurno) = 'PROXIMO'
        AND t.fechaHora BETWEEN :inicio AND :fin
      """)
  long contarTurnosPendientesPorOdontologoYFecha(
      @Param("id") Long idOdontologo,
      @Param("inicio") LocalDateTime inicio,
      @Param("fin") LocalDateTime fin);

  @Query("""
      SELECT t.paciente.id_paciente, t.odontologo.id_odontologo, COUNT(t)
      FROM Turno t
      WHERE UPPER(t.estadoTurno) = 'ATENDIDO'
      GROUP BY t.paciente.id_paciente, t.odontologo.id_odontologo
      ORDER BY COUNT(t) DESC
      """)
  List<Object[]> odontologoFrecuentePorPaciente();

  @Query("""
      SELECT COUNT(t)
      FROM Turno t
      WHERE t.paciente.id_paciente = :idPaciente
        AND UPPER(t.estadoTurno) = UPPER(:estado)
      """)
  long contarTurnosPorPacienteYEstado(@Param("idPaciente") Long idPaciente, @Param("estado") String estado);

  @Query("""
      SELECT t.motivoConsulta, COUNT(t)
      FROM Turno t
      WHERE t.odontologo.id_odontologo = :idOdontologo
        AND t.motivoConsulta IS NOT NULL
        AND UPPER(t.estadoTurno) = 'ATENDIDO'
      GROUP BY t.motivoConsulta
      ORDER BY COUNT(t) DESC
      """)
  List<Object[]> practicaMasSolicitadaPorOdontologo(@Param("idOdontologo") Long idOdontologo);

 @Query("""
      SELECT new com.clinica.clinica_coc.dashboard.TurnosPorObraSocialDTO(
        COALESCE(c.nombre_cobertura, 'Sin cobertura'),
        COUNT(t) AS totalTurnos
      )
      FROM Turno t
      LEFT JOIN t.coberturaSocial c
      GROUP BY COALESCE(c.nombre_cobertura, 'Sin cobertura')
      ORDER BY totalTurnos DESC
      """)
  List<TurnosPorObraSocialDTO> contarTurnosPorCobertura();
}
