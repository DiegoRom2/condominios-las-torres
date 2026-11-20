package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.VisitanteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.Mappers;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IAccesoRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IVisitanteRepository;

/**
 * Reglas de negocio de los visitantes autorizados.
 */
@Service
public class VisitantesBusinessLayer {

    private final IVisitanteRepository visitanteRepository;
    private final IAccesoRepository accesoRepository;
    private final ResidentesBusinessLayer residentesBusinessLayer;

    public VisitantesBusinessLayer(IVisitanteRepository visitanteRepository,
            IAccesoRepository accesoRepository,
            ResidentesBusinessLayer residentesBusinessLayer) {
        this.visitanteRepository = visitanteRepository;
        this.accesoRepository = accesoRepository;
        this.residentesBusinessLayer = residentesBusinessLayer;
    }

    public List<VisitanteDto> obtenerTodos() {
        return visitanteRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(Mappers::toDto)
                .toList();
    }

    public VisitanteDto obtenerPorId(int id) {
        return Mappers.toDto(obtenerEntidad(id));
    }

    /** Visitantes que autorizó un residente (pantalla "Mis visitantes"). */
    public List<VisitanteDto> obtenerDeResidente(int residenteId) {
        return visitanteRepository.findByResidente_IdAndActivoTrueOrderByIdAsc(residenteId).stream()
                .map(Mappers::toDto)
                .toList();
    }

    /**
     * Un visitante solo si lo autorizó ese residente. Si es de otro, responde
     * "no existe" (404): así nadie puede ver o cambiar visitantes ajenos cambiando el número en la URL.
     */
    public VisitanteDto obtenerPropio(int id, int residenteId) {
        VisitanteEntity entity = obtenerEntidad(id);
        if (entity.getResidente().getId() != residenteId) {
            throw new NoEncontradoException("No existe el visitante con id " + id);
        }
        return Mappers.toDto(entity);
    }

    /** Lo usa la caseta (AccesosBusinessLayer) para validar que el visitante exista. */
    public VisitanteEntity obtenerEntidad(int id) {
        return visitanteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el visitante con id " + id));
    }

    @Transactional
    public IdDto agregar(VisitanteDtoIn dtoIn) {
        // Solo un residente existente puede autorizar visitas
        ResidenteEntity residente = residentesBusinessLayer.obtenerEntidad(dtoIn.getResidenteId());

        VisitanteEntity entity = visitanteRepository.save(new VisitanteEntity(
                dtoIn.getNombre().trim(),
                dtoIn.getApellidos().trim(),
                limpiarPlacas(dtoIn.getPlacas()),
                residente));

        return new IdDto(entity.getId(), "Visitante registrado");
    }

    @Transactional
    public IdDto actualizar(int id, VisitanteDtoIn dtoIn) {
        VisitanteEntity entity = obtenerEntidad(id);
        ResidenteEntity residente = residentesBusinessLayer.obtenerEntidad(dtoIn.getResidenteId());

        entity.setNombre(dtoIn.getNombre().trim());
        entity.setApellidos(dtoIn.getApellidos().trim());
        entity.setPlacas(limpiarPlacas(dtoIn.getPlacas()));
        entity.setResidente(residente);
        visitanteRepository.save(entity);

        return new IdDto(id, "Visitante actualizado");
    }

    @Transactional
    public IdDto borrar(int id) {
        VisitanteEntity entity = obtenerEntidad(id);

        if (accesoRepository.existsByVisitante_IdAndFechaSalidaIsNull(id)) {
            throw new ReglaNegocioException(
                    "No se puede borrar al visitante mientras está dentro del condominio; registra primero su salida");
        }

        entity.setActivo(false);
        visitanteRepository.save(entity);

        return new IdDto(id, "Visitante borrado");
    }

    private String limpiarPlacas(String placas) {
        return placas == null ? "" : placas.trim().toUpperCase();
    }
}
