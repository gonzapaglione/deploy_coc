package com.clinica.clinica_coc.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaBasicDTO {
    private Long id_persona;
    private String nombre;
    private String apellido;
    private Long dni;
    private String email;
    private String domicilio;
    private String telefono;
    private String isActive = "Activo";
}
