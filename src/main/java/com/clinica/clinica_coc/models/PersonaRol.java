package com.clinica.clinica_coc.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "persona_rol")

@Data
public class PersonaRol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_per_rol;

    @JoinColumn(name = "id_persona", referencedColumnName = "id_persona", nullable = false)
    @ManyToOne(optional = false)
    private Persona idPersona;

    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol", nullable = false)
    @ManyToOne(optional = false)
    private Rol idRol;

    @Override
    public String toString(){
        return "Id persona_ rol: "+ id_per_rol;
    }
}
