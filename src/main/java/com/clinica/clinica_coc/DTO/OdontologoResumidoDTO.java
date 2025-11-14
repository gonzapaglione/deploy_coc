package com.clinica.clinica_coc.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OdontologoResumidoDTO {
    private Long id_odontologo;
    private String nombre;
    private String apellido;

    // Método de conveniencia
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}