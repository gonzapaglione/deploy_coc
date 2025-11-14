package com.clinica.clinica_coc.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.clinica.clinica_coc.DTO.PersonaRequest;
import com.clinica.clinica_coc.models.Especialidad;
import com.clinica.clinica_coc.models.EspecialidadOdontologo;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.models.PersonaRol;
import com.clinica.clinica_coc.models.Rol;
import com.clinica.clinica_coc.repositories.EspecialidadOdontologoRepositorio;
import com.clinica.clinica_coc.repositories.EspecialidadRepositorio;
import com.clinica.clinica_coc.repositories.OdontologoRepositorio;
import com.clinica.clinica_coc.repositories.PersonaRepositorio;
import com.clinica.clinica_coc.repositories.PersonaRolRepositorio;
import com.clinica.clinica_coc.repositories.RolRepositorio;

import jakarta.transaction.Transactional;

@Service
public class OdontologoServicio implements IOdontologoServicio {

    @Autowired
    private OdontologoRepositorio odontologoRepositorio;

    @Autowired
    private PersonaServicio personaServicio;

    @Autowired
    private PersonaRepositorio personaRepositorio;

    @Autowired
    private PersonaRolRepositorio personaRolRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EspecialidadRepositorio especialidadRepositorio;

    @Autowired
    private EspecialidadOdontologoRepositorio especialidadOdontologoRepositorio;

    @Autowired
    private PersonaRolServicio personaRolServicio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Override
    public List<Odontologo> listarOdontologos() {
           return odontologoRepositorio.findAll();
    }

     public List<Odontologo> listarOdontologosActivos(String estado) {
       return odontologoRepositorio.buscarTodosPorEstado(estado);
    }

    @Override
    public Odontologo buscarOdontologoPorId(Long id) {
        return odontologoRepositorio.findById(id).orElse(null);
    }



    @Override
    public Odontologo guardarOdontologo(Odontologo odontologo) {
        if (odontologo.getPersona() == null || odontologo.getPersona().getId_persona() == null) {
            throw new RuntimeException("Debe asignarse una persona existente al odontólogo");
        }
        return odontologoRepositorio.save(odontologo);
    }

    @Override
    public void eliminarOdontologo(Odontologo odontologo) {
        odontologoRepositorio.delete(odontologo);
    }

    @Transactional
    public Odontologo bajaLogicaOdontologo(Long idOdontologo) {
        Odontologo odontologo = odontologoRepositorio.findById(idOdontologo)
                .orElseThrow(() -> new RuntimeException(
                        "Odontólogo no encontrado con id: " + idOdontologo));

        Persona persona = odontologo.getPersona();
        if (persona == null) {
            throw new RuntimeException("El odontólogo no tiene una persona asociada.");
        }

        // Dar baja lógica al odontólogo
        odontologo.setEstado_odont("Inactivo");
        odontologoRepositorio.save(odontologo);

        // Eliminar la tupla persona_rol correspondiente al rol Odontólogo (id 2L)
        try {
            Long idPersona = persona.getId_persona();
            Long idRolOdontologo = 2L; // 2L = Odontólogo
            java.util.List<com.clinica.clinica_coc.models.PersonaRol> rolesAEliminar = personaRolRepositorio
                    .findSpecificRolesForPersona(idPersona, idRolOdontologo);
            if (rolesAEliminar != null && !rolesAEliminar.isEmpty()) {
                personaRolServicio.eliminarTodos(rolesAEliminar);
            }
        } catch (Exception e) {
            System.out.println("Advertencia al eliminar persona_rol en baja de odontólogo: " + e.getMessage());
        }

        return odontologoRepositorio.findById(idOdontologo).orElse(null);
    }

    @Override
    public Odontologo crearOdontologoConPersonaYRol(PersonaRequest personaRequest, List<Long> especialidadesIds) {
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

        // 2. Asignar rol "Odontólogo"
        Rol rolOdontologo = rolRepositorio.findById(2L)
                .orElseThrow(() -> new RuntimeException("Rol odontólogo no encontrado"));
        PersonaRol personaRol = new PersonaRol();
        personaRol.setIdPersona(persona);
        personaRol.setIdRol(rolOdontologo);
        personaRolServicio.guardar(personaRol);

        // 3. Crear odontólogo
        Odontologo odontologo = new Odontologo();
        odontologo.setPersona(persona);
        odontologo.setEstado_odont("Activo");
        odontologo = odontologoRepositorio.save(odontologo);

        // 4. Asignar especialidades si vienen
        if (especialidadesIds != null && !especialidadesIds.isEmpty()) {
            for (Long especialidadId : especialidadesIds) {
                Especialidad especialidad = especialidadRepositorio.findById(especialidadId)
                        .orElseThrow(
                                () -> new RuntimeException("Especialidad no encontrada con id: " + especialidadId));

                EspecialidadOdontologo especialidadOdontologo = new EspecialidadOdontologo();
                especialidadOdontologo.setOdontologo(odontologo);
                especialidadOdontologo.setEspecialidad(especialidad);
                especialidadOdontologoRepositorio.save(especialidadOdontologo);
            }
        }

        return odontologo;
    }

    public Odontologo buscarOdontologoPorIdPersona(Long idPersona) {
        return odontologoRepositorio.findByPersonaId(idPersona).orElse(null);
    }

    public Odontologo editarOdontologo(Long id, PersonaRequest personaRequest, List<Long> especialidadesIds, String estado) {
        Odontologo odontologo = odontologoRepositorio.findById(id).orElse(null);
        if (odontologo == null)
            return null;

        // Actualizar datos de la persona
        Persona persona = odontologo.getPersona();
        if (personaRequest != null) {
            personaServicio.editarPersona(persona.getId_persona(), personaRequest);
        }
        odontologo.setEstado_odont(estado);
       

        // Actualizar especialidades si vienen
        if (especialidadesIds != null && !especialidadesIds.isEmpty()) {
            // Eliminar relaciones previas
            List<EspecialidadOdontologo> relacionesPrevias = odontologo.getEspecialidadOdontologoList();
            if (relacionesPrevias != null && !relacionesPrevias.isEmpty()) {
                especialidadOdontologoRepositorio.deleteAll(relacionesPrevias);
            }

            // Crear nuevas relaciones
            for (Long especialidadId : especialidadesIds) {
                Especialidad especialidad = especialidadRepositorio.findById(especialidadId)
                        .orElseThrow(
                                () -> new RuntimeException("Especialidad no encontrada con id: " + especialidadId));

                EspecialidadOdontologo especialidadOdontologo = new EspecialidadOdontologo();
                especialidadOdontologo.setOdontologo(odontologo);
                especialidadOdontologo.setEspecialidad(especialidad);
                especialidadOdontologoRepositorio.save(especialidadOdontologo);
            }
        }

        return odontologoRepositorio.findById(id).orElse(null);
    }

    @Transactional
    public Odontologo asignarRolOdontologo(Long idPersona, List<Long> especialidadesIds) {
        // 1. Buscar la persona
        Persona persona = personaRepositorio.findById(idPersona)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + idPersona));

        // 2. Verifica si ya es odontólogo
        Optional<Odontologo> yaExiste = odontologoRepositorio.findByPersonaId(idPersona);
        if (yaExiste.isPresent()) {
            throw new RuntimeException("Esta persona ya es un odontólogo.");
        }

        // 3. Asignar el Rol de Odontólogo (ID 2)
        Rol rolOdontologo = rolRepositorio.findById(2L) // Asumiendo 2L = Odontologo
                .orElseThrow(() -> new RuntimeException("Rol odontólogo no encontrado"));

        // 4. Verificar si ya tiene el rol antes de añadirlo
        boolean tieneRol = persona.getPersonaRolList().stream()
                .anyMatch(pr -> pr.getIdRol().getId_rol().equals(2L));

        if (!tieneRol) {
            PersonaRol personaRol = new PersonaRol();
            personaRol.setIdPersona(persona);
            personaRol.setIdRol(rolOdontologo);
            personaRolServicio.guardar(personaRol);
        }

        // 5. Crear la entidad Odontologo
        Odontologo odontologo = new Odontologo();
        odontologo.setPersona(persona);
        odontologo.setEstado_odont("Activo");
        odontologo = odontologoRepositorio.save(odontologo);

        // 6. Asignar especialidades (lógica copiada de tu otro método)
        if (especialidadesIds != null && !especialidadesIds.isEmpty()) {
            for (Long especialidadId : especialidadesIds) {
                Especialidad especialidad = especialidadRepositorio.findById(especialidadId)
                        .orElseThrow(
                                () -> new RuntimeException("Especialidad no encontrada con id: " + especialidadId));

                EspecialidadOdontologo especialidadOdontologo = new EspecialidadOdontologo();
                especialidadOdontologo.setOdontologo(odontologo);
                especialidadOdontologo.setEspecialidad(especialidad);
                especialidadOdontologoRepositorio.save(especialidadOdontologo);
            }
        }

        return odontologo;
    }

    @Transactional
    public void quitarRolYOdontologo(Long idOdontologo) {

        Odontologo odontologo = odontologoRepositorio.findById(idOdontologo)
                .orElseThrow(() -> new RuntimeException("Odontólogo no encontrado con id: " + idOdontologo));

        Persona persona = odontologo.getPersona();
        if (persona == null) {
            throw new RuntimeException("El odontólogo no tiene una persona asociada.");
        }
        Long idPersona = persona.getId_persona();
        Long idRolOdontologo = 2L;

        List<PersonaRol> rolesAEliminar = personaRolRepositorio.findSpecificRolesForPersona(idPersona, idRolOdontologo);

        if (!rolesAEliminar.isEmpty()) {
            personaRolRepositorio.deleteAll(rolesAEliminar);
            persona.getPersonaRolList().removeIf(pr -> pr.getIdRol().getId_rol().equals(idRolOdontologo));
        } else {
            System.out.println("Advertencia: No se encontró el rol Odontólogo para la persona ID: " + idPersona);
        }

        if (odontologo.getEspecialidadOdontologoList() != null
                && !odontologo.getEspecialidadOdontologoList().isEmpty()) {
            especialidadOdontologoRepositorio.deleteAll(odontologo.getEspecialidadOdontologoList());
        }
        // Ahora, elimina al odontólogo
        odontologoRepositorio.delete(odontologo);
    }

   
}
