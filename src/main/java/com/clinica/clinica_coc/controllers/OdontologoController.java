package com.clinica.clinica_coc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.clinica.clinica_coc.DTO.OdontologoRequest;
import com.clinica.clinica_coc.DTO.OdontologoResponse;
import com.clinica.clinica_coc.DTO.PersonaBasicDTO;
import com.clinica.clinica_coc.DTO.AsignarOdontologoRequest;
import com.clinica.clinica_coc.DTO.BajaResponse;
import com.clinica.clinica_coc.DTO.EspecialidadDTO;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.services.OdontologoServicio;
import com.clinica.clinica_coc.services.TurnoServicio;

import java.util.List;

@RestController
@RequestMapping("/api/odontologos")
@CrossOrigin("http://localhost:5173")
public class OdontologoController {

    private static final Logger logger = LoggerFactory.getLogger(OdontologoController.class);

    @Autowired
    private OdontologoServicio odontologoServicio;

    @Autowired
    private TurnoServicio turnoServicio;

    // GET: listar todos los odontólogos
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_ODONTOLOGOS', 'PERM_GESTIONAR_TURNOS_ADMIN', 'PERM_GESTIONAR_TURNOS_OD', 'PERM_RESERVAR_TURNO')")
    public ResponseEntity<List<OdontologoResponse>> listarOdontologos() {
        List<Odontologo> odontologos = odontologoServicio.listarOdontologos();

        if (odontologos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<OdontologoResponse> odontologosDTO = odontologos.stream()
                .map(this::convertirAResponse)
                .toList();

        return ResponseEntity.ok(odontologosDTO);
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_ODONTOLOGOS', 'PERM_GESTIONAR_TURNOS_ADMIN', 'PERM_GESTIONAR_TURNOS_OD', 'PERM_RESERVAR_TURNO')")
    public ResponseEntity<List<OdontologoResponse>> listarOdontologosActivos() {
        List<Odontologo> odontologos = odontologoServicio.listarOdontologosActivos("Activo");

        if (odontologos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<OdontologoResponse> odontologosDTO = odontologos.stream()
                .map(this::convertirAResponse)
                .toList();

        return ResponseEntity.ok(odontologosDTO);
    }

    // GET: obtener odontólogo por id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_ODONTOLOGOS', 'PERM_GESTIONAR_TURNOS_ADMIN', 'PERM_GESTIONAR_TURNOS_OD', 'PERM_RESERVAR_TURNO')")
    public ResponseEntity<OdontologoResponse> obtenerOdontologoPorId(@PathVariable Long id) {
        Odontologo odontologo = odontologoServicio.buscarOdontologoPorId(id);

        if (odontologo == null) {
            return ResponseEntity.notFound().build();
        }

        OdontologoResponse dto = convertirAResponse(odontologo);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/persona/{idPersona}")
    @PreAuthorize("hasAnyAuthority('PERM_VER_MI_PERFIL_OD', 'PERM_VER_MI_PERFIL_ADMIN')")
    public ResponseEntity<OdontologoResponse> obtenerOdontologoPorIdPersona(@PathVariable Long idPersona) {
        Odontologo odontologo = odontologoServicio.buscarOdontologoPorIdPersona(idPersona);

        if (odontologo == null) {
            return ResponseEntity.notFound().build();
        }

        OdontologoResponse dto = convertirAResponse(odontologo);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/horarios-disponibles")
    @PreAuthorize("hasAnyAuthority('PERM_RESERVAR_TURNO', 'PERM_GESTIONAR_TURNOS_ADMIN', 'PERM_GESTIONAR_TURNOS_OD')")
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(
            @PathVariable Long id,
            @RequestParam("fecha") String fecha) {
        List<String> horariosDisponibles = turnoServicio.obtenerHorariosDisponibles(id, fecha);
        return ResponseEntity.ok(horariosDisponibles);
    }

    // POST: crear nuevo odontólogo
    @PostMapping
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_ODONTOLOGOS')")
    public ResponseEntity<OdontologoResponse> crearOdontologo(@RequestBody OdontologoRequest request) {
        logger.info("Creando odontólogo: " + request);

        Odontologo nuevoOdontologo = odontologoServicio.crearOdontologoConPersonaYRol(
                request.getPersona(), request.getEspecialidadesIds());

        if (nuevoOdontologo == null) {
            return ResponseEntity.badRequest().build();
        }

        OdontologoResponse dto = convertirAResponse(nuevoOdontologo);
        return ResponseEntity.status(201).body(dto);
    }

    // PUT: editar odontólogo
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_ODONTOLOGOS')")
    public ResponseEntity<OdontologoResponse> editarOdontologo(
            @PathVariable Long id,
            @RequestBody OdontologoRequest request) {

        Odontologo odontologoEditado = odontologoServicio.editarOdontologo(
                id, request.getPersona(), request.getEspecialidadesIds(), request.getEstado_odont());

        if (odontologoEditado == null) {
            return ResponseEntity.notFound().build();
        }

        OdontologoResponse dto = convertirAResponse(odontologoEditado);
        return ResponseEntity.ok(dto);
    }

    // DELETE: baja lógica (delegada al servicio)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_ODONTOLOGOS')")
    public ResponseEntity<BajaResponse> quitarRolOdontologo(@PathVariable Long id) {
        // Verificar existencia previa para devolver 404 si no existe
        Odontologo existente = odontologoServicio.buscarOdontologoPorId(id);
        if (existente == null) {
            logger.warn("Odontólogo con id {} no encontrado para baja lógica", id);
            return ResponseEntity.notFound().build();
        }

        try {
            // Llama al método que realiza la baja lógica: set Estado Odontologo = Inactivo
            // + eliminar persona_rol
            Odontologo odontActualizado = odontologoServicio.bajaLogicaOdontologo(id);
            if (odontActualizado == null) {
                return ResponseEntity.notFound().build();
            }

            OdontologoResponse dto = convertirAResponse(odontActualizado);
            BajaResponse response = new BajaResponse("Odontólogo dado de baja lógicamente", dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al dar de baja lógica al odontólogo con id {}: {}", id, e.getMessage(), e);
            BajaResponse response = new BajaResponse("Error al procesar la baja lógica: " + e.getMessage(), id);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Método auxiliar: convertir Odontologo a OdontologoResponse
    private OdontologoResponse convertirAResponse(Odontologo odontologo) {
        PersonaBasicDTO personaDTO = convertirPersonaABasicDTO(odontologo.getPersona());

        List<EspecialidadDTO> especialidadesDTO = odontologo.getEspecialidadOdontologoList() != null
                ? odontologo.getEspecialidadOdontologoList().stream()
                        .map(eo -> new EspecialidadDTO(
                                eo.getEspecialidad().getId_especialidad(),
                                eo.getEspecialidad().getNombre(),
                                eo.getEspecialidad().getEstado_especialidad()))
                        .toList()
                : List.of();

        return new OdontologoResponse(
                odontologo.getId_odontologo(),
                personaDTO,
                especialidadesDTO,
                odontologo.getEstado_odont());
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
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_ODONTOLOGOS')")
    public ResponseEntity<OdontologoResponse> asignarRolOdontologo(@RequestBody AsignarOdontologoRequest request) {
        try {
            Odontologo nuevoOdontologo = odontologoServicio.asignarRolOdontologo(
                    request.getIdPersona(),
                    request.getEspecialidadesIds());

            OdontologoResponse dto = convertirAResponse(nuevoOdontologo);
            return ResponseEntity.status(201).body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // O un DTO de error
        }
    }
}
