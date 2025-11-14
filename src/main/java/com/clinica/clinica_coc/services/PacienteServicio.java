package com.clinica.clinica_coc.services;

import com.clinica.clinica_coc.DTO.PacienteRequest;
import com.clinica.clinica_coc.DTO.PersonaRequest;
import com.clinica.clinica_coc.models.CoberturaSocial;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.models.Paciente;
import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.models.PersonaRol;
import com.clinica.clinica_coc.models.Rol;
import com.clinica.clinica_coc.repositories.CoberturaSocialRepositorio;
import com.clinica.clinica_coc.repositories.PacienteRepositorio;
import com.clinica.clinica_coc.repositories.PersonaRepositorio;
import com.clinica.clinica_coc.repositories.RolRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PacienteServicio implements IPacienteServicio {

    @Autowired
    private PacienteRepositorio pacienteRepositorio;

    @Autowired
    private PersonaRepositorio personaRepositorio;

    @Autowired
    private com.clinica.clinica_coc.repositories.PersonaRolRepositorio personaRolRepositorio;

    @Autowired
    private PersonaServicio personaServicio;

    @Autowired
    private CoberturaSocialServicio coberturaServicio;

    @Autowired
    private CoberturaSocialRepositorio coberturaSocialRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonaRolServicio personaRolServicio;

    @Autowired
    private RolRepositorio rolRepositorio; 

    @Autowired
    private OdontologoServicio odontologoServicio;

    @Override
    public List<Paciente> listarPacientes() {
        return pacienteRepositorio.findAll();
    }

    @Override
    public Paciente buscarPacientePorId(Long id) {
        return pacienteRepositorio.findById(id).orElse(null);
    }

    @Override
    public Paciente guardarPaciente(Paciente paciente) {
        if (paciente.getPersona() == null || paciente.getPersona().getId_persona() == null) {
            throw new RuntimeException("Debe asignarse una persona existente al paciente");
        }
        return pacienteRepositorio.save(paciente);
    }

    @Override
    public void eliminarPaciente(Paciente paciente) {
        pacienteRepositorio.delete(paciente);
    }

    @Transactional
    public Paciente bajaLogicaPaciente(Long idPaciente) {
        Paciente paciente = pacienteRepositorio.findById(idPaciente)
                .orElse(null);

        if (paciente == null) {
            return null;
        }

        Persona persona = paciente.getPersona();
        if (persona == null) {
            throw new RuntimeException("El paciente no tiene una persona asociada.");
        }

        // Realizar baja lógica sobre el estado del paciente
        paciente.setEstado_paciente("Inactivo");
        pacienteRepositorio.save(paciente);

        // Eliminar la tupla persona_rol correspondiente al rol Paciente (id 1L)
        try {
            Long idPersona = persona.getId_persona();
            Long idRolPaciente = 1L; // 1L = Paciente
            java.util.List<com.clinica.clinica_coc.models.PersonaRol> rolesAEliminar = personaRolRepositorio
                    .findSpecificRolesForPersona(idPersona, idRolPaciente);
            if (rolesAEliminar != null && !rolesAEliminar.isEmpty()) {
                personaRolServicio.eliminarTodos(rolesAEliminar);
            }
        } catch (Exception e) {
            // Registrar/log si es necesario; no interrumpir la baja lógica
            System.out.println("Advertencia al eliminar persona_rol en baja de paciente: " + e.getMessage());
        }

        return paciente;
    }


    public Paciente crearPacienteConPersonaYRol(PersonaRequest personaRequest, List<Long> coberturasIds, String estadoPaciente) {
        // 1. Crear Persona a partir de PersonaRequest
        Persona persona = new Persona();
        persona.setNombre(personaRequest.getNombre());
        persona.setApellido(personaRequest.getApellido());
        persona.setDni(personaRequest.getDni());
        persona.setEmail(personaRequest.getEmail());
        persona.setPassword(passwordEncoder.encode(personaRequest.getPassword()));
        persona.setDomicilio(personaRequest.getDomicilio());
        persona.setTelefono(personaRequest.getTelefono());
        persona.setIsActive("Activo");
        persona = personaServicio.guardarPersona(persona);

        // 2. Asignar rol "Paciente"
        Rol rolPaciente = rolRepositorio.findById(1L)
                .orElseThrow(() -> new RuntimeException("Rol paciente no encontrado"));
        PersonaRol personaRol = new PersonaRol();
        personaRol.setIdPersona(persona);
        personaRol.setIdRol(rolPaciente);
        personaRolServicio.guardar(personaRol);

        // 3. Buscar coberturas usando el servicio
        List<CoberturaSocial> coberturas = coberturaServicio.buscarPorIds(coberturasIds);


        // 4. Crear paciente
        Paciente paciente = new Paciente();
        paciente.setPersona(persona);
        // Establecer estado del paciente: si el request trae uno, usarlo; si no, default a Activo
        if (estadoPaciente != null && !estadoPaciente.trim().isEmpty()) {
            paciente.setEstado_paciente(estadoPaciente);
        } else {
            paciente.setEstado_paciente("Activo");
        }
        paciente.setCoberturas(coberturas);

        return pacienteRepositorio.save(paciente);
    }

    @Transactional
    public Paciente editarPaciente(Long id, PacienteRequest request) {

        // 1. Buscar paciente
        Paciente paciente = pacienteRepositorio.findById(id).orElse(null);
        if (paciente == null)
            return null;
        paciente.setEstado_paciente(request.getEstado_paciente());

        // 2. Actualizar datos de la persona
        Persona persona = paciente.getPersona();
        if (request.getPersona() != null) {
            personaServicio.editarPersona(persona.getId_persona(), request.getPersona());
        }

         Rol rolPaciente = rolRepositorio.findById(1L)
            .orElseThrow(() -> new RuntimeException("Rol paciente no encontrado"));

    // 3. Asignar PersonaRol 
    boolean tieneRol = persona.getPersonaRolList().stream()
            .anyMatch(pr -> pr.getIdRol().getId_rol().equals(1L));

    if (!tieneRol) {
        PersonaRol personaRol = new PersonaRol();
        personaRol.setIdPersona(persona);
        personaRol.setIdRol(rolPaciente);
        personaRolServicio.guardar(personaRol);
        // Es importante refrescar la lista en la entidad Persona si la usas después
        persona.getPersonaRolList().add(personaRol); 
    }
        List<Long> coberturasIds = request.getCoberturasIds(); // Obtén los IDs del request
        paciente.setEstado_paciente(request.getEstado_paciente());

        // Inicializa la lista en el paciente si es null para evitar
        // NullPointerException
        if (paciente.getCoberturas() == null) {
            paciente.setCoberturas(new ArrayList<>());
        }

        // Limpia las coberturas anteriores
        paciente.getCoberturas().clear();

        // Si se proporcionaron nuevos IDs de cobertura...
        if (coberturasIds != null && !coberturasIds.isEmpty()) {
            List<CoberturaSocial> nuevasCoberturas = coberturaSocialRepositorio.findAllById(coberturasIds);
            // Añade las nuevas coberturas a la lista del paciente
            paciente.getCoberturas().addAll(nuevasCoberturas);
        }

        // 4. Guardar paciente
        return pacienteRepositorio.save(paciente);
    }

    public Paciente buscarPacientePorIdPersona(Long idPersona) {
        return pacienteRepositorio.findByPersonaId(idPersona)
                .orElse(null);
    }

    @Override
    public Paciente crearPacienteConPersonaYRol(PersonaRequest personaRequest, List<Long> coberturasIds) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'crearPacienteConPersonaYRol'");
    }

    @Transactional
public Paciente asignarRolPaciente(Long idPersona, List<Long> coberturasIds) {
    // 1. Buscar la Persona
    Persona persona = personaServicio.buscarPersonaPorId(idPersona);
    if (persona == null) {
        throw new RuntimeException("Persona no encontrada con id: " + idPersona);
    }

    // 2. Buscar el Rol Paciente
    Rol rolPaciente = rolRepositorio.findById(1L)
            .orElseThrow(() -> new RuntimeException("Rol paciente no encontrado"));

    // 3. Asignar PersonaRol 
    boolean tieneRol = persona.getPersonaRolList().stream()
            .anyMatch(pr -> pr.getIdRol().getId_rol().equals(1L));

    if (!tieneRol) {
        PersonaRol personaRol = new PersonaRol();
        personaRol.setIdPersona(persona);
        personaRol.setIdRol(rolPaciente);
        personaRolServicio.guardar(personaRol);
        // Es importante refrescar la lista en la entidad Persona si la usas después
        persona.getPersonaRolList().add(personaRol); 
    }

    // 4. Lógica de Coberturas (con default 'Particular')
    List<Long> finalCoberturasIds = new ArrayList<>();
    if (coberturasIds != null) {
        finalCoberturasIds.addAll(coberturasIds);
    }

    if (finalCoberturasIds.isEmpty()) {
        CoberturaSocial particular = coberturaSocialRepositorio.findByNombreNativoConParam("Particular")
                .orElseThrow(() -> new RuntimeException("No se encontró la cobertura 'Particular' por defecto"));
        finalCoberturasIds.add(particular.getId_cob_social());
    }
    
    // Busca las entidades CoberturaSocial
    List<CoberturaSocial> coberturas = coberturaSocialRepositorio.findAllById(finalCoberturasIds);

   
    Optional<Paciente> optPaciente = pacienteRepositorio.findByPersonaId(idPersona);

    Paciente paciente;

    if (optPaciente.isPresent()) {
        // --- CASO 1: El Paciente YA EXISTE  ---
        paciente = optPaciente.get();
        paciente.setEstado_paciente("Activo"); // Lo reactivamos
        paciente.setCoberturas(coberturas); // Actualizamos sus coberturas
        
    } else {
        // --- CASO 2: El Paciente NO EXISTE ---
        paciente = new Paciente();
        paciente.setPersona(persona);
        paciente.setEstado_paciente("Activo");
        paciente.setCoberturas(coberturas);
    }

    // 6. Guardar la entidad Paciente 
    return pacienteRepositorio.save(paciente);
}

    public List<Paciente> listarPacientesPorOdontologoLogueado(Authentication authentication) {
        
        // 1. Obtener el email de la persona logueada
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); 

        Persona persona = personaRepositorio.findByEmail(username) 
                .orElseThrow(() -> new RuntimeException("Persona no encontrada para el usuario: " + username));
        
        Long idPersonaLogueada = persona.getId_persona();

        // 3. Buscar el ID de Odontólogo
        Odontologo odontologo = odontologoServicio.buscarOdontologoPorIdPersona(idPersonaLogueada);
        
        // 4. Llamar al nuevo método del repositorio
        List<Paciente> pacientes = pacienteRepositorio.findPacientesConTurnosPorOdontologo(odontologo.getId_odontologo());

        return pacientes;
    }

}
