package com.clinica.clinica_coc.controllers;

import com.clinica.clinica_coc.DTO.CoberturaSocialDTO;
import com.clinica.clinica_coc.models.CoberturaSocial;
import com.clinica.clinica_coc.services.CoberturaSocialServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/coberturas")
@CrossOrigin(origins = "http://localhost:5173")
public class CoberturaSocialController {

    @Autowired
    private CoberturaSocialServicio coberturaSocialServicio;

    @GetMapping
    public ResponseEntity<?> listarCoberturas() {
        try {
            List<CoberturaSocialDTO> lista = coberturaSocialServicio.findAllDtos();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar coberturas: " + e.getMessage());
        }
    }

       @GetMapping("/activas")
    public ResponseEntity<List<CoberturaSocialDTO>> listarActivas() {
        return ResponseEntity.ok(coberturaSocialServicio.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCoberturaPorId(@PathVariable Long id) {
        try {
            CoberturaSocialDTO dto = coberturaSocialServicio.findDtoById(id);
            if (dto == null) {
                return ResponseEntity.status(404).body("Cobertura no encontrada");
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener cobertura: " + e.getMessage());
        }
    }

 


    // POST /api/coberturas
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_COBERTURAS')")
    public ResponseEntity<?> crearCobertura(@RequestBody CoberturaSocial cobertura) {
        try {
            CoberturaSocial nuevaCobertura = coberturaSocialServicio.crearCobertura(cobertura);
            return new ResponseEntity<>(nuevaCobertura, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Error por duplicado
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // PUT /api/coberturas/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_COBERTURAS')")
    public ResponseEntity<?> actualizarCobertura(@PathVariable Long id, @RequestBody CoberturaSocial cobertura) {
        try {
            CoberturaSocial actualizada = coberturaSocialServicio.actualizarCobertura(id, cobertura);
            return ResponseEntity.ok(actualizada);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // PUT /api/coberturas/{id}/baja-logica
  @PutMapping("/{id}/baja-logica")
  @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_COBERTURAS')")
    public ResponseEntity<?> bajaLogica(@PathVariable Long id) {
        try {
            CoberturaSocial coberturaInactiva = coberturaSocialServicio.bajaLogica(id);
            return ResponseEntity.ok(coberturaInactiva);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) { 
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    // PUT /api/coberturas/{id}/reactivar
    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_COBERTURAS')")
    public ResponseEntity<?> reactivarCobertura(@PathVariable Long id) {
        try {
            CoberturaSocial coberturaActiva = coberturaSocialServicio.reactivarCobertura(id);
            return ResponseEntity.ok(coberturaActiva);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
