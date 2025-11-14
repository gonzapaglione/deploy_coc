package com.clinica.clinica_coc.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaRequest {
    private String nombre;
    private String apellido;
    private Long dni;
    private String email;
    private String password;
    private String domicilio;
    private String telefono;
    private String isActive; // OPCIONAL: estado de la persona
    private List<Long> rolesIds; // OPCIONAL: lista de IDs de roles
}
