package com.clinica.clinica_coc.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.clinica.clinica_coc.models.PersonaRol;

@Repository
public interface PersonaRolRepositorio extends JpaRepository<PersonaRol, Long> {
   @Query("SELECT pr FROM PersonaRol pr WHERE pr.idPersona.id_persona = :idPersona AND pr.idRol.id_rol = :idRol")
    List<PersonaRol> findSpecificRolesForPersona(
        @Param("idPersona") Long idPersona, 
        @Param("idRol") Long idRol
    );

}