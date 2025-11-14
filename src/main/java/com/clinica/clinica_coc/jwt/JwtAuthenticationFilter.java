package com.clinica.clinica_coc.jwt;

import java.io.IOException;
import java.util.List; 
import java.util.stream.Collectors; 
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.clinica.clinica_coc.repositories.PersonaRepositorio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PersonaRepositorio personaRepositorio;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String token = getTokenFromRequest(request);
        final String userEmail;

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtService.getUsernameFromToken(token);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var personaOpt = personaRepositorio.findByEmailWithRoles(userEmail);

                if (personaOpt.isPresent()) {
                    UserDetails userDetails = personaOpt.get(); 

                    if (jwtService.isTokenValid(token, userDetails)) {

                        // Extraemos los PERMISOS del token (ej. "PERM_RESERVAR_TURNO")
                        List<String> permisos = jwtService.getPermisosFromToken(token);
                        
                        // Convertimos esa lista de Strings a una colección de GrantedAuthority
                        var authorities = permisos.stream()
                                .map(permiso -> new SimpleGrantedAuthority(permiso))
                                .collect(Collectors.toList());

                        // Creamos el token de autenticación USANDO los permisos del token,
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities); 
                        
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("[Filtro JWT] ERROR: El token es INVÁLIDO.");
                    }
                } else {
                    System.out.println("[Filtro JWT] ERROR: El usuario '" + userEmail + "' del token NO existe en la BD.");
                }
            }
        } catch (Exception e) {
            System.out.println("[Filtro JWT] ERROR: Excepción al procesar el token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")) {
            return authHeader.substring(7);
        } else {
            return null;
        }
    }
}