package com.clinica.clinica_coc.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.clinica.clinica_coc.repositories.OdontologoRepositorio;
import com.clinica.clinica_coc.repositories.PacienteRepositorio;
import com.clinica.clinica_coc.repositories.PersonaRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.clinica.clinica_coc.exceptions.ResourceNotFoundException;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.models.Paciente;
import com.clinica.clinica_coc.models.Persona;

@Service
public class DashboardServicio {

    private static final String ESTADO_ATENDIDO = "ATENDIDO";
    private static final String ESTADO_CANCELADO = "CANCELADO";
    private static final String ESTADO_SIN_ASISTIR = "SIN_ASISTIR";

    @Autowired
    private DashboardRepositorio dashboardRepositorio;
    @Autowired
    private OdontologoRepositorio odontologoRepositorio;
    @Autowired
    private PacienteRepositorio pacienteRepositorio;
    @Autowired
    private PersonaRepositorio personaRepositorio;

    // DASHBOARD De Admin

    // Metodo para contar la cantidad de usuarios activos
    @Transactional(readOnly = true)
    public int contarUsuariosActivos() {
        return dashboardRepositorio.contarPersonasActivas();
    }

    // Metodo para contar la cantidad de pacientes activos
    @Transactional(readOnly = true)
    public int contarPacientesActivos() {
        return dashboardRepositorio.contarPacientesActivos();
    }

    // Metodo para contar la cantidad de odontologos activos
    @Transactional(readOnly = true)
    public int contarOdontologosActivos() {
        return dashboardRepositorio.contarOdontologosActivos();
    }

    // Este metodo listar todos los odontologos y devuelve el nombre completo
    // del que
    // mas turnos atendio

    public String odontologoDestacado() {
        List<Object[]> ranking = dashboardRepositorio.odontologoConMasTurnos();

        if (ranking == null || ranking.isEmpty()) {
            return "No hay odontólogos disponibles";
        }

        Object[] primerRegistro = ranking.get(0);
        if (primerRegistro == null || primerRegistro.length < 2) {
            return "No hay odontólogos disponibles";
        }

        Long odontologoId = (Long) primerRegistro[0];
        if (odontologoId == null) {
            return "No hay odontólogos disponibles";
        }

        return odontologoRepositorio.findById(odontologoId)
                .map(odontologo -> odontologo.getPersona().getNombre() + " "
                        + odontologo.getPersona().getApellido())
                .orElse("No hay odontólogos disponibles");
    }

    // Este metodo lista todos los pacientes y devuelve el nombre completo
    // del que
    // mas turnos asistio

    @Transactional(readOnly = true)
    public Paciente buscarPacienteDestacado() {
        List<Object[]> ranking = dashboardRepositorio.pacienteConMasTurnos();

        if (ranking == null || ranking.isEmpty()) {
            return null;
        }

        Object[] primerRegistro = ranking.get(0);
        if (primerRegistro == null || primerRegistro.length < 2) {
            return null;
        }

        Long pacienteId = (Long) primerRegistro[0];
        if (pacienteId == null) {
            return null;
        }

        return pacienteRepositorio.findById(pacienteId).orElse(null);
    }

    @Transactional(readOnly = true)
    public String pacienteDestacado() {
        Paciente pacienteDestacado = buscarPacienteDestacado();

        if (pacienteDestacado == null || pacienteDestacado.getPersona() == null) {
            return "No hay pacientes registrados.";
        }

        return pacienteDestacado.getPersona().getNombre().concat(" " + pacienteDestacado.getPersona().getApellido());
    }

    @Transactional(readOnly = true)
    public int turnosAtendidos() {
        return dashboardRepositorio.contarTurnosPorEstado(ESTADO_ATENDIDO);
    }

    @Transactional(readOnly = true)
    public int turnosCancelados() {
        return dashboardRepositorio.contarTurnosPorEstado(ESTADO_CANCELADO);
    }

    @Transactional(readOnly = true)
    public int turnosSinAsistir() {
        return dashboardRepositorio.contarTurnosPorEstado(ESTADO_SIN_ASISTIR);
    }


   @Transactional(readOnly = true)
    public DashboardAdminDTO obtenerDashboardAdmin() {
        
        // 1. Obtenemos los conteos que ya tenías
        int atendidos = turnosAtendidos();
        int cancelados = turnosCancelados();
        int sinAsistir = turnosSinAsistir();

        // 2. Calculamos el total y los porcentajes
        int totalTurnosBarra = atendidos + cancelados + sinAsistir;
        int porcAtendidos = 0;
        int porcCancelados = 0;
        int porcSinAsistir = 0;

        if (totalTurnosBarra > 0) {
            // Calculamos los dos primeros
            porcAtendidos = (int) Math.round(((double) atendidos / totalTurnosBarra) * 100);
            porcCancelados = (int) Math.round(((double) cancelados / totalTurnosBarra) * 100);
            
            // El tercero es 100 menos los otros para que la suma siempre sea 100 (evita errores de redondeo)
            porcSinAsistir = 100 - porcAtendidos - porcCancelados;

            // Caso especial: si los dos primeros redondean a 0 pero "sinAsistir" no es 0
            if (atendidos == 0 && cancelados == 0 && sinAsistir > 0) {
                porcSinAsistir = 100;
            }
        }
        
        // 3. Devolvemos el DTO con los nuevos campos
        return new DashboardAdminDTO(
                contarUsuariosActivos(),
                contarPacientesActivos(),
                contarOdontologosActivos(),
                odontologoDestacado(),
                pacienteDestacado(),
                atendidos, 
                cancelados, 
                sinAsistir, 
                porcAtendidos, 
                porcCancelados, 
                porcSinAsistir  
        );
    }

    // DASHBOARD De Odontologo

    @Transactional(readOnly = true)
    public long contarTurnosAtendidosPorOdontologo(Long idOdontologo) {
        if (idOdontologo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id de odontólogo es obligatorio");
        }
        if (!odontologoRepositorio.existsById(idOdontologo)) {
            throw new ResourceNotFoundException("Odontólogo no encontrado con id: " + idOdontologo);
        }
        return dashboardRepositorio.contarTurnosPorOdontologoYEstado(idOdontologo, ESTADO_ATENDIDO);
    }

    @Transactional(readOnly = true)
    public long contarTurnosPendientesPorOdontologoEnFecha(Long idOdontologo, LocalDate fecha) {
        if (idOdontologo == null || fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El id de odontólogo y la fecha son obligatorios");
        }
        if (!odontologoRepositorio.existsById(idOdontologo)) {
            throw new ResourceNotFoundException("Odontólogo no encontrado con id: " + idOdontologo);
        }

        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);
        return dashboardRepositorio.contarTurnosPendientesPorOdontologoYFecha(idOdontologo, inicioDelDia, finDelDia);
    }

    @Transactional(readOnly = true)
    public List<TurnosPorObraSocialDTO> obtenerTurnosPorCobertura() {
        return dashboardRepositorio.contarTurnosPorCobertura();
    }

    // DASHBOARD De Paciente

    // Metodo para contar la cantidad de turnos mios(Siendo paciente)
    @Transactional(readOnly = true)
    public int contarMisTurnos(Long idPaciente) {
        if (idPaciente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id de paciente es obligatorio");
        }
        if (!pacienteRepositorio.existsById(idPaciente)) {
            throw new ResourceNotFoundException("Paciente no encontrado con id: " + idPaciente);
        }
        return (int) dashboardRepositorio.contarTurnosPorPacienteYEstado(idPaciente, ESTADO_ATENDIDO);
    }

    // Metodo que devuelve el nombre del odontologo con el que mas me atendi (Siendo
    // paciente)
    @Transactional(readOnly = true)
    public String odontologoFrecuentePorPaciente(Long idPaciente) {
        if (idPaciente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id de paciente es obligatorio");
        }
        if (!pacienteRepositorio.existsById(idPaciente)) {
            throw new ResourceNotFoundException("Paciente no encontrado con id: " + idPaciente);
        }

        List<Object[]> resultados = dashboardRepositorio.odontologoFrecuentePorPaciente();

        Long odontologoFrecuenteId = null;
        long maxTurnos = 0;

        for (Object[] resultado : resultados) {
            if (resultado == null || resultado.length < 3) {
                continue;
            }

            Long pacienteId = (Long) resultado[0];
            Long odontologoId = (Long) resultado[1];
            Long cantidadTurnos = (Long) resultado[2];

            if (pacienteId != null && pacienteId.equals(idPaciente) && cantidadTurnos != null
                    && cantidadTurnos > maxTurnos) {
                maxTurnos = cantidadTurnos;
                odontologoFrecuenteId = odontologoId;
            }
        }

        if (odontologoFrecuenteId != null) {
            final Long idSeleccionado = odontologoFrecuenteId;
            Odontologo odontologoFrecuente = odontologoRepositorio.findById(idSeleccionado)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Odontólogo no encontrado con id: " + idSeleccionado));
            return odontologoFrecuente.getPersona().getNombre() + " " + odontologoFrecuente.getPersona().getApellido();
        } else {
            return "No se encontraron turnos para este paciente.";
        }
    }

    @Transactional(readOnly = true)
    public DashboardPacienteDTO obtenerDashboardPacientePorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es obligatorio");
        }

        Persona persona = personaRepositorio.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con email: " + email));

        Paciente paciente = pacienteRepositorio.findByPersonaId(persona.getId_persona())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente no encontrado para persona con id: " + persona.getId_persona()));

        int totalTurnos = contarMisTurnos(paciente.getId_paciente());
        String odontologoMasVisitado = odontologoFrecuentePorPaciente(paciente.getId_paciente());

        return new DashboardPacienteDTO(totalTurnos, odontologoMasVisitado);
    }

    @Transactional(readOnly = true)
    public DashboardOdontDTO obtenerDashboardOdontologoPorEmail(String email, LocalDate fecha) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es obligatorio");
        }

        Persona persona = personaRepositorio.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con email: " + email));

        Odontologo odontologo = odontologoRepositorio.findByPersonaId(persona.getId_persona())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Odontólogo no encontrado para persona con id: " + persona.getId_persona()));

        LocalDate fechaObjetivo = fecha != null ? fecha : LocalDate.now();

        int turnosCompletados = (int) contarTurnosAtendidosPorOdontologo(odontologo.getId_odontologo());
        int turnosPendientes = (int) contarTurnosPendientesPorOdontologoEnFecha(odontologo.getId_odontologo(),
                fechaObjetivo);
        String practicaMasRealizada = obtenerPracticaMasRealizada(odontologo.getId_odontologo());

        return new DashboardOdontDTO(turnosCompletados, turnosPendientes, practicaMasRealizada);
    }

    private String obtenerPracticaMasRealizada(Long idOdontologo) {
        List<Object[]> practicas = dashboardRepositorio.practicaMasSolicitadaPorOdontologo(idOdontologo);

        if (practicas == null || practicas.isEmpty()) {
            return "Sin información";
        }

        Object[] registro = practicas.get(0);
        if (registro == null || registro.length < 2 || registro[0] == null) {
            return "Sin información";
        }

        return registro[0].toString();
    }

}
