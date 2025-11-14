
package com.clinica.clinica_coc.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.clinica.clinica_coc.models.CoberturaSocial;


 
@Repository
public interface CoberturaSocialRepositorio extends JpaRepository<CoberturaSocial, Long> {

@Query(value = "SELECT * FROM cob_social WHERE nombre_cobertura = :nombreBusqueda", nativeQuery = true)
    Optional<CoberturaSocial> findByNombreNativoConParam(@Param("nombreBusqueda") String nombre);    

    
   @Query("SELECT c FROM CoberturaSocial c WHERE c.estado_cobertura = :estado")
List<CoberturaSocial> findByEstadoCobertura(@Param("estado") String estado);
}
