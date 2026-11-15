package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ViviendaEntity;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.Mappers;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IResidenteRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IViviendaRepository;

/**
 * Reglas de negocio de las viviendas (departamentos) de Las Torres.
 */
@Service
public class ViviendasBusinessLayer {

    private final IViviendaRepository viviendaRepository;
    private final IResidenteRepository residenteRepository;

    public ViviendasBusinessLayer(IViviendaRepository viviendaRepository, IResidenteRepository residenteRepository) {
        this.viviendaRepository = viviendaRepository;
        this.residenteRepository = residenteRepository;
    }

    public List<ViviendaDto> obtenerTodas() {
        return viviendaRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(Mappers::toDto)
                .toList();
    }

    public ViviendaDto obtenerPorId(int id) {
        return Mappers.toDto(obtenerEntidad(id));
    }

    /** Lo usan otros business layers para validar que la vivienda exista. */
    public ViviendaEntity obtenerEntidad(int id) {
        return viviendaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la vivienda con id " + id));
    }

    @Transactional
    public IdDto agregar(ViviendaDtoIn dtoIn) {
        String torre = dtoIn.getTorre().trim().toUpperCase();
        String numero = dtoIn.getNumero().trim();
        validarQueNoExista(torre, numero, 0);

        ViviendaEntity entity = viviendaRepository.save(new ViviendaEntity(torre, numero, dtoIn.getPiso()));

        return new IdDto(entity.getId(), "Vivienda registrada");
    }

    @Transactional
    public IdDto actualizar(int id, ViviendaDtoIn dtoIn) {
        ViviendaEntity entity = obtenerEntidad(id);
        String torre = dtoIn.getTorre().trim().toUpperCase();
        String numero = dtoIn.getNumero().trim();
        validarQueNoExista(torre, numero, id);

        entity.setTorre(torre);
        entity.setNumero(numero);
        entity.setPiso(dtoIn.getPiso());
        viviendaRepository.save(entity);

        return new IdDto(id, "Vivienda actualizada");
    }

    @Transactional
    public IdDto borrar(int id) {
        ViviendaEntity entity = obtenerEntidad(id);

        if (residenteRepository.existsByVivienda_IdAndActivoTrue(id)) {
            throw new ReglaNegocioException(
                    "No se puede borrar " + Mappers.etiqueta(entity) + " porque tiene residentes registrados");
        }

        // Borrado lógico: se desactiva en lugar de eliminar el registro
        entity.setActivo(false);
        viviendaRepository.save(entity);

        return new IdDto(id, "Vivienda borrada");
    }

    /** No puede haber dos departamentos con la misma torre y número. */
    private void validarQueNoExista(String torre, String numero, int idActual) {
        if (viviendaRepository.existsByTorreIgnoreCaseAndNumeroIgnoreCaseAndActivoTrueAndIdNot(torre, numero, idActual)) {
            throw new ReglaNegocioException("Ya existe la vivienda Torre " + torre + " - " + numero);
        }
    }
}
