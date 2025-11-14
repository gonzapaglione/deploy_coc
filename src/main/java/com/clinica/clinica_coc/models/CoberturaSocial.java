package com.clinica.clinica_coc.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_cob_social")

@Entity
@Table(name = "cob_social")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoberturaSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_cob_social;

    private String nombre_cobertura;

   @Column(name = "estado_cobertura", nullable = false, length = 20)
    private String estado_cobertura;

    // Relación inversa ManyToMany con Paciente
    @ManyToMany(mappedBy = "coberturas")
    private List<Paciente> pacientes;
}
