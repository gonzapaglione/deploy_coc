package com.clinica.clinica_coc.controllers;

import com.clinica.clinica_coc.DTO.EspecialidadDTO;
import com.clinica.clinica_coc.models.Especialidad;
import com.clinica.clinica_coc.services.EspecialidadServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/especialidades")
@CrossOrigin(origins = "http://localhost:5173")
public class EspecialidadController {

    @Autowired
    private EspecialidadServicio especialidadServicio;

    @GetMapping
    public ResponseEntity<?> listarEspecialidades() {
        try {
            List<EspecialidadDTO> lista = especialidadServicio.findAllDtos();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar especialidades: " + e.getMessage());
        }
    }

    @GetMapping("/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Especialidad>> listarActivas() {
        return ResponseEntity.ok(especialidadServicio.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEspecialidadPorId(@PathVariable Long id) {
        try {
            EspecialidadDTO dto = especialidadServicio.findDtoById(id);
            if (dto == null) {
                return ResponseEntity.status(404).body("Especialidad no encontrada");
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener especialidad: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crearEspecialidad(@RequestBody Especialidad especialidad) {
        try {
            Especialidad nuevaEspecialidad = especialidadServicio.crearEspecialidad(especialidad);
            return new ResponseEntity<>(nuevaEspecialidad, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Error por duplicado
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEspecialidad(@PathVariable Long id, @RequestBody Especialidad especialidad) {
        try {
            Especialidad actualizada = especialidadServicio.actualizarEspecialidad(id, especialidad);
            return ResponseEntity.ok(actualizada);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/baja-logica")
    public ResponseEntity<?> bajaLogica(@PathVariable Long id) {
        try {
            Especialidad especialidadInactiva = especialidadServicio.bajaLogica(id);
            return ResponseEntity.ok(especialidadInactiva);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // PUT: /api/v1/especialidades/{id}/reactivar
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivarEspecialidad(@PathVariable Long id) {
        try {
            Especialidad especialidadActiva = especialidadServicio.reactivarEspecialidad(id);
            return ResponseEntity.ok(especialidadActiva);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
