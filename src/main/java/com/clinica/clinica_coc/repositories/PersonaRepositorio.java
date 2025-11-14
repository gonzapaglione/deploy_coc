package com.clinica.clinica_coc.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinica.clinica_coc.models.Persona;

@Repository
public interface PersonaRepositorio extends JpaRepository<Persona, Long> {
    Optional<Persona> findByEmail(String email);

    // Cargar persona junto con sus roles para evitar LazyInitializationException
    @Query("select p from Persona p left join fetch p.personaRolList pr left join fetch pr.idRol where p.email = :email")
    Optional<Persona> findByEmailWithRoles(@Param("email") String email);

   Optional<Persona> existsByEmail(String email);
   Optional<Persona> findByDni(Long dni);
}
