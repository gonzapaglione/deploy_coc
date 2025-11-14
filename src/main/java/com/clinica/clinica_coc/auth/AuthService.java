package com.clinica.clinica_coc.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.models.Paciente;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.jwt.JwtService;
import com.clinica.clinica_coc.models.CoberturaSocial;
import com.clinica.clinica_coc.models.Rol;
import com.clinica.clinica_coc.models.PersonaRol;
import com.clinica.clinica_coc.repositories.PersonaRepositorio;
import com.clinica.clinica_coc.repositories.PacienteRepositorio;
import com.clinica.clinica_coc.repositories.OdontologoRepositorio; 
import com.clinica.clinica_coc.repositories.RolRepositorio;
import com.clinica.clinica_coc.repositories.PersonaRolRepositorio;
import com.clinica.clinica_coc.services.CoberturaSocialServicio;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet; 
import java.util.Set; 

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final PersonaRepositorio personaRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final OdontologoRepositorio odontologoRepositorio; 
    private final CoberturaSocialServicio coberturaServicio;
    private final RolRepositorio rolRepositorio;
    private final PersonaRolRepositorio personaRolRepositorio;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        
        // Cargar la Persona
        UserDetails userDetails = personaRepositorio.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado post-autenticación"));
        Persona persona = (Persona) userDetails;

        
        //  Preparar el Set de permisos
        Set<String> permisos = new HashSet<>();
        Set<String> roles = new HashSet<>();

        //  Obtener los roles "entidad" (Paciente, Odontologo)
        Paciente paciente = pacienteRepositorio.findByPersonaId(persona.getId_persona()).orElse(null);
        Odontologo odontologo = odontologoRepositorio.findByPersonaId(persona.getId_persona()).orElse(null);

        //  Lógica para Paciente (basada en ESTADO)
        if (paciente != null) {
            roles.add("PACIENTE");
            // Permisos que tiene sin importar el estado (siempre que sea paciente)
            permisos.add("PERM_VER_MI_PERFIL_PACIENTE");
            permisos.add("PERM_VER_INICIO");
            if (paciente.getEstado_paciente() != null && paciente.getEstado_paciente().equalsIgnoreCase("Activo")) {
                permisos.add("PERM_RESERVAR_TURNO");
                permisos.add("PERM_VER_MIS_TURNOS");
                permisos.add("PERM_EDITAR_PERFIL_PACIENTE");
            } else if (paciente.getEstado_paciente() != null && paciente.getEstado_paciente().equalsIgnoreCase("Inactivo")) {
                permisos.add("PERM_VER_MIS_TURNOS"); // Paciente inactivo SÍ puede ver sus turnos/historia
            }
        }

        //  Lógica para Odontólogo (basada en ESTADO)
        if (odontologo != null) {
            roles.add("ODONTOLOGO");
            if (odontologo.getEstado_odont() != null && odontologo.getEstado_odont().equalsIgnoreCase("Activo")) {
                permisos.add("PERM_VER_MI_PERFIL_OD");
                permisos.add("PERM_VER_INICIO_ODONT");
                permisos.add("PERM_GESTIONAR_HORARIOS_OD");
                permisos.add("PERM_GESTIONAR_TURNOS_OD");
                permisos.add("PERM_VER_HISTORIAS_CLINICAS_OD");
                permisos.add("PERM_VER_MI_AGENDA_OD");
            }
            // Si está Inactivo, simplemente no se le añaden permisos de odontólogo.
        }

        // 6. Lógica para Admin (basada en ROL estático)
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("Admin"));
        
        if (isAdmin) {
            roles.add("ADMIN");
            permisos.add("PERM_GESTIONAR_PACIENTES");
            permisos.add("PERM_VER_AGENDA_EQUIPO_ADMIN");
            permisos.add("PERM_GESTIONAR_ODONTOLOGOS");
            permisos.add("PERM_VER_INICIO_ADMIN");
            permisos.add("PERM_GESTIONAR_PERSONAS");
            permisos.add("PERM_GESTIONAR_ESPECIALIDADES");
            permisos.add("PERM_GESTIONAR_COBERTURAS");
            permisos.add("PERM_GESTIONAR_HORARIOS_ADMIN");
            permisos.add("PERM_GESTIONAR_TURNOS_ADMIN");
            permisos.add("PERM_GESTIONAR_HISTORIAS_CLINICAS_ADMIN");
            permisos.add("PERM_VER_MI_PERFIL_ADMIN");
            permisos.add("PERM_EDITAR_PERFIL_PACIENTE");
}

        //  Permisos generales para CUALQUIER usuario logueado
        permisos.add("PERM_CAMBIAR_PASSWORD");


        //  Crear los claims para el JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("permisos", new ArrayList<>(permisos)); 
        extraClaims.put("idUsuario", persona.getId_persona()); 
        extraClaims.put("roles", new ArrayList<>(roles)); 
        extraClaims.put("nombre", persona.getNombre() + " " + persona.getApellido());

        //  Genera el token USANDO los claims
        String token = jwtService.getToken(extraClaims, userDetails);
        
        //  Devolver la nueva respuesta completa
        return AuthResponse.builder()
            .token(token)
            .idUsuario(persona.getId_persona())
            .email(persona.getEmail())
            .permisos(new ArrayList<>(permisos)) 
            .roles(new ArrayList<>(roles)) 
            .nombre(persona.getNombre() + " " + persona.getApellido()) 
            .build();
    }

  @Transactional
    public AuthResponse register(RegisterRequest request) {

        // --- VALIDACIÓN DE DUPLICADOS ---
      if (personaRepositorio.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está en uso.");
        }
        if (personaRepositorio.findByDni(request.getDni()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya está registrado.");
        }
       
        // 1) Crear y guardar Persona
        Persona persona = new Persona();
        persona.setNombre(request.getNombre());
        persona.setApellido(request.getApellido());
        persona.setEmail(request.getEmail());
        persona.setPassword(passwordEncoder.encode(request.getPassword()));
        persona.setDomicilio(request.getDomicilio());
        persona.setTelefono(request.getTelefono());
        persona.setDni(request.getDni());

        Persona savedPersona = personaRepositorio.save(persona);
        Rol rolPaciente = rolRepositorio.findById(1L)
                .orElseThrow(() -> new RuntimeException("Error: Rol PACIENTE no encontrado en la base de datos."));

        // Crear la relación en la tabla persona_rol
        PersonaRol personaRol = new PersonaRol();
        personaRol.setIdPersona(savedPersona); 
        personaRol.setIdRol(rolPaciente); 
        personaRolRepositorio.save(personaRol);

        // Crear Paciente asociado
        Paciente paciente = new Paciente();
        paciente.setPersona(savedPersona);
        paciente.setEstado_paciente("Activo"); // Establecer estado al registrar

        if (request.getCoberturaIds() != null && !request.getCoberturaIds().isEmpty()) {
            List<CoberturaSocial> coberturas = coberturaServicio.buscarPorIds(request.getCoberturaIds());
            paciente.setCoberturas(coberturas);
        } else {
            paciente.setCoberturas(new ArrayList<>());
        }

        pacienteRepositorio.save(paciente);

        // 3) Generar token HACIENDO LOGIN
        LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getPassword());
        return login(loginRequest); 
    }
}