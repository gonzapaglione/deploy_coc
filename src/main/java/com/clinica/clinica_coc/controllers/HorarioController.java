package com.clinica.clinica_coc.controllers;

import com.clinica.clinica_coc.DTO.HorarioRequest;
import com.clinica.clinica_coc.DTO.HorarioResponse;
import com.clinica.clinica_coc.services.HorarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "http://localhost:5173") 
public class HorarioController {

    @Autowired
    private HorarioServicio horarioServicio;

    /**
     * Endpoint para obtener todos los horarios de UN odontólogo
     * GET /api/horarios/{idOdontologo}
     */
    @GetMapping("/{idOdontologo}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_HORARIOS_OD', 'PERM_GESTIONAR_HORARIOS_ADMIN')")
    public ResponseEntity<List<HorarioResponse>> getHorariosPorOdontologo(
            @PathVariable Long idOdontologo) {
        List<HorarioResponse> horarios = horarioServicio.getHorariosPorOdontologo(idOdontologo);
        return ResponseEntity.ok(horarios);
    }

    /**
     * Endpoint para crear un nuevo horario
     * POST /api/horarios
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_HORARIOS_OD', 'PERM_GESTIONAR_HORARIOS_ADMIN')")
    public ResponseEntity<HorarioResponse> crearHorario(
            @RequestBody HorarioRequest request) {
        HorarioResponse nuevoHorario = horarioServicio.crearHorario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoHorario);
    }

    /**
     * Endpoint para actualizar un horario existente
     * PUT /api/horarios/{idHorario}
     */
    @PutMapping("/{idHorario}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_HORARIOS_OD', 'PERM_GESTIONAR_HORARIOS_ADMIN')")
    public ResponseEntity<HorarioResponse> actualizarHorario(
            @PathVariable Long idHorario,
            @RequestBody HorarioRequest request) {
        HorarioResponse horarioActualizado = horarioServicio.actualizarHorario(idHorario, request);
        return ResponseEntity.ok(horarioActualizado);
    }

    /**
     * Endpoint para eliminar un horario
     * DELETE /api/horarios/{idHorario}
     */
    @DeleteMapping("/{idHorario}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_HORARIOS_OD', 'PERM_GESTIONAR_HORARIOS_ADMIN')")
    public ResponseEntity<Void> eliminarHorario(@PathVariable Long idHorario) {
        horarioServicio.eliminarHorario(idHorario);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
} 
