package com.clinica.clinica_coc.controllers;

import com.clinica.clinica_coc.DTO.AsignarPacienteRequest;
import com.clinica.clinica_coc.DTO.BajaResponse;
import com.clinica.clinica_coc.DTO.PacienteRequest;
import com.clinica.clinica_coc.DTO.PacienteResponse;
import com.clinica.clinica_coc.DTO.PersonaBasicDTO;
import com.clinica.clinica_coc.DTO.CoberturaSocialDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.clinica.clinica_coc.services.PacienteServicio;
import com.clinica.clinica_coc.models.Paciente;
import com.clinica.clinica_coc.models.Persona;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin("http://localhost:5173")
public class PacienteController {

    @Autowired
    private PacienteServicio pacienteServicio;

    // GET: listar todos
    @GetMapping()
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_PACIENTES', 'PERM_GESTIONAR_TURNOS_ADMIN', 'PERM_GESTIONAR_TURNOS_OD')")
    public ResponseEntity<List<PacienteResponse>> listarPacientes() {
        List<Paciente> pacientes = pacienteServicio.listarPacientes();

        if (pacientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<PacienteResponse> response = pacientes.stream()
                .map(this::convertirAResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // GET: listar por id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_PACIENTES', 'PERM_GESTIONAR_TURNOS_ADMIN', 'PERM_GESTIONAR_TURNOS_OD', 'PERM_VER_MI_PERFIL_PACIENTE')")
    public ResponseEntity<PacienteResponse> listarPacientePorId(@PathVariable Long id) {
        Paciente paciente = pacienteServicio.buscarPacientePorId(id);

        if (paciente == null) {
            return ResponseEntity.notFound().build();
        }

        PacienteResponse response = convertirAResponse(paciente);
        return ResponseEntity.ok(response);
    }

    // GET: Ver perfil de paciente (solo el propio paciente)
    @GetMapping("/persona/{idPersona}")
    @PreAuthorize("hasAnyAuthority('PERM_VER_MI_PERFIL_PACIENTE', 'PERM_EDITAR_PERFIL_PACIENTE')")
    public ResponseEntity<PacienteResponse> listarPacientePorIdPersona(@PathVariable Long idPersona) {
        Paciente paciente = pacienteServicio.buscarPacientePorIdPersona(idPersona);

        if (paciente == null) {
            return ResponseEntity.notFound().build(); // Devuelve 404 si no existe
        }

        PacienteResponse response = convertirAResponse(paciente);
        return ResponseEntity.ok(response);
    }

    // POST: agregar paciente
    @PostMapping()
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_PACIENTES')") 
    public ResponseEntity<PacienteResponse> agregarPaciente(@RequestBody PacienteRequest request) {

        System.out.println("Persona a agregar: " + request.getPersona());
        Paciente pacienteGuardado = pacienteServicio.crearPacienteConPersonaYRol(
                request.getPersona(),
                request.getCoberturasIds(),
                request.getEstado_paciente());

        PacienteResponse response = convertirAResponse(pacienteGuardado);
        return ResponseEntity.status(201).body(response);
    }

    // PUT: editar paciente
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_PACIENTES', 'PERM_EDITAR_PERFIL_PACIENTE')") 
    public ResponseEntity<PacienteResponse> editarPaciente(
            @PathVariable Long id,
            @RequestBody PacienteRequest request) {

        Paciente pacienteEditado = pacienteServicio.editarPaciente(id, request);

        if (pacienteEditado == null) {
            return ResponseEntity.notFound().build();
        }

        PacienteResponse response = convertirAResponse(pacienteEditado);
        return ResponseEntity.ok(response);
    }

    // DELETE: baja logica paciente
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_PACIENTES')") 
    public ResponseEntity<BajaResponse> bajaLogicaPaciente(@PathVariable Long id) {
        Paciente pacienteActualizado = pacienteServicio.bajaLogicaPaciente(id);
        if (pacienteActualizado == null) {
            return ResponseEntity.notFound().build();
        }

        PacienteResponse response = convertirAResponse(pacienteActualizado);

        BajaResponse bajaResponse = new BajaResponse(
                "Paciente dado de baja lógicamente",
                response);

        return ResponseEntity.ok(bajaResponse);
    }

    // Método auxiliar: convertir Paciente a PacienteResponse
    private PacienteResponse convertirAResponse(Paciente paciente) {
        PersonaBasicDTO personaDTO = convertirPersonaABasicDTO(paciente.getPersona());

        List<CoberturaSocialDTO> coberturasDTO = paciente.getCoberturas() != null
                ? paciente.getCoberturas().stream()
                        .map(c -> new CoberturaSocialDTO(c.getId_cob_social(), c.getNombre_cobertura(), c.getEstado_cobertura()))
                        .toList()
                : List.of();

        return new PacienteResponse(
                paciente.getId_paciente(),
                personaDTO,
                paciente.getEstado_paciente(),
                coberturasDTO);
    }

    // Método auxiliar para convertir Persona a PersonaBasicDTO (sin roles)
    private PersonaBasicDTO convertirPersonaABasicDTO(Persona persona) {
        return new PersonaBasicDTO(
                persona.getId_persona(),
                persona.getNombre(),
                persona.getApellido(),
                persona.getDni(),
                persona.getEmail(),
                persona.getDomicilio(),
                persona.getTelefono(),
                persona.getIsActive());
    }

    @PostMapping("/asignar")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_PACIENTES', 'PERM_GESTIONAR_PERSONAS')") 
    public ResponseEntity<?> asignarRolPaciente(@RequestBody AsignarPacienteRequest request) {
        try {
            Paciente nuevoPaciente = pacienteServicio.asignarRolPaciente(
                    request.getIdPersona(),
                    request.getCoberturasIds());

            PacienteResponse dto = convertirAResponse(nuevoPaciente); 
            return ResponseEntity.status(201).body(dto);
        
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mis-pacientes")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_TURNOS_OD')")
    public ResponseEntity<List<PacienteResponse>> getMisPacientes(Authentication authentication) { // <-- 1. Cambia el tipo de retorno

        List<Paciente> pacientes = pacienteServicio.listarPacientesPorOdontologoLogueado(authentication);
        
        if (pacientes == null || pacientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
   
        List<PacienteResponse> response = pacientes.stream()
                .map(this::convertirAResponse)
                .toList();

        return ResponseEntity.ok(response); 
    }
}
