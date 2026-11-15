package mx.unam.aragon.tsp.lastorres.condominios.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mx.unam.aragon.tsp.lastorres.condominios.entities.AccesoEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.VisitanteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ViviendaEntity;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IAccesoRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IResidenteRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IVisitanteRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IViviendaRepository;

/**
 * Al arrancar, si la base de datos está vacía, carga datos de ejemplo
 * para que la API no aparezca vacía en la demo. Si ya hay datos, no hace nada.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final IViviendaRepository viviendaRepository;
    private final IResidenteRepository residenteRepository;
    private final IVisitanteRepository visitanteRepository;
    private final IAccesoRepository accesoRepository;

    public DatosIniciales(IViviendaRepository viviendaRepository, IResidenteRepository residenteRepository,
            IVisitanteRepository visitanteRepository, IAccesoRepository accesoRepository) {
        this.viviendaRepository = viviendaRepository;
        this.residenteRepository = residenteRepository;
        this.visitanteRepository = visitanteRepository;
        this.accesoRepository = accesoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
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
