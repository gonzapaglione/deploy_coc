package com.clinica.clinica_coc.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.clinica.clinica_coc.DTO.CoberturaSocialDTO;
import com.clinica.clinica_coc.DTO.OdontologoResumidoDTO;
import com.clinica.clinica_coc.DTO.PacienteResumidoDTO;
import com.clinica.clinica_coc.DTO.TurnoRequest;
import com.clinica.clinica_coc.DTO.TurnoResponse;
import com.clinica.clinica_coc.exceptions.ResourceNotFoundException;
import com.clinica.clinica_coc.models.CoberturaSocial;
import com.clinica.clinica_coc.models.Horario;
import com.clinica.clinica_coc.models.MotivoConsultaEnum;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.models.Paciente;
import com.clinica.clinica_coc.models.Turno;
import com.clinica.clinica_coc.repositories.CoberturaSocialRepositorio;
import com.clinica.clinica_coc.repositories.HorarioRepositorio;
import com.clinica.clinica_coc.repositories.OdontologoRepositorio;
import com.clinica.clinica_coc.repositories.PacienteRepositorio;
import com.clinica.clinica_coc.repositories.TurnoRepositorio;
import com.clinica.clinica_coc.repositories.TurnoSpecification;

@Service
public class TurnoServicio {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private TurnoRepositorio turnoRepositorio;

    @Autowired
    private PacienteRepositorio pacienteRepositorio;

    @Autowired
    private OdontologoRepositorio odontologoRepositorio;

    @Autowired
    private HorarioRepositorio horarioRepositorio;

    @Autowired
    private CoberturaSocialRepositorio coberturaSocialRepositorio;

    @Transactional(readOnly = true)
    public List<TurnoResponse> listarTurnos() {
        return turnoRepositorio.findAll().stream()
                .map(this::mapTurnoToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> buscarTurnosConFiltros(
            String pacienteNombre,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<String> estados,
            Long odontologoId,
            String orden) {

        // 1. Construimos la especificación dinámica
        Specification<Turno> spec = TurnoSpecification.build(pacienteNombre, fechaInicio, fechaFin, estados,
                odontologoId);

        // 2. Definimos el orden
        Sort.Direction direction = "ASC".equalsIgnoreCase(orden)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "fechaHora");

        // 3. Ejecutamos la consulta
        List<Turno> turnos = turnoRepositorio.findAll(spec, sort);

        // 4. Mapeamos a DTO
        return turnos.stream()
                .map(this::mapTurnoToResponse)
                .collect(Collectors.toList());
    }

    public List<TurnoResponse> buscarTurnosPorMes(
            Long odontologoId,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        // Convierte fechas a rango LocalDateTime
        LocalDateTime inicioRango = fechaInicio.atStartOfDay();

        // (ej. 2025-10-31 -> 2025-10-31T23:59:59.999...)
        LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);

        // Llamar al repositorio
        List<Turno> turnos = turnoRepositorio.findTurnosByMes(
                odontologoId, inicioRango, finRango);
        return turnos.stream()
                .map(this::mapTurnoToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TurnoResponse obtenerTurno(Long idTurno) {
        Turno turno = turnoRepositorio.findById(idTurno)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + idTurno));
        return mapTurnoToResponse(turno);
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listarTurnosPorPaciente(Long idPaciente) {
        return turnoRepositorio.findByPacienteId(idPaciente).stream()
                .map(this::mapTurnoToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listarTurnosPorOdontologo(Long idOdontologo) {
        return turnoRepositorio.findByOdontologoId(idOdontologo).stream()
                .map(this::mapTurnoToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> obtenerHorariosDisponibles(Long idOdontologo, String fechaStr) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido. Usar yyyy-MM-dd");
        }
       ZoneId zonaClinica = ZoneId.of("America/Argentina/Buenos_Aires");

        // 2. Obtenemos la fecha y hora actuales para argentina (sino se adelanta 3 horas y no funciona el filtro)
        LocalDate hoy = LocalDate.now(zonaClinica);
        LocalTime ahora = LocalTime.now(zonaClinica);

      if (fecha.isBefore(hoy)) {
            return List.of(); 
        }

        boolean esHoy = fecha.isEqual(hoy);

        if (!odontologoRepositorio.existsById(idOdontologo)) {
            throw new ResourceNotFoundException("Odontólogo no encontrado con id: " + idOdontologo);
        }

        com.clinica.clinica_coc.models.DiaSemana diaEnum = convertirDiaJavaAEnum(fecha.getDayOfWeek());
        List<Horario> horariosDelDia = horarioRepositorio.findByOdontologoIdOdontologoAndDiaSemana(idOdontologo,
                diaEnum);

        if (horariosDelDia.isEmpty()) {
            return List.of();
        }

        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

        List<Turno> turnosDelDia = turnoRepositorio.findByOdontologoIdAndFechaHoraBetween(idOdontologo, inicioDelDia,
                finDelDia);

        Set<LocalTime> horariosReservados = turnosDelDia.stream()
                .filter(turno -> turnoCuentaComoOcupado(turno.getEstadoTurno()))
                .map(turno -> turno.getFechaHora().toLocalTime())
                .collect(Collectors.toCollection(HashSet::new));

        Set<LocalTime> horariosDisponibles = new HashSet<>();

        for (Horario horario : horariosDelDia) {
            Integer duracion = horario.getDuracionTurno();
            if (duracion == null || duracion <= 0) {
                continue;
            }

            LocalTime inicioSlot = horario.getHoraInicio();
            LocalTime ultimoInicio = horario.getHoraFin().minusMinutes(duracion);

            if (esHoy && ahora.isAfter(inicioSlot)) {

                LocalTime primerSlotValido = inicioSlot; 
                
                while (primerSlotValido.isBefore(ahora)) {
                    primerSlotValido = primerSlotValido.plusMinutes(duracion);
                }

                inicioSlot = primerSlotValido; 
            }

            while (!inicioSlot.isAfter(ultimoInicio)) {
                if (!horariosReservados.contains(inicioSlot)) {
                    horariosDisponibles.add(inicioSlot);
                }
                inicioSlot = inicioSlot.plusMinutes(duracion);
            }
        }

        return horariosDisponibles.stream()
                .sorted()
                .map(hora -> hora.format(HORA_FORMATTER))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listarTurnosPorOdontologoYFecha(Long idOdontologo, String fechaStr) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido. Usar yyyy-MM-dd");
        }

        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

        com.clinica.clinica_coc.models.DiaSemana diaEnum = convertirDiaJavaAEnum(fecha.getDayOfWeek());

        System.out.println("DEBUG: Buscando horarios para Odontólogo ID: " + idOdontologo + " en Día: " + diaEnum);

        List<Horario> horariosDelDia = horarioRepositorio.findByOdontologoIdOdontologoAndDiaSemana(idOdontologo,
                diaEnum);

        System.out.println("DEBUG: Horarios encontrados: " + horariosDelDia);
        // 3. Pasa la lista de horarios al método de mapeo
        return turnoRepositorio.findByOdontologoIdAndFechaHoraBetween(idOdontologo, inicioDelDia, finDelDia).stream()
                .map(turno -> mapTurnoToResponse(turno, horariosDelDia)) // <-- Pasamos la lista aquí
                .collect(Collectors.toList());
    }

    private com.clinica.clinica_coc.models.DiaSemana convertirDiaJavaAEnum(java.time.DayOfWeek diaJava) {
        switch (diaJava) {
            case MONDAY:
                return com.clinica.clinica_coc.models.DiaSemana.Lunes;
            case TUESDAY:
                return com.clinica.clinica_coc.models.DiaSemana.Martes;
            case WEDNESDAY:
                return com.clinica.clinica_coc.models.DiaSemana.Miércoles;
            case THURSDAY:
                return com.clinica.clinica_coc.models.DiaSemana.Jueves;
            case FRIDAY:
                return com.clinica.clinica_coc.models.DiaSemana.Viernes;
            case SATURDAY:
                return com.clinica.clinica_coc.models.DiaSemana.Sábado;
            case SUNDAY:
                return com.clinica.clinica_coc.models.DiaSemana.Domingo;
            default:
                throw new IllegalArgumentException("Día de la semana inválido: " + diaJava);
        }
    }

   @Transactional
    public TurnoResponse crearTurno(TurnoRequest request) {
        Paciente paciente = pacienteRepositorio.findById(request.getIdPaciente())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente no encontrado con id: " + request.getIdPaciente()));

        Odontologo odontologo = odontologoRepositorio.findById(request.getIdOdontologo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Odontólogo no encontrado con id: " + request.getIdOdontologo()));

        if (request.getFechaHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora del turno es obligatoria");
        }
        
       
        final Long ID_COBERTURA_PARTICULAR = 1L;
        
        CoberturaSocial cobertura; 
        if (request.getIdCobertura() == null) {
            
            cobertura = coberturaSocialRepositorio.findById(ID_COBERTURA_PARTICULAR)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "¡Error de Configuración! Cobertura 'Particular' (ID 1) no encontrada."));
            // (Como es particular, no validamos si el paciente la tiene)
        } else {
            cobertura = coberturaSocialRepositorio.findById(request.getIdCobertura()) //
                    .orElseThrow(() -> new ResourceNotFoundException( //
                            "Cobertura no encontrada con id: " + request.getIdCobertura())); //
            if(request.getIdCobertura()==1){ //
               
            }else{
             boolean pacienteTieneCobertura = paciente.getCoberturas().stream() //
                 .anyMatch(cob -> cob.getId_cob_social().equals(request.getIdCobertura())); //
             if (!pacienteTieneCobertura) { 
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El paciente no posee esa cobertura"); //
             }} 
        }

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setOdontologo(odontologo);
        turno.setCoberturaSocial(cobertura); 
        turno.setFechaHora(request.getFechaHora());
        turno.setEstadoTurno(validarEstado(request.getEstadoTurno()));
        turno.setMotivoConsulta(request.getMotivoConsulta());
        turno.setTratamiento(request.getTratamiento());
        turno.setEvolucion(request.getEvolucion());

        Turno turnoGuardado = turnoRepositorio.save(turno);
        return mapTurnoToResponse(turnoGuardado);
    }
    @Transactional
    public TurnoResponse actualizarTurno(Long idTurno, TurnoRequest request) {
        Turno turno = turnoRepositorio.findById(idTurno)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + idTurno));

        if (request.getIdPaciente() != null
                && !turno.getPaciente().getId_paciente().equals(request.getIdPaciente())) {
            Paciente nuevoPaciente = pacienteRepositorio.findById(request.getIdPaciente())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Paciente no encontrado con id: " + request.getIdPaciente()));
            turno.setPaciente(nuevoPaciente);
        }

        if (request.getIdOdontologo() != null
                && !turno.getOdontologo().getId_odontologo().equals(request.getIdOdontologo())) {
            Odontologo nuevoOdontologo = odontologoRepositorio.findById(request.getIdOdontologo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Odontólogo no encontrado con id: " + request.getIdOdontologo()));
            turno.setOdontologo(nuevoOdontologo);
        }

        if (request.getFechaHora() != null) {
            turno.setFechaHora(request.getFechaHora());
        }

        if (request.getEstadoTurno() != null) {
            turno.setEstadoTurno(validarEstado(request.getEstadoTurno()));
        }

        turno.setMotivoConsulta(request.getMotivoConsulta());
        turno.setTratamiento(request.getTratamiento());
        turno.setEvolucion(request.getEvolucion());

        Turno turnoActualizado = turnoRepositorio.save(turno);
        return mapTurnoToResponse(turnoActualizado);
    }

    @Transactional
    public void eliminarTurno(Long idTurno) {
        if (!turnoRepositorio.existsById(idTurno)) {
            throw new ResourceNotFoundException("Turno no encontrado con id: " + idTurno);
        }
        turnoRepositorio.deleteById(idTurno);
    }

    private String validarEstado(String estado) {
        String estadoNormalizado = estado == null ? "Pendiente" : estado.trim();
        if (estadoNormalizado.isEmpty()) {
            estadoNormalizado = "Pendiente";
        }
        if (estadoNormalizado.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado del turno no puede superar los 20 caracteres");
        }
        return estadoNormalizado;
    }

    private TurnoResponse mapTurnoToResponse(Turno turno) {
        if (turno == null) {
            return null;
        }

        List<Horario> horariosDelOdontologo;
        Odontologo odontologo = turno.getOdontologo();

        if (odontologo != null && turno.getFechaHora() != null) {
            // 1. Obtenemos la fecha y el día ENUM
            LocalDate fecha = turno.getFechaHora().toLocalDate();
            com.clinica.clinica_coc.models.DiaSemana diaEnum = convertirDiaJavaAEnum(fecha.getDayOfWeek());

            // 2. Buscamos los horarios para ESE día
            horariosDelOdontologo = horarioRepositorio.findByOdontologoIdOdontologoAndDiaSemana(
                    odontologo.getId_odontologo(),
                    diaEnum);
        } else {
            // Si no hay odontólogo o fecha, pasamos una lista vacía
            horariosDelOdontologo = java.util.Collections.emptyList();
        }

        return mapTurnoToResponse(turno, horariosDelOdontologo);
    }

    private TurnoResponse mapTurnoToResponse(Turno turno, List<Horario> horariosDelOdontologo) {
        Paciente paciente = turno.getPaciente();
        Odontologo odontologo = turno.getOdontologo();
        CoberturaSocial cobertura = turno.getCoberturaSocial();

        PacienteResumidoDTO pacienteDTO = null;
        if (paciente != null) {
            pacienteDTO = PacienteResumidoDTO.builder()
                    .id_paciente(paciente.getId_paciente())
                    .nombre(paciente.getPersona() != null ? paciente.getPersona().getNombre() : "N/A")
                    .apellido(paciente.getPersona() != null ? paciente.getPersona().getApellido() : "")
                    .build();
        }

        OdontologoResumidoDTO odontologoDTO = null;
        if (odontologo != null) {
            odontologoDTO = OdontologoResumidoDTO.builder()
                    .id_odontologo(odontologo.getId_odontologo())
                    .nombre(odontologo.getPersona() != null ? odontologo.getPersona().getNombre() : "N/A")
                    .apellido(odontologo.getPersona() != null ? odontologo.getPersona().getApellido() : "")
                    .build();
        }

        CoberturaSocialDTO coberturaDTO = null;
        if (cobertura != null) {
            coberturaDTO = new CoberturaSocialDTO(
                cobertura.getId_cob_social(),
                cobertura.getNombre_cobertura(),
                cobertura.getEstado_cobertura()
            );
        }

        LocalDateTime fechaHoraInicio = turno.getFechaHora();
        LocalTime horaInicioTurno = fechaHoraInicio.toLocalTime();

        System.out.println("--- INICIO MAPEO TURNO ID: " + turno.getId_turno() + " ---");
        System.out.println("DEBUG: Hora del turno leída: " + horaInicioTurno);
        System.out.println("DEBUG: Horarios disponibles para filtrar (" + horariosDelOdontologo.size() + "):");
        horariosDelOdontologo.forEach(h -> {
            System.out.println("  -> Horario: " + h.getHoraInicio() + " a " + h.getHoraFin() + " (Dur: "
                    + h.getDuracionTurno() + ")");
        });

        long duracionMinutos = 30;

        if (horariosDelOdontologo != null) {
            Optional<Horario> horarioContenedor = horariosDelOdontologo.stream()
                    .filter(h -> !h.getHoraInicio().isAfter(horaInicioTurno) && h.getHoraFin().isAfter(horaInicioTurno))
                    .findFirst();

            if (horarioContenedor.isPresent()) {
                System.out.println("DEBUG: ¡ÉXITO! Turno contenido en el bloque: "
                        + horarioContenedor.get().getHoraInicio() + " - " + horarioContenedor.get().getHoraFin());
            } else {
                System.out.println("DEBUG: ¡FALLO! El turno NO está contenido en ningún bloque de horario.");
            }

            if (horarioContenedor.isPresent()) {
                duracionMinutos = horarioContenedor.get().getDuracionTurno();
            }
        }
        System.out.println("DEBUG: Duración asignada: " + duracionMinutos);
        System.out.println("--- FIN MAPEO ---");

        MotivoConsultaEnum motivoEnum = turno.getMotivoConsulta();
        String motivoDescripcion = (motivoEnum != null) ? motivoEnum.getDescripcion() : null;

        LocalDateTime fechaHoraFin = fechaHoraInicio.plusMinutes(duracionMinutos);
        return TurnoResponse.builder()
                .id_turno(turno.getId_turno())
                .paciente(pacienteDTO)
                .odontologo(odontologoDTO)
                .cobertura(coberturaDTO)
                .motivoConsulta(motivoDescripcion)
                .estadoTurno(turno.getEstadoTurno())
                .fecha(fechaHoraInicio.toLocalDate())
                .horaInicio(fechaHoraInicio.toLocalTime())
                .horaFin(fechaHoraFin.toLocalTime())
                .tratamiento(turno.getTratamiento())
                .evolucion(turno.getEvolucion())
                .build();
    }

    private boolean turnoCuentaComoOcupado(String estadoTurno) {
        if (estadoTurno == null) {
            return true;
        }
        String normalizado = estadoTurno.trim().toLowerCase();
        return !normalizado.startsWith("cancelado");
    }

    @Transactional
    public TurnoResponse actualizarTurnoParcial(Long idTurno, Map<String, Object> campos) {
        Turno turno = turnoRepositorio.findById(idTurno)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + idTurno));

        // Iteramos sobre los campos que llegaron en el JSON
        campos.forEach((key, value) -> {
            switch (key) {
                case "estadoTurno":
                    turno.setEstadoTurno((String) value);
                    break;
                case "tratamiento":
                    turno.setTratamiento((String) value);
                    break;
                case "evolucion":
                    turno.setEvolucion((String) value);
                    break;
                case "motivoConsulta":
                    if (value != null) {
                        turno.setMotivoConsulta(MotivoConsultaEnum.valueOf((String) value));
                    } else {
                        turno.setMotivoConsulta(null);
                    }
                    break;
                case "fechaHora":
                    turno.setFechaHora(LocalDateTime.parse((String) value));
                    break;
            }
        });

        Turno turnoActualizado = turnoRepositorio.save(turno);
        return mapTurnoToResponse(turnoActualizado);
    }

    public List<TurnoResponse> listarHistoriaParaOdontologo(Long idPaciente, Authentication authentication) {
        // 1. Obtener el ID del Odontólogo logueado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String EmailPersonaLogueada = userDetails.getUsername();
        Odontologo odontologo = odontologoRepositorio.findByEmail(EmailPersonaLogueada);
               
        Long idOdontologoLogueado = odontologo.getId_odontologo();

        // 2. Obtener TODOS los turnos del paciente
        List<Turno> todosLosTurnos = turnoRepositorio.findByPacienteId(idPaciente);

        // 3. Procesar la lista
        return todosLosTurnos.stream()
            .filter(turno -> {
                // Si el turno es "PROXIMO", solo incluirlo si pertenece al odontólogo logueado
                if (turno.getEstadoTurno().equals("PROXIMO")) {
                    return turno.getOdontologo().getId_odontologo().equals(idOdontologoLogueado);
                }
                return true;
            })
            .map(turno -> {
                TurnoResponse dto = mapTurnoToResponse(turno);
                
                // Si el turno NO es del odontólogo logueado
                if (!turno.getOdontologo().getId_odontologo().equals(idOdontologoLogueado)) {
                    // Y NO es un turno próximo (doble chequeo por si acaso)
                    if (turno.getEstadoTurno() != "PROXIMO") {
                        OdontologoResumidoDTO odAnonimo = new OdontologoResumidoDTO();
                        odAnonimo.setNombre("Otro colega");
                        
                        dto.setOdontologo(odAnonimo);
                    }
                }
                return dto;
            })
            .collect(Collectors.toList());
    }
}
