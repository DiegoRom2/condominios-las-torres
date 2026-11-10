package mx.unam.aragon.tsp.lastorres.condominios.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import lombok.Getter;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;

/**
 * TEMPORAL: hace las veces de base de datos mientras no conectamos Neon.
 * Los datos se pierden al reiniciar la aplicación.
 * En el siguiente avance esta clase se reemplaza por entities + repositories (JPA).
 */
@Component
@Getter
public class DatosEnMemoria {

    private final List<ViviendaDto> viviendas = new CopyOnWriteArrayList<>();
    private final List<ResidenteDto> residentes = new CopyOnWriteArrayList<>();
    private final List<VisitanteDto> visitantes = new CopyOnWriteArrayList<>();
    private final List<AccesoDto> accesos = new CopyOnWriteArrayList<>();

    private final AtomicInteger siguienteIdVivienda = new AtomicInteger(1);
    private final AtomicInteger siguienteIdResidente = new AtomicInteger(1);
    private final AtomicInteger siguienteIdVisitante = new AtomicInteger(1);
    private final AtomicInteger siguienteIdAcceso = new AtomicInteger(1);

    public DatosEnMemoria() {
        cargarDatosDePrueba();
    }

    /** Datos iniciales para que la API no arranque vacía en la demo. */
    private void cargarDatosDePrueba() {
        viviendas.add(new ViviendaDto(siguienteIdVivienda.getAndIncrement(), "A", "101", 1));
        viviendas.add(new ViviendaDto(siguienteIdVivienda.getAndIncrement(), "A", "102", 1));
        viviendas.add(new ViviendaDto(siguienteIdVivienda.getAndIncrement(), "B", "201", 2));
        viviendas.add(new ViviendaDto(siguienteIdVivienda.getAndIncrement(), "B", "202", 2));

        residentes.add(new ResidenteDto(siguienteIdResidente.getAndIncrement(),
                "Laura", "Hernández Ruiz", "laura.hernandez@ejemplo.com", "5511223344", 1, null));
        residentes.add(new ResidenteDto(siguienteIdResidente.getAndIncrement(),
                "Carlos", "Mendoza López", "carlos.mendoza@ejemplo.com", "5599887766", 3, null));

        visitantes.add(new VisitanteDto(siguienteIdVisitante.getAndIncrement(),
                "Ana", "Torres Díaz", "ABC1234", 1, null, null));
        visitantes.add(new VisitanteDto(siguienteIdVisitante.getAndIncrement(),
                "Jorge", "Ramírez Soto", "", 2, null, null));

        accesos.add(new AccesoDto(siguienteIdAcceso.getAndIncrement(),
                1, "Ana Torres Díaz", "Torre A - 101", "Visita familiar",
                LocalDateTime.now(ZoneId.of("America/Mexico_City")).minusHours(1), null));
    }
}
