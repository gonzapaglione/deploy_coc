package com.clinica.clinica_coc.services;

import com.clinica.clinica_coc.DTO.PersonaRequest;
import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.models.PersonaRol;
import com.clinica.clinica_coc.models.Rol;
import com.clinica.clinica_coc.repositories.PersonaRepositorio;
import com.clinica.clinica_coc.repositories.RolRepositorio;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

@Service
public class PersonaServicio implements IPersonaServicio {

    @Autowired
    private PersonaRepositorio personaRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private PersonaRolServicio personaRolServicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Persona> listarPersonas() {
        return personaRepositorio.findAll();
    }

    @Override
    public Persona buscarPersonaPorId(Long id) {
        return personaRepositorio.findById(id).orElse(null);
    }

    @Override
    public Persona guardarPersona(Persona persona) {
        return personaRepositorio.save(persona);
    }

    @Override
    public void darBajaPersona(Persona persona) {
        persona.setIsActive("Inactivo");
        personaRepositorio.save(persona);
    }

    @Transactional
    public Persona editarPersona(Long idPersona, PersonaRequest request) { // O tu método de edición
        Persona persona = personaRepositorio.findById(idPersona)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        // Actualiza otros campos
        persona.setNombre(request.getNombre());
        persona.setApellido(request.getApellido());
        persona.setEmail(request.getEmail());
        persona.setDni(request.getDni());
        persona.setDomicilio(request.getDomicilio());
        persona.setTelefono(request.getTelefono());

        // --- LÓGICA CONDICIONAL PARA CONTRASEÑA ---
        String nuevaPassword = request.getPassword(); // Obtén la contraseña del request
        
        // Solo actualiza si se proporcionó una nueva contraseña NO vacía
        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            // Genera el NUEVO hash
            String nuevoHash = passwordEncoder.encode(nuevaPassword);
            // Guarda el NUEVO hash
            persona.setPassword(nuevoHash);
            System.out.println("Contraseña actualizada para usuario ID: " + idPersona); // Log de depuración
        } else {
            // Si la contraseña vino vacía o null, NO SE HACE NADA.
            // El hash existente en la BD se mantiene.
             System.out.println("Contraseña NO actualizada (campo vacío/null) para usuario ID: " + idPersona); // Log de depuración
        }

        return personaRepositorio.save(persona);
    }

    public Optional<Persona> findByEmail(String userEmail) {
     return personaRepositorio.findByEmail(userEmail);
    }

    public void save(Persona usuario) {
        personaRepositorio.save(usuario);
    }

    @Transactional
    public Persona asignarRolAdmin(Long idPersona) {
        // 1. Buscar la persona
        Persona persona = personaRepositorio.findById(idPersona)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + idPersona));

        // 2. Asignar el Rol de Admin
        Rol rolAdmin = rolRepositorio.findById(3L)
                .orElseThrow(() -> new RuntimeException("Rol Admin no encontrado"));

        // 3. Verificar si ya tiene el rol antes de añadirlo
        boolean tieneRol = persona.getPersonaRolList().stream()
                .anyMatch(pr -> pr.getIdRol().getId_rol().equals(3L));

        if (tieneRol) {
            throw new RuntimeException("Esta persona ya es un Admin.");
        }

        // 4. Guardar la nueva relación PersonaRol
        PersonaRol personaRol = new PersonaRol();
        personaRol.setIdPersona(persona);
        personaRol.setIdRol(rolAdmin);
        personaRolServicio.guardar(personaRol);
        
       
        persona.getPersonaRolList().add(personaRol);

        return persona;
    }

}