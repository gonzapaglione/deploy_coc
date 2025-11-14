package com.clinica.clinica_coc.controllers;

import com.clinica.clinica_coc.DTO.AsignarAdminRequest;
import com.clinica.clinica_coc.DTO.CambioPasswordDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.clinica.clinica_coc.services.PersonaServicio;
import com.clinica.clinica_coc.services.PersonaRolServicio;
import com.clinica.clinica_coc.DTO.PersonaDTO;
import com.clinica.clinica_coc.DTO.PersonaRequest;
import com.clinica.clinica_coc.DTO.RolDTO;
import com.clinica.clinica_coc.exceptions.ResourceNotFoundException;
import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.models.PersonaRol;
import com.clinica.clinica_coc.models.Rol;
import com.clinica.clinica_coc.repositories.RolRepositorio;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/personas")
@CrossOrigin("http://localhost:5173")
public class PersonaController {

    private static final Logger logger = LoggerFactory.getLogger(PersonaController.class);

    @Autowired
    private PersonaServicio personaServicio;

    @Autowired
    private PersonaRolServicio personaRolServicio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET: listar todas las personas con roles
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_PERSONAS')")
    public ResponseEntity<List<PersonaDTO>> listarPersonas() {
        List<Persona> personas = personaServicio.listarPersonas();

        if (personas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<PersonaDTO> personasDTO = personas.stream().map(this::convertirADTO).toList();
        return ResponseEntity.ok(personasDTO);
    }

    // GET: listar persona por ID con roles
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_PERSONAS')")
    public ResponseEntity<PersonaDTO> listarPersonaPorId(@PathVariable Long id) {
        Persona persona = personaServicio.buscarPersonaPorId(id);

        if (persona == null) {
            return ResponseEntity.notFound().build();
        }

        PersonaDTO dto = convertirADTO(persona);
        return ResponseEntity.ok(dto);
    }

    // POST: agregar persona (con roles opcionales)
    @PostMapping
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_PERSONAS')")
    public ResponseEntity<PersonaDTO> agregarPersona(@RequestBody PersonaRequest request) {
        logger.info("Persona a agregar: " + request);

        // 1. Crear y guardar persona
        Persona persona = new Persona();
        persona.setNombre(request.getNombre());
        persona.setApellido(request.getApellido());
        persona.setDni(request.getDni());
        persona.setEmail(request.getEmail());
        persona.setPassword(passwordEncoder.encode(request.getPassword()));
        persona.setDomicilio(request.getDomicilio());
        persona.setTelefono(request.getTelefono());
        persona.setIsActive(request.getIsActive() != null ? request.getIsActive() : "Activo");

        Persona nuevaPersona = personaServicio.guardarPersona(persona);

        if (nuevaPersona == null) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Asignar roles si vienen (OPCIONAL)
        if (request.getRolesIds() != null && !request.getRolesIds().isEmpty()) {
            for (Long rolId : request.getRolesIds()) {
                Rol rol = rolRepositorio.findById(rolId)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + rolId));
                PersonaRol personaRol = new PersonaRol();
                personaRol.setIdPersona(nuevaPersona);
                personaRol.setIdRol(rol);
                personaRolServicio.guardar(personaRol);
            }
        }

        // 3. Recargar persona con roles y convertir a DTO
        Persona personaConRoles = personaServicio.buscarPersonaPorId(nuevaPersona.getId_persona());
        PersonaDTO dto = convertirADTO(personaConRoles);

        return ResponseEntity.status(201).body(dto);
    }

    // PUT: editar persona (con roles opcionales)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_PERSONAS', 'PERM_GESTIONAR_PACIENTES')")
    public ResponseEntity<PersonaDTO> editarPersona(
            @PathVariable Long id,
            @RequestBody PersonaRequest request) {

        Persona persona = personaServicio.buscarPersonaPorId(id);
        if (persona == null) {
            return ResponseEntity.notFound().build();
        }

        // 1. Actualizar campos de la persona
        persona.setNombre(request.getNombre());
        persona.setApellido(request.getApellido());
        persona.setDni(request.getDni());
        persona.setEmail(request.getEmail());
        if (request.getPassword() != null) {
            persona.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        persona.setDomicilio(request.getDomicilio());
        persona.setTelefono(request.getTelefono());
        if (request.getIsActive() != null) {
            persona.setIsActive(request.getIsActive());
        }

        Persona personaGuardada = personaServicio.guardarPersona(persona);

        // 2. Actualizar roles si vienen (OPCIONAL)
        if (request.getRolesIds() != null && !request.getRolesIds().isEmpty()) {
            // Eliminar roles previos
            if (personaGuardada.getPersonaRolList() != null && !personaGuardada.getPersonaRolList().isEmpty()) {
                personaRolServicio.eliminarTodos(personaGuardada.getPersonaRolList());
                personaGuardada.getPersonaRolList().clear(); // Limpiar la lista en memoria
            }

            // Asignar nuevos roles
            for (Long rolId : request.getRolesIds()) {
                Rol rol = rolRepositorio.findById(rolId)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + rolId));
                PersonaRol personaRol = new PersonaRol();
                personaRol.setIdPersona(personaGuardada);
                personaRol.setIdRol(rol);
                PersonaRol personaRolGuardada = personaRolServicio.guardar(personaRol);
                personaGuardada.getPersonaRolList().add(personaRolGuardada); // Agregar a la lista en memoria
            }
        }

        // 3. Recargar persona con roles actualizados (para asegurar sincronización)
        Persona personaActualizada = personaServicio.buscarPersonaPorId(personaGuardada.getId_persona());
        PersonaDTO dto = convertirADTO(personaActualizada);

        return ResponseEntity.ok(dto);
    }

    // DELETE: baja lógica
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_PERSONAS')")
    public ResponseEntity<?> bajaLogicaPersona(@PathVariable Long id) {
        Persona persona = personaServicio.buscarPersonaPorId(id);
        if (persona == null) {
            return ResponseEntity.notFound().build();
        }

        persona.setIsActive("Inactivo");
        personaServicio.guardarPersona(persona);

        // Recargar persona actualizada
        Persona personaActualizada = personaServicio.buscarPersonaPorId(id);
        PersonaDTO dto = convertirADTO(personaActualizada);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("mensaje", "Persona dada de baja lógicamente");
        response.put("datos", dto);

        return ResponseEntity.ok(response);
    }

    // Método auxiliar para convertir Persona a PersonaDTO
    private PersonaDTO convertirADTO(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setId_persona(persona.getId_persona());
        dto.setNombre(persona.getNombre());
        dto.setApellido(persona.getApellido());
        dto.setDni(persona.getDni());
        dto.setEmail(persona.getEmail());
        dto.setPassword(persona.getPassword());
        dto.setDomicilio(persona.getDomicilio());
        dto.setTelefono(persona.getTelefono());
        dto.setIsActive(persona.getIsActive());

        // Roles
        List<RolDTO> rolesDTO = persona.getPersonaRolList() != null
                ? persona.getPersonaRolList().stream()
                        .map(pr -> new RolDTO(pr.getIdRol().getId_rol(), pr.getIdRol().getNombre_rol()))
                        .toList()
                : List.of();
        dto.setRoles(rolesDTO);

        return dto;
    }
    
@PutMapping("/cambiar-password")
@PreAuthorize("hasAuthority('PERM_CAMBIAR_PASSWORD')")
public ResponseEntity<?> cambiarPassword(@RequestBody CambioPasswordDTO dto) {

    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    Persona usuario = personaServicio.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    
    if (passwordEncoder.matches(dto.getContraseñaActual(), usuario.getPassword())) {
        
        // 3. Si coincide, hashear y guardar la NUEVA contraseña
        String nuevoHash = passwordEncoder.encode(dto.getNuevaContraseña());
        usuario.setPassword(nuevoHash);
        personaServicio.save(usuario); 

        return ResponseEntity.ok("Contraseña actualizada con éxito");

    } else {
        // 4. Si no coincide, devolver el error
        return new ResponseEntity<>("La contraseña actual es incorrecta", HttpStatus.UNAUTHORIZED);
    }
}

@PostMapping("/asignar-admin")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_PERSONAS')")
    public ResponseEntity<?> asignarRolAdmin(@RequestBody AsignarAdminRequest request) {
        try {
            Persona personaActualizada = personaServicio.asignarRolAdmin(request.getIdPersona());
            PersonaDTO dto = convertirADTO(personaActualizada); 
            return ResponseEntity.status(201).body(dto);
        
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
