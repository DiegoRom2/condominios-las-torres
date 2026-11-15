package mx.unam.aragon.tsp.lastorres.condominios.helpers;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mx.unam.aragon.tsp.lastorres.condominios.entities.AccesoEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.UsuarioEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.VisitanteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ViviendaEntity;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IAccesoRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IResidenteRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IUsuarioRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IVisitanteRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IViviendaRepository;

/**
 * Al arrancar, si la base de datos está vacía, carga datos de ejemplo
 * para que la API no aparezca vacía en la demo. Si ya hay datos, no hace nada.
 *
 * También crea los usuarios iniciales (admin y guardia) si no existe ninguno.
 * Sus contraseñas se leen de las variables de entorno ADMIN_PASSWORD y
 * GUARDIA_PASSWORD; si no están definidas, se genera una al azar y se
 * muestra una sola vez en el log de arranque.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final IViviendaRepository viviendaRepository;
    private final IResidenteRepository residenteRepository;
    private final IVisitanteRepository visitanteRepository;
    private final IAccesoRepository accesoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_PASSWORD:}")
    private String contrasenaAdmin;

    @Value("${GUARDIA_PASSWORD:}")
    private String contrasenaGuardia;

    public DatosIniciales(IViviendaRepository viviendaRepository, IResidenteRepository residenteRepository,
            IVisitanteRepository visitanteRepository, IAccesoRepository accesoRepository,
            IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.viviendaRepository = viviendaRepository;
        this.residenteRepository = residenteRepository;
        this.visitanteRepository = visitanteRepository;
        this.accesoRepository = accesoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        crearUsuarios();
        cargarDatosDeEjemplo();
    }

    private void crearUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        crearUsuario("admin", contrasenaAdmin, "Administración Las Torres", "ADMIN");
        crearUsuario("guardia", contrasenaGuardia, "Caseta de vigilancia", "GUARDIA");
    }

    private void crearUsuario(String usuario, String contrasena, String nombre, String rol) {
        if (contrasena == null || contrasena.isBlank()) {
            contrasena = contrasenaAlAzar();
            log.warn("Usuario '{}' creado con contraseña temporal: {}", usuario, contrasena);
        }
        usuarioRepository.save(new UsuarioEntity(usuario, passwordEncoder.encode(contrasena), nombre, rol));
    }

    private String contrasenaAlAzar() {
        String letras = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom azar = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            sb.append(letras.charAt(azar.nextInt(letras.length())));
        }
        return sb.toString();
    }

    private void cargarDatosDeEjemplo() {
        if (viviendaRepository.count() > 0) {
            return;
        }

        ViviendaEntity a101 = viviendaRepository.save(new ViviendaEntity("A", "101", 1));
        viviendaRepository.save(new ViviendaEntity("A", "102", 1));
        ViviendaEntity b201 = viviendaRepository.save(new ViviendaEntity("B", "201", 2));
        viviendaRepository.save(new ViviendaEntity("B", "202", 2));

        ResidenteEntity laura = residenteRepository.save(new ResidenteEntity(
                "Laura", "Hernández Ruiz", "laura.hernandez@ejemplo.com", "5511223344", a101));
        ResidenteEntity carlos = residenteRepository.save(new ResidenteEntity(
                "Carlos", "Mendoza López", "carlos.mendoza@ejemplo.com", "5599887766", b201));

        VisitanteEntity ana = visitanteRepository.save(new VisitanteEntity("Ana", "Torres Díaz", "ABC1234", laura));
        visitanteRepository.save(new VisitanteEntity("Jorge", "Ramírez Soto", "", carlos));

        accesoRepository.save(new AccesoEntity(ana, "Ana Torres Díaz", Mappers.etiqueta(a101), "Visita familiar",
                LocalDateTime.now(ZoneId.of("America/Mexico_City")).minusHours(1)));
    }
}
