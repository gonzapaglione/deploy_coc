package com.clinica.clinica_coc.services;

import com.clinica.clinica_coc.DTO.HorarioRequest;
import com.clinica.clinica_coc.DTO.HorarioResponse;
import com.clinica.clinica_coc.models.Horario;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.repositories.HorarioRepositorio;
import com.clinica.clinica_coc.repositories.OdontologoRepositorio;
import com.clinica.clinica_coc.exceptions.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorarioServicio {

    @Autowired
    private HorarioRepositorio horarioRepositorio;
    
    @Autowired
    private OdontologoRepositorio odontologoRepositorio; 

    /**
     * Busca todos los horarios de un odontólogo
     */
    @Transactional(readOnly = true)
    public List<HorarioResponse> getHorariosPorOdontologo(Long idOdontologo) {
        List<Horario> horarios = horarioRepositorio.findHorariosPorOdontologo(idOdontologo);
        return horarios.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo horario
     */
    @Transactional
    public HorarioResponse crearHorario(HorarioRequest request) {
        // 1. Buscar al odontólogo
        Odontologo odontologo = odontologoRepositorio.findById(request.getId_odontologo())
                .orElseThrow(() -> new ResourceNotFoundException("Odontologo no encontrado"));

        // 2. Mapear DTO a Entidad
        Horario horario = mapRequestToEntity(request);
        horario.setOdontologo(odontologo);

        // 3. Guardar
        Horario horarioGuardado = horarioRepositorio.save(horario);

        // 4. Mapear Entidad a DTO de respuesta y retornar
        return mapEntityToResponse(horarioGuardado);
    }

    /**
     * Actualiza un horario existente
     */
    @Transactional
    public HorarioResponse actualizarHorario(Long idHorario, HorarioRequest request) {
        // 1. Buscar el horario existente
        Horario horario = horarioRepositorio.findById(idHorario)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado"));

        // 2. (Opcional) Validar/re-asignar odontólogo si es necesario
        if (!horario.getOdontologo().getId_odontologo().equals(request.getId_odontologo())) {
             Odontologo nuevoOdontologo = odontologoRepositorio.findById(request.getId_odontologo())
                .orElseThrow(() -> new ResourceNotFoundException("Odontologo no encontrado"));
             horario.setOdontologo(nuevoOdontologo);
        }
        
        // 3. Actualizar los campos
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setDuracionTurno(request.getDuracionTurno());

        // 4. Guardar
        Horario horarioActualizado = horarioRepositorio.save(horario);
        
        // 5. Retornar DTO de respuesta
        return mapEntityToResponse(horarioActualizado);
    }

    /**
     * Elimina un horario
     */
    @Transactional
    public void eliminarHorario(Long idHorario) {
        if (!horarioRepositorio.existsById(idHorario)) {
            throw new ResourceNotFoundException("Horario no encontrado");
        }
        horarioRepositorio.deleteById(idHorario);
    }


    // --- MAPPERS PRIVADOS ---

    private HorarioResponse mapEntityToResponse(Horario horario) {
        return HorarioResponse.builder()
                .id_horario(horario.getIdHorario())
                .id_odontologo(horario.getOdontologo().getId_odontologo())
                .diaSemana(horario.getDiaSemana().name()) // Convertir Enum a String
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .duracionTurno(horario.getDuracionTurno())
                .build();
    }

    private Horario mapRequestToEntity(HorarioRequest request) {
        Horario horario = new Horario();
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setDuracionTurno(request.getDuracionTurno());
        // El odontólogo se asigna por separado en el método principal
        return horario;
    }
}