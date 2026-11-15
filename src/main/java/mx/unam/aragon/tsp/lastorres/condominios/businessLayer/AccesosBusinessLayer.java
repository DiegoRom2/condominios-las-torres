package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.entities.AccesoEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.VisitanteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.Mappers;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IAccesoRepository;

/**
 * Bitácora de la caseta: entradas y salidas de visitantes.
 * Es el corazón del control de acceso.
 */
@Service
public class AccesosBusinessLayer {

    /** Hora de la CDMX aunque el servidor en la nube esté en otra zona. */
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");

    private final IAccesoRepository accesoRepository;
    private final VisitantesBusinessLayer visitantesBusinessLayer;

    public AccesosBusinessLayer(IAccesoRepository accesoRepository, VisitantesBusinessLayer visitantesBusinessLayer) {
        this.accesoRepository = accesoRepository;
        this.visitantesBusinessLayer = visitantesBusinessLayer;
    }

    /** Todos los accesos, del más reciente al más antiguo. */
    public List<AccesoDto> obtenerTodos() {
        return accesoRepository.findAllByOrderByFechaEntradaDesc().stream()
                .map(Mappers::toDto)
                .toList();
    }

    /** Visitantes que entraron y todavía no registran su salida. */
    public List<AccesoDto> obtenerDentro() {
        return accesoRepository.findByFechaSalidaIsNullOrderByFechaEntradaDesc().stream()
                .map(Mappers::toDto)
                .toList();
    }

    public AccesoDto obtenerPorId(int id) {
        return Mappers.toDto(obtenerEntidad(id));
    }

    @Transactional
    public IdDto registrarEntrada(AccesoDtoIn dtoIn) {
        VisitanteEntity visitante = visitantesBusinessLayer.obtenerEntidad(dtoIn.getVisitanteId());
        String nombre = visitante.getNombre() + " " + visitante.getApellidos();

        if (accesoRepository.existsByVisitante_IdAndFechaSalidaIsNull(visitante.getId())) {
            throw new ReglaNegocioException(nombre + " ya está dentro del condominio");
        }

        AccesoEntity entity = accesoRepository.save(new AccesoEntity(
                visitante,
                nombre,
                Mappers.etiqueta(visitante.getResidente().getVivienda()),
                dtoIn.getMotivo().trim(),
                LocalDateTime.now(ZONA_HORARIA)));

        return new IdDto(entity.getId(), "Entrada registrada");
    }

    @Transactional
    public IdDto registrarSalida(int id) {
        AccesoEntity entity = obtenerEntidad(id);

        if (entity.getFechaSalida() != null) {
            throw new ReglaNegocioException("La salida de este acceso ya se había registrado");
        }

        entity.setFechaSalida(LocalDateTime.now(ZONA_HORARIA));
        accesoRepository.save(entity);

        return new IdDto(id, "Salida registrada");
    }

    private AccesoEntity obtenerEntidad(int id) {
        return accesoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el acceso con id " + id));
    }
}
