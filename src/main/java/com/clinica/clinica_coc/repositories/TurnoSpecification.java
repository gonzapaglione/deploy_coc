package com.clinica.clinica_coc.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import com.clinica.clinica_coc.models.Paciente;
import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.models.Turno;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class TurnoSpecification {

    /**
     * Construye una especificación de JPA dinámica para filtrar turnos.
     */
    public static Specification<Turno> build(
            String pacienteNombre,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<String> estados,
            Long odontologoId) {

        return (root, query, cb) -> {
            
            // Optimización N+1: Hacemos los JOIN FETCH
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("paciente", JoinType.LEFT).fetch("persona", JoinType.LEFT);
                root.fetch("odontologo", JoinType.LEFT).fetch("persona", JoinType.LEFT);
                query.distinct(true); // Evitar duplicados por los fetches
            }

            // Empezamos con un predicado vacío (WHERE 1=1)
            Predicate predicate = cb.conjunction();

            // --- 1. Filtro por Nombre de Paciente ---
            if (pacienteNombre != null && !pacienteNombre.trim().isEmpty()) {
                Join<Turno, Paciente> pacienteJoin = root.join("paciente");
                Join<Paciente, Persona> personaJoin = pacienteJoin.join("persona");
                
                // Concatenamos nombre y apellido para buscar
                jakarta.persistence.criteria.Expression<String> nombreCompleto = cb.concat(personaJoin.get("nombre"), " ");
                nombreCompleto = cb.concat(nombreCompleto, personaJoin.get("apellido"));
                
                predicate = cb.and(predicate,
                        cb.like(cb.lower(nombreCompleto), "%" + pacienteNombre.toLowerCase() + "%"));
            }

            // --- 2. Filtro por Fecha Inicio ---
            // (Asumimos que fechaInicio nunca es nulo, el frontend siempre manda "hoy")
            if (fechaInicio != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("fechaHora"), fechaInicio.atStartOfDay()));
            }

            // --- 3. Filtro por Fecha Fin ---
            if (fechaFin != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("fechaHora"), fechaFin.atTime(LocalTime.MAX)));
            }

            // --- 4. Filtro por Estados ---
            if (estados != null && !estados.isEmpty()) {
                predicate = cb.and(predicate, root.get("estadoTurno").in(estados));
            }
            if (odontologoId != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("odontologo").get("id_odontologo"), odontologoId));
            }

            return predicate;
        };
    }
}
