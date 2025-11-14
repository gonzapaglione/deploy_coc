package com.clinica.clinica_coc.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        
        // Crea un cuerpo de respuesta simple
        Map<String, String> body = new HashMap<>();
        
        // Usa getReason() para obtener el mensaje ("El DNI ya está registrado.")
        body.put("message", ex.getReason()); 

        // Devuelve el cuerpo JSON con el estado HTTP original de la excepción (ej. 409)
        return new ResponseEntity<>(body, ex.getStatusCode());
    }
}
