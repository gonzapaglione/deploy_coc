package com.clinica.clinica_coc.DTO;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDTO {
    private Long id_persona;
    private String nombre;
    private String apellido;
    private Long dni;
    private String email;
    private String password;
    private String domicilio;
    private String telefono;
    private String isActive = "Activo";

    private List<RolDTO> roles;
}
