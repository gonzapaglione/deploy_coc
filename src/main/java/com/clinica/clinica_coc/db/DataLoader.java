package com.clinica.clinica_coc.db;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import com.clinica.clinica_coc.models.CoberturaSocial;
import com.clinica.clinica_coc.models.DiaSemana;
import com.clinica.clinica_coc.models.Especialidad;
import com.clinica.clinica_coc.models.EspecialidadOdontologo;
import com.clinica.clinica_coc.models.Horario;
import com.clinica.clinica_coc.models.MotivoConsultaEnum;
import com.clinica.clinica_coc.models.Odontologo;
import com.clinica.clinica_coc.models.Paciente;
import com.clinica.clinica_coc.models.Persona;
import com.clinica.clinica_coc.models.PersonaRol;
import com.clinica.clinica_coc.models.Rol;
import com.clinica.clinica_coc.models.Turno;
import com.clinica.clinica_coc.repositories.CoberturaSocialRepositorio;
import com.clinica.clinica_coc.repositories.EspecialidadOdontologoRepositorio;
import com.clinica.clinica_coc.repositories.EspecialidadRepositorio;
import com.clinica.clinica_coc.repositories.HorarioRepositorio;
import com.clinica.clinica_coc.repositories.OdontologoRepositorio;
import com.clinica.clinica_coc.repositories.PacienteRepositorio;
import com.clinica.clinica_coc.repositories.PersonaRepositorio;
import com.clinica.clinica_coc.repositories.PersonaRolRepositorio;
import com.clinica.clinica_coc.repositories.RolRepositorio;
import com.clinica.clinica_coc.repositories.TurnoRepositorio;

@Component
public class DataLoader implements CommandLineRunner {

        @Autowired
        private RolRepositorio rolRepositorio;

        @Autowired
        private EspecialidadRepositorio especialidadRepositorio;

        @Autowired
        private CoberturaSocialRepositorio coberturaRepositorio;

        @Autowired
        private PersonaRepositorio personaRepositorio;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private PacienteRepositorio pacienteRepositorio;

        @Autowired
        private OdontologoRepositorio odontologoRepositorio;

        @Autowired
        private PersonaRolRepositorio personaRolRepositorio;

        @Autowired
        private EspecialidadOdontologoRepositorio especialidadOdontologoRepositorio;

        @Autowired
        private TurnoRepositorio turnoRepositorio;

        @Autowired
        private HorarioRepositorio horarioRepositorio;

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                System.out.println(">>> Iniciando precarga de datos...");

                if (datosPrecargados()) {
                        System.out.println(">>> Datos de prueba ya existentes. Saltando precarga completa");
                        return;
                }

                precargarRoles();
                precargarEspecialidades();
                precargarCoberturas();

                precargarPersonasYUsuarios();
                crearHorariosDemo();
                System.out.println(">>> Precarga completada exitosamente");
        }

        private void precargarRoles() {
                if (rolRepositorio.count() == 0) {
                        rolRepositorio.saveAll(List.of(
                                        new Rol(null, "Paciente", new ArrayList<>()),
                                        new Rol(null, "Odontologo", new ArrayList<>()),
                                        new Rol(null, "Admin", new ArrayList<>())));
                        System.out.println("   - Roles precargados");
                }
        }

        private void precargarEspecialidades() {
                if (especialidadRepositorio.count() == 0) {
                        especialidadRepositorio.saveAll(List.of(
                                        new Especialidad(null, "Ortodoncia", "Activo"),
                                        new Especialidad(null, "Endodoncia", "Activo"),
                                        new Especialidad(null, "Periodoncia", "Activo"),
                                        new Especialidad(null, "Implantología", "Activo")));
                        System.out.println("   - Especialidades precargadas");
                }
        }

        private void precargarCoberturas() {
                if (coberturaRepositorio.count() == 0) {
                        coberturaRepositorio.saveAll(List.of(
                                        new CoberturaSocial(null, "Particular", "Activo", new ArrayList<>()), // ID 1
                                        new CoberturaSocial(null, "OSDE", "Activo", new ArrayList<>()), // ID 2
                                        new CoberturaSocial(null, "PAMI", "Activo", new ArrayList<>()), // ID 3
                                        new CoberturaSocial(null, "Galeno", "Activo", new ArrayList<>()), // ID 4
                                        new CoberturaSocial(null, "SwissMedical", "Activo", new ArrayList<>()), // ID 5
                                        new CoberturaSocial(null, "OSPE", "Activo", new ArrayList<>()), // ID 6
                                        new CoberturaSocial(null, "IOSEP", "Activo", new ArrayList<>()) // ID 7

                        ));
                        System.out.println("   - Coberturas precargadas");
                }
        }

        private void precargarPersonasYUsuarios() {
                if (personaRepositorio.count() > 0) {
                        System.out.println("   - Personas ya existentes. Saltando precarga de usuarios.");
                        return;
                }

                PersonasIniciales personas = crearPersonasBasicas();
                PacientesIniciales pacientes = crearPacientes(personas);
                OdontologosIniciales odontologos = crearOdontologos(personas);

                asignarRoles(personas);
                asignarEspecialidades(odontologos);
                crearTurnosDemo(pacientes, odontologos);

                System.out.println("   - Personas, pacientes, odontólogos y turnos precargados");
        }

        private PersonasIniciales crearPersonasBasicas() {
                Persona per1 = new Persona(null, "Gonzalo", "Lopez Paglione", 30111222L, "paciente1@gmail.com",
                                passwordEncoder.encode("paciente"), "Av. Siempreviva 123", "111-222", "Activo",
                                new ArrayList<>());
                Persona per2 = new Persona(null, "Lourdes", "Guerrieri Viaña", 30122333L, "paciente2@gmail.com",
                                passwordEncoder.encode("paciente"), "Calle 9 456", "111-333", "Activo",
                                new ArrayList<>());
                Persona per3 = new Persona(null, "Lautaro", "Mercado", 30133444L, "paciente3@gmail.com",
                                passwordEncoder.encode("paciente"), "Bv. San Martín 12", "111-444", "Activo",
                                new ArrayList<>());
                Persona per4 = new Persona(null, "Dr. Diego", "Ruiz", 40111222L, "odontologo1@gmail.com",
                                passwordEncoder.encode("odontologo"), "Calle Doc 1", "222-111", "Activo",
                                new ArrayList<>());
                Persona per5 = new Persona(null, "Dra. Laura", "Sosa", 40122333L, "odontologo2@gmail.com",
                                passwordEncoder.encode("odontologo"), "Calle Doc 2", "222-333", "Activo",
                                new ArrayList<>());
                Persona per6 = new Persona(null, "Admin", "Sistema", 50000000L, "admin@gmail.com",
                                passwordEncoder.encode("administrador"), "Oficina", "000-000", "Activo",
                                new ArrayList<>());

 
                Persona per7 = new Persona(null, "Dra. Ana", "Gomez", 40133444L, "odontologo3@gmail.com",
                                passwordEncoder.encode("odontologo"), "Calle Doc 3", "222-444", "Activo",
                                new ArrayList<>());
                Persona per8 = new Persona(null, "Dr. Carlos", "Vera", 40144555L, "odontologo4@gmail.com",
                                passwordEncoder.encode("odontologo"), "Calle Doc 4", "222-555", "Activo",
                                new ArrayList<>());
                Persona per9 = new Persona(null, "María", "Sol", 30144555L, "paciente4@gmail.com",
                                passwordEncoder.encode("paciente"), "Calle Falsa 123", "111-555", "Activo",
                                new ArrayList<>());
                Persona per10 = new Persona(null, "Juan", "Perez", 30155666L, "paciente5@gmail.com",
                                passwordEncoder.encode("paciente"), "Av. Colón 456", "111-666", "Activo",
                                new ArrayList<>());

                Persona per11 = new Persona(null, "Ana Clara", "Díaz", 30166777L, "paciente6@gmail.com",
                                passwordEncoder.encode("paciente"), "Av. Belgrano 789", "111-777", "Activo",
                                new ArrayList<>());
                Persona per12 = new Persona(null, "Martín", "Bosa", 30177888L, "paciente7@gmail.com",
                                passwordEncoder.encode("paciente"), "Calle San Juan 321", "111-888", "Activo",
                                new ArrayList<>());

                personaRepositorio.saveAll(List.of(per1, per2, per3, per4, per5, per6, per7, per8, per9, per10, per11, per12));
                return new PersonasIniciales(per1, per2, per3, per4, per5, per6, per7, per8, per9, per10, per11, per12);
        }

        private PacientesIniciales crearPacientes(PersonasIniciales personas) {

                Optional<CoberturaSocial> particularOpt = coberturaRepositorio.findByNombreNativoConParam("Particular");
                Optional<CoberturaSocial> osdeOpt = coberturaRepositorio.findByNombreNativoConParam("OSDE");
                Optional<CoberturaSocial> pamiOpt = coberturaRepositorio.findByNombreNativoConParam("PAMI");
                Optional<CoberturaSocial> galenoOpt = coberturaRepositorio.findByNombreNativoConParam("Galeno");
                Optional<CoberturaSocial> swissOpt = coberturaRepositorio.findByNombreNativoConParam("SwissMedical");
                Optional<CoberturaSocial> iosepOpt = coberturaRepositorio.findByNombreNativoConParam("IOSEP");

                CoberturaSocial particular = particularOpt.orElse(null);
                CoberturaSocial osde = osdeOpt.orElse(null);
                CoberturaSocial pami = pamiOpt.orElse(null);
                CoberturaSocial galeno = galenoOpt.orElse(null);
                CoberturaSocial swiss = swissOpt.orElse(null);
                CoberturaSocial iosep = iosepOpt.orElse(null);


                Paciente pac1 = new Paciente(null, personas.paciente1(), "Activo",
                                osde != null ? List.of(osde) : new ArrayList<>());
                Paciente pac2 = new Paciente(null, personas.paciente2(), "Activo",
                                pami != null ? List.of(pami) : new ArrayList<>());
                Paciente pac3 = new Paciente(null, personas.paciente3(), "Activo",
                                osde != null && pami != null ? List.of(osde, pami) : new ArrayList<>());
                
                // --- NUEVOS PACIENTES ---
                Paciente pac4 = new Paciente(null, personas.paciente4(), "Activo",
                                galeno != null ? List.of(galeno) : new ArrayList<>());
                Paciente pac5 = new Paciente(null, personas.paciente5(), "Activo",
                                swiss != null ? List.of(swiss, particular) : new ArrayList<>()); 
                Paciente pac6 = new Paciente(null, personas.paciente6(), "Activo",
                                particular != null ? List.of(particular) : new ArrayList<>());
                Paciente pac7 = new Paciente(null, personas.paciente7(), "Activo",
                                iosep != null ? List.of(iosep) : new ArrayList<>());

                pacienteRepositorio.saveAll(List.of(pac1, pac2, pac3, pac4, pac5, pac6, pac7));
                return new PacientesIniciales(pac1, pac2, pac3, pac4, pac5, pac6, pac7, particular, osde, pami, galeno, swiss, iosep);
        }

        private OdontologosIniciales crearOdontologos(PersonasIniciales personas) {
                Odontologo od1 = new Odontologo(null, personas.odontologo1(), new ArrayList<>(), "Activo");
                Odontologo od2 = new Odontologo(null, personas.odontologo2(), new ArrayList<>(), "Activo");
                Odontologo od3 = new Odontologo(null, personas.odontologo3(), new ArrayList<>(), "Activo");
                Odontologo od4 = new Odontologo(null, personas.odontologo4(), new ArrayList<>(), "Activo");

                odontologoRepositorio.saveAll(List.of(od1, od2, od3, od4));
                return new OdontologosIniciales(od1, od2, od3, od4);
        }

        private void asignarRoles(PersonasIniciales personas) {
                List<Rol> roles = rolRepositorio.findAll();
                Rol rolPaciente = roles.stream().filter(r -> "Paciente".equalsIgnoreCase(r.getNombre_rol()))
                                .findFirst().orElse(null);
                Rol rolOdontologo = roles.stream().filter(r -> "Odontologo".equalsIgnoreCase(r.getNombre_rol()))
                                .findFirst().orElse(null);
                Rol rolAdmin = roles.stream().filter(r -> "Admin".equalsIgnoreCase(r.getNombre_rol()))
                                .findFirst().orElse(null);

                List<PersonaRol> personaRoles = new ArrayList<>();
                if (rolPaciente != null) {
                        personaRoles.add(construirPersonaRol(personas.paciente1(), rolPaciente));
                        personaRoles.add(construirPersonaRol(personas.paciente2(), rolPaciente));
                        personaRoles.add(construirPersonaRol(personas.paciente3(), rolPaciente));
                        personaRoles.add(construirPersonaRol(personas.paciente4(), rolPaciente)); 
                        personaRoles.add(construirPersonaRol(personas.paciente5(), rolPaciente)); 
                        personaRoles.add(construirPersonaRol(personas.paciente6(), rolPaciente)); 
                        personaRoles.add(construirPersonaRol(personas.paciente7(), rolPaciente)); 
                }
                if (rolOdontologo != null) {
                        personaRoles.add(construirPersonaRol(personas.odontologo1(), rolOdontologo));
                        personaRoles.add(construirPersonaRol(personas.odontologo2(), rolOdontologo));
                        personaRoles.add(construirPersonaRol(personas.odontologo3(), rolOdontologo)); 
                        personaRoles.add(construirPersonaRol(personas.odontologo4(), rolOdontologo)); 
                }
                if (rolAdmin != null) {
                        personaRoles.add(construirPersonaRol(personas.admin(), rolAdmin));
                }

                if (!personaRoles.isEmpty()) {
                        personaRolRepositorio.saveAll(personaRoles);
                }
        }

        private void asignarEspecialidades(OdontologosIniciales odontologos) {
                Optional<Especialidad> orto = especialidadRepositorio.findByNombre("Ortodoncia");
                Optional<Especialidad> endo = especialidadRepositorio.findByNombre("Endodoncia");
                Optional<Especialidad> perio = especialidadRepositorio.findByNombre("Periodoncia"); 
                Optional<Especialidad> impl = especialidadRepositorio.findByNombre("Implantología"); 
                
                Especialidad ortodoncia = orto.orElse(null);
                Especialidad endodoncia = endo.orElse(null);
                Especialidad periodoncia = perio.orElse(null);
                Especialidad implantologia = impl.orElse(null); 


                List<EspecialidadOdontologo> especialidadesAsignadas = new ArrayList<>();
                if (ortodoncia != null) {
                        especialidadesAsignadas
                                        .add(new EspecialidadOdontologo(null, odontologos.odontologo1(), ortodoncia));
                        especialidadesAsignadas
                                        .add(new EspecialidadOdontologo(null, odontologos.odontologo2(), ortodoncia));
                }
                if (endodoncia != null) {
                        especialidadesAsignadas
                                        .add(new EspecialidadOdontologo(null, odontologos.odontologo2(), endodoncia));
                }
         
                if (periodoncia != null) {
                        especialidadesAsignadas
                                        .add(new EspecialidadOdontologo(null, odontologos.odontologo3(), periodoncia));
                }
                if (implantologia != null) {
                        especialidadesAsignadas
                                        .add(new EspecialidadOdontologo(null, odontologos.odontologo4(), implantologia));
                        especialidadesAsignadas
                                        .add(new EspecialidadOdontologo(null, odontologos.odontologo1(), implantologia)); // od1 tiene 2
                }

                if (!especialidadesAsignadas.isEmpty()) {
                        especialidadOdontologoRepositorio.saveAll(especialidadesAsignadas);
                }
        }

       private void crearTurnosDemo(PacientesIniciales pacientes, OdontologosIniciales odontologos) {
                
                // --- OBTENEMOS "PARTICULAR" PARA CORREGIR EL TURNO NULL ---
                Optional<CoberturaSocial> particularOpt = coberturaRepositorio.findByNombreNativoConParam("Particular");
                CoberturaSocial particular = particularOpt.orElseThrow(() -> new RuntimeException("Cobertura Particular no encontrada"));


                List<Turno> turnos = new ArrayList<>();
                LocalDateTime momentoActual = LocalDateTime.now();

                // --- TURNOS ORIGINALES (4) ---
                turnos.add(new Turno(null, pacientes.paciente1(), odontologos.odontologo1(), pacientes.osde(),
                                momentoActual.minusDays(10).withHour(18).withMinute(0).withSecond(0).withNano(0),
                                "ATENDIDO", MotivoConsultaEnum.REVISION_PERIODICA, "Limpieza realizada", "OK"));
                turnos.add(new Turno(null, pacientes.paciente2(), odontologos.odontologo2(), pacientes.pami(),
                                momentoActual.plusDays(5).withHour(8).withMinute(0).withSecond(0).withNano(0),
                                "PROXIMO", MotivoConsultaEnum.CARIES, null, null));
                turnos.add(new Turno(null, pacientes.paciente3(), odontologos.odontologo1(), pacientes.osde(),
                                momentoActual.plusDays(2).withHour(17).withMinute(30).withSecond(0).withNano(0),
                                "CANCELADO", MotivoConsultaEnum.OTRO, null, null));
                
                // --- TURNO CORREGIDO (YA NO ES NULL) ---
                turnos.add(new Turno(null, pacientes.paciente1(), odontologos.odontologo2(), particular, //
                                momentoActual.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0),
                                "PROXIMO", MotivoConsultaEnum.DOLOR_DENTAL, null, null));

                // --- NUEVOS 15 TURNOS (La mayoría 'PROXIMO') ---
                
                // Turnos para Odontologo 3 (Dra. Ana Gomez)
                turnos.add(new Turno(null, pacientes.paciente4(), odontologos.odontologo3(), pacientes.galeno(),
                                momentoActual.plusDays(3).withHour(10).withMinute(0), "PROXIMO", MotivoConsultaEnum.GINGIVITIS, null, null));
                turnos.add(new Turno(null, pacientes.paciente5(), odontologos.odontologo3(), pacientes.swiss(),
                                momentoActual.plusDays(3).withHour(10).withMinute(30), "PROXIMO", MotivoConsultaEnum.LIMPIEZA_DENTAL, null, null));
                turnos.add(new Turno(null, pacientes.paciente6(), odontologos.odontologo3(), pacientes.particular(),
                                momentoActual.minusDays(7).withHour(11).withMinute(0), "ATENDIDO", MotivoConsultaEnum.REVISION_PERIODICA, "Control OK", "Paciente refiere sensibilidad"));
                
                // Turnos para Odontologo 4 (Dr. Carlos Vera)
                turnos.add(new Turno(null, pacientes.paciente7(), odontologos.odontologo4(), pacientes.iosep(),
                                momentoActual.plusDays(4).withHour(15).withMinute(0), "PROXIMO", MotivoConsultaEnum.IMPLANTES, null, null));
                turnos.add(new Turno(null, pacientes.paciente1(), odontologos.odontologo4(), pacientes.osde(),
                                momentoActual.plusDays(4).withHour(15).withMinute(30), "PROXIMO", MotivoConsultaEnum.ORTODONCIA, null, null));
                turnos.add(new Turno(null, pacientes.paciente2(), odontologos.odontologo4(), pacientes.pami(),
                                momentoActual.minusDays(2).withHour(16).withMinute(0), "SIN_ASISTIR", MotivoConsultaEnum.DOLOR_DENTAL, null, null));

                // Más turnos para Odontologo 1 (Dr. Diego Ruiz)
                turnos.add(new Turno(null, pacientes.paciente3(), odontologos.odontologo1(), pacientes.osde(),
                                momentoActual.plusDays(6).withHour(18).withMinute(0), "PROXIMO", MotivoConsultaEnum.BLANQUEAMIENTO, null, null));
                turnos.add(new Turno(null, pacientes.paciente4(), odontologos.odontologo1(), pacientes.galeno(),
                                momentoActual.plusDays(8).withHour(19).withMinute(0), "PROXIMO", MotivoConsultaEnum.REVISION_PERIODICA, null, null));
                
                // Más turnos para Odontologo 2 (Dra. Laura Sosa)
                turnos.add(new Turno(null, pacientes.paciente5(), odontologos.odontologo2(), pacientes.swiss(),
                                momentoActual.plusDays(7).withHour(9).withMinute(30), "PROXIMO", MotivoConsultaEnum.FRACTURA_DENTAL, null, null));
                turnos.add(new Turno(null, pacientes.paciente6(), odontologos.odontologo2(), pacientes.particular(),
                                momentoActual.plusDays(7).withHour(10).withMinute(0), "PROXIMO", MotivoConsultaEnum.CARIES, null, null));
                turnos.add(new Turno(null, pacientes.paciente7(), odontologos.odontologo2(), pacientes.iosep(),
                                momentoActual.minusDays(5).withHour(11).withMinute(0), "ATENDIDO", MotivoConsultaEnum.EXTRACCION_MUELAS_JUICIO, "Extracción sin complicaciones", "OK"));

                // Turnos variados adicionales
                turnos.add(new Turno(null, pacientes.paciente1(), odontologos.odontologo3(), pacientes.osde(),
                                momentoActual.plusDays(10).withHour(12).withMinute(0), "PROXIMO", MotivoConsultaEnum.DOLOR_DENTAL, null, null));
                turnos.add(new Turno(null, pacientes.paciente2(), odontologos.odontologo4(), pacientes.pami(),
                                momentoActual.plusDays(11).withHour(17).withMinute(0), "PROXIMO", MotivoConsultaEnum.LIMPIEZA_DENTAL, null, null));
                turnos.add(new Turno(null, pacientes.paciente4(), odontologos.odontologo1(), pacientes.galeno(),
                                momentoActual.minusDays(1).withHour(18).withMinute(30), "ATENDIDO", MotivoConsultaEnum.OTRO, "Consulta urgencia", "Se deriva a especialista"));
                turnos.add(new Turno(null, pacientes.paciente5(), odontologos.odontologo2(), pacientes.particular(),
                                momentoActual.plusDays(9).withHour(8).withMinute(30), "PROXIMO", MotivoConsultaEnum.CARIES, null, null));


                turnos.add(new Turno(null, pacientes.paciente7(), odontologos.odontologo4(), pacientes.iosep(), 
                                momentoActual.minusDays(8).withHour(16).withMinute(0), "ATENDIDO", MotivoConsultaEnum.IMPLANTES, "Inicio de tratamiento", "Buena oseointegración"));

                turnos.add(new Turno(null, pacientes.paciente6(), odontologos.odontologo3(), pacientes.particular(), 
                                momentoActual.minusDays(15).withHour(10).withMinute(0), "ATENDIDO", MotivoConsultaEnum.LIMPIEZA_DENTAL, "Limpieza profunda", "OK"));
                

                turnos.add(new Turno(null, pacientes.paciente4(), odontologos.odontologo2(), pacientes.galeno(), 
                                momentoActual.minusDays(4).withHour(11).withMinute(0), "CANCELADO", MotivoConsultaEnum.CARIES, null, null));

                turnos.add(new Turno(null, pacientes.paciente5(), odontologos.odontologo1(), pacientes.swiss(), 
                                momentoActual.plusDays(3).withHour(18).withMinute(0), "CANCELADO", MotivoConsultaEnum.ORTODONCIA, null, null));

                turnos.add(new Turno(null, pacientes.paciente3(), odontologos.odontologo3(), pacientes.osde(), 
                                momentoActual.minusDays(5).withHour(9).withMinute(30), "SIN_ASISTIR", MotivoConsultaEnum.REVISION_PERIODICA, null, null));

                turnos.add(new Turno(null, pacientes.paciente1(), odontologos.odontologo4(), pacientes.osde(), 
                                momentoActual.minusDays(2).withHour(15).withMinute(0), "SIN_ASISTIR", MotivoConsultaEnum.DOLOR_DENTAL, null, null));

                
                turnoRepositorio.saveAll(turnos);
        }
        private PersonaRol construirPersonaRol(Persona persona, Rol rol) {
                PersonaRol personaRol = new PersonaRol();
                personaRol.setIdPersona(persona);
                personaRol.setIdRol(rol);
                return personaRol;
        }

        private void crearHorariosDemo() {
                Odontologo odontologo1 = odontologoRepositorio.findByEmail("odontologo1@gmail.com");
                Odontologo odontologo2 = odontologoRepositorio.findByEmail("odontologo2@gmail.com");
                Odontologo odontologo3 = odontologoRepositorio.findByEmail("odontologo3@gmail.com"); 
                Odontologo odontologo4 = odontologoRepositorio.findByEmail("odontologo4@gmail.com"); 

                List<Horario> horariosNuevos = new ArrayList<>();

                if (odontologo1 != null
                                && horarioRepositorio.findHorariosPorOdontologo(odontologo1.getId_odontologo())
                                                .isEmpty()) {
                        horariosNuevos.add(crearHorario(odontologo1, DiaSemana.Lunes, LocalTime.of(17, 0),
                                        LocalTime.of(21, 0)));
                        horariosNuevos.add(crearHorario(odontologo1, DiaSemana.Miércoles, LocalTime.of(17, 0),
                                        LocalTime.of(21, 0)));
                        horariosNuevos.add(crearHorario(odontologo1, DiaSemana.Viernes, LocalTime.of(17, 0),
                                        LocalTime.of(21, 0)));
                }

                if (odontologo2 != null
                                && horarioRepositorio.findHorariosPorOdontologo(odontologo2.getId_odontologo())
                                                .isEmpty()) {
                        horariosNuevos.add(crearHorario(odontologo2, DiaSemana.Martes, LocalTime.of(7, 0),
                                        LocalTime.of(12, 0)));
                        horariosNuevos.add(crearHorario(odontologo2, DiaSemana.Jueves, LocalTime.of(7, 0),
                                        LocalTime.of(12, 0)));
                        horariosNuevos.add(crearHorario(odontologo2, DiaSemana.Viernes, LocalTime.of(7, 0),
                                        LocalTime.of(12, 0)));
                }

                if (odontologo3 != null
                                && horarioRepositorio.findHorariosPorOdontologo(odontologo3.getId_odontologo())
                                                .isEmpty()) {
                        horariosNuevos.add(crearHorario(odontologo3, DiaSemana.Lunes, LocalTime.of(9, 0),
                                        LocalTime.of(13, 0)));
                        horariosNuevos.add(crearHorario(odontologo3, DiaSemana.Miércoles, LocalTime.of(9, 0),
                                        LocalTime.of(13, 0)));
                }

                if (odontologo4 != null
                                && horarioRepositorio.findHorariosPorOdontologo(odontologo4.getId_odontologo())
                                                .isEmpty()) {
                        horariosNuevos.add(crearHorario(odontologo4, DiaSemana.Martes, LocalTime.of(14, 0),
                                        LocalTime.of(19, 0)));
                        horariosNuevos.add(crearHorario(odontologo4, DiaSemana.Jueves, LocalTime.of(14, 0),
                                        LocalTime.of(19, 0)));
                }


                if (!horariosNuevos.isEmpty()) {
                        horarioRepositorio.saveAll(horariosNuevos);
                        System.out.println("   - Horarios de odontólogos precargados");
                }
        }

        private Horario crearHorario(Odontologo odontologo, DiaSemana dia, LocalTime inicio, LocalTime fin) {
                Horario horario = new Horario();
                horario.setOdontologo(odontologo);
                horario.setDiaSemana(dia);
                horario.setHoraInicio(inicio);
                horario.setHoraFin(fin);
                horario.setDuracionTurno(30);
                return horario;
        }

        private boolean datosPrecargados() {
                return rolRepositorio.count() > 0
                                && especialidadRepositorio.count() > 0
                                && coberturaRepositorio.count() > 0
                                && personaRepositorio.count() > 0
                                && pacienteRepositorio.count() > 0
                                && odontologoRepositorio.count() > 0
                                && personaRolRepositorio.count() > 0
                                && especialidadOdontologoRepositorio.count() > 0
                                && turnoRepositorio.count() > 0
                                && horarioRepositorio.count() > 0;
        }


        private record PersonasIniciales(
                        Persona paciente1, Persona paciente2, Persona paciente3,
                        Persona odontologo1, Persona odontologo2, Persona admin,
                        Persona odontologo3, Persona odontologo4, 
                        Persona paciente4, Persona paciente5, Persona paciente6, Persona paciente7 
        ) {
        }

        private record PacientesIniciales(
                        Paciente paciente1, Paciente paciente2, Paciente paciente3,
                        Paciente paciente4, Paciente paciente5, Paciente paciente6, Paciente paciente7, 
                        CoberturaSocial particular, CoberturaSocial osde, CoberturaSocial pami, 
                        CoberturaSocial galeno, CoberturaSocial swiss, CoberturaSocial iosep
                        ) {
        }

        private record OdontologosIniciales(
                        Odontologo odontologo1, Odontologo odontologo2,
                        Odontologo odontologo3, Odontologo odontologo4 
                        ) {
        }

}