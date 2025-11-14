package com.clinica.clinica_coc.auth;

import java.util.List;

// import com.clinica.clinica_coc.models.CoberturaSocial; // ya no se usa aquí

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    String nombre;
    String apellido;
    String email;
    String domicilio;
    String telefono;
    Long dni;
    String password;
    // IDs de coberturas seleccionadas en el formulario de registro (Paciente)
    List<Long> coberturaIds;

}
