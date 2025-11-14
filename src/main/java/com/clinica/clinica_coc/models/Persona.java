package com.clinica.clinica_coc.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persona")
@Builder
@Data // genera getters, setters, toString, equals y hashCode
@NoArgsConstructor
@AllArgsConstructor

public class Persona implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_persona;

    private String nombre;
    private String apellido;
    @Column(name = "DNI", unique = true)
    private Long dni;

    private String email;
    private String password;

    private String domicilio;
    private String telefono;

    @Column(name = "is_active", nullable = false, columnDefinition = "ENUM('Activo','Inactivo') default 'Activo'")
    private String isActive = "Activo";

    @OneToMany(mappedBy = "idPersona")
    private List<PersonaRol> personaRolList = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapear cada PersonaRol al nombre del rol y envolverlo en
        // SimpleGrantedAuthority
        return personaRolList.stream()
                .map(pr -> pr.getIdRol())
                .filter(rol -> rol != null && rol.getNombre_rol() != null)
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre_rol()))
                .toList();

    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Asume que isActive == "Activo" significa habilitado
        return "Activo".equalsIgnoreCase(this.isActive);
    }

    @Override
public String toString() {
    // Solo imprime campos locales, NUNCA la lista de roles
    return "Persona [id=" + id_persona + ", nombre=" + nombre + ", apellido=" + apellido + "]";
}

}
