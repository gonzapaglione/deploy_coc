package com.clinica.clinica_coc.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    // Admin

    // Odontologo
    @Autowired
    private DashboardServicio dashboardServicio;

    @GetMapping("/admin/resumen")
    @PreAuthorize("hasAuthority('PERM_GESTIONAR_TURNOS_ADMIN')")
    public ResponseEntity<DashboardAdminDTO> obtenerDashboardAdmin() {
        return ResponseEntity.ok(dashboardServicio.obtenerDashboardAdmin());
    }

    @GetMapping("/paciente/resumen")
    @PreAuthorize("hasAuthority('PERM_VER_MIS_TURNOS')")
    public ResponseEntity<DashboardPacienteDTO> obtenerDashboardPaciente(Authentication authentication) {
        DashboardPacienteDTO resumen = dashboardServicio.obtenerDashboardPacientePorEmail(authentication.getName());
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/odontologo/{idOdontologo}/atendidos")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_TURNOS_OD', 'PERM_GESTIONAR_TURNOS_ADMIN')")
    public ResponseEntity<Long> contarTurnosAtendidosPorOdontologo(@PathVariable Long idOdontologo) {
        long cantidad = dashboardServicio.contarTurnosAtendidosPorOdontologo(idOdontologo);
        return ResponseEntity.ok(cantidad);
    }

    @GetMapping("/odontologo/resumen")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_TURNOS_OD', 'PERM_GESTIONAR_TURNOS_ADMIN')")
    public ResponseEntity<DashboardOdontDTO> obtenerDashboardOdontologo(
            Authentication authentication,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        DashboardOdontDTO resumen = dashboardServicio.obtenerDashboardOdontologoPorEmail(authentication.getName(),
                fecha);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/odontologo/{idOdontologo}/pendientes")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_TURNOS_OD', 'PERM_GESTIONAR_TURNOS_ADMIN')")
    public ResponseEntity<Long> contarTurnosPendientesPorFecha(
            @PathVariable Long idOdontologo,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        long cantidad = dashboardServicio.contarTurnosPendientesPorOdontologoEnFecha(idOdontologo, fecha);
        return ResponseEntity.ok(cantidad);
    }

    @GetMapping("/estadisticas/coberturas")
    @PreAuthorize("hasAnyAuthority('PERM_GESTIONAR_TURNOS_OD', 'PERM_GESTIONAR_TURNOS_ADMIN')")
    public ResponseEntity<List<TurnosPorObraSocialDTO>> obtenerTurnosPorCobertura() {
        List<TurnosPorObraSocialDTO> resultado = dashboardServicio.obtenerTurnosPorCobertura();
        if (resultado.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultado);
    }

    // Paciente
}
