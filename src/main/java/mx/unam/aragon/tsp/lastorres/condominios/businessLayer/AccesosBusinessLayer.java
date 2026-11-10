package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.DatosEnMemoria;

/**
 * Bitácora de la caseta: entradas y salidas de visitantes.
 * Es el corazón del control de acceso.
 */
@Service
public class AccesosBusinessLayer {

    /** Hora de la CDMX aunque el servidor en la nube esté en otra zona. */
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");

    private final DatosEnMemoria datos;
    private final VisitantesBusinessLayer visitantesBusinessLayer;

    public AccesosBusinessLayer(DatosEnMemoria datos, VisitantesBusinessLayer visitantesBusinessLayer) {
        this.datos = datos;
        this.visitantesBusinessLayer = visitantesBusinessLayer;
    }

    /** Todos los accesos, del más reciente al más antiguo. */
    public List<AccesoDto> obtenerTodos() {
        return datos.getAccesos().stream()
                .sorted(Comparator.comparing(AccesoDto::getFechaEntrada).reversed())
                .toList();
    }

    /** Visitantes que entraron y todavía no registran su salida. */
    public List<AccesoDto> obtenerDentro() {
        return obtenerTodos().stream()
                .filter(AccesoDto::isDentro)
                .toList();
    }

    public AccesoDto obtenerPorId(int id) {
        return datos.getAccesos().stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("No existe el acceso con id " + id));
    }

    public IdDto registrarEntrada(AccesoDtoIn dtoIn) {
        VisitanteDto visitante = visitantesBusinessLayer.obtenerPorId(dtoIn.getVisitanteId());

        boolean yaEstaDentro = datos.getAccesos().stream()
                .anyMatch(a -> a.getVisitanteId() == visitante.getId() && a.isDentro());
        if (yaEstaDentro) {
            throw new ReglaNegocioException(
                    visitante.getNombre() + " " + visitante.getApellidos() + " ya está dentro del condominio");
        }

        int id = datos.getSiguienteIdAcceso().getAndIncrement();
        // Se guarda una "foto" del nombre y la vivienda para que la bitácora
        // no cambie aunque después se edite o borre el visitante.
        datos.getAccesos().add(new AccesoDto(
                id,
                visitante.getId(),
                visitante.getNombre() + " " + visitante.getApellidos(),
                visitante.getVivienda(),
                dtoIn.getMotivo().trim(),
                LocalDateTime.now(ZONA_HORARIA),
                null));

        return new IdDto(id, "Entrada registrada");
    }

    public IdDto registrarSalida(int id) {
        AccesoDto acceso = obtenerPorId(id);

        if (!acceso.isDentro()) {
            throw new ReglaNegocioException("La salida de este acceso ya se había registrado");
        }

        acceso.setFechaSalida(LocalDateTime.now(ZONA_HORARIA));
        return new IdDto(id, "Salida registrada");
    }
}
