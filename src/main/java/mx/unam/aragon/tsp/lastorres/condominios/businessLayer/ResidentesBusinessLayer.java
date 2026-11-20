package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ViviendaEntity;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.Mappers;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IResidenteRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IUsuarioRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IVisitanteRepository;

/**
 * Reglas de negocio de los residentes.
 */
@Service
public class ResidentesBusinessLayer {

    private final IResidenteRepository residenteRepository;
    private final IVisitanteRepository visitanteRepository;
    private final ViviendasBusinessLayer viviendasBusinessLayer;
    private final IUsuarioRepository usuarioRepository;

    public ResidentesBusinessLayer(IResidenteRepository residenteRepository,
            IVisitanteRepository visitanteRepository,
            ViviendasBusinessLayer viviendasBusinessLayer,
            IUsuarioRepository usuarioRepository) {
        this.residenteRepository = residenteRepository;
        this.visitanteRepository = visitanteRepository;
        this.viviendasBusinessLayer = viviendasBusinessLayer;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ResidenteDto> obtenerTodos() {
        return residenteRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(Mappers::toDto)
                .toList();
    }

    public ResidenteDto obtenerPorId(int id) {
        return Mappers.toDto(obtenerEntidad(id));
    }

    /** Lo usan otros business layers para validar que el residente exista. */
    public ResidenteEntity obtenerEntidad(int id) {
        return residenteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el residente con id " + id));
    }

    @Transactional
    public IdDto agregar(ResidenteDtoIn dtoIn) {
        // Si la vivienda no existe, esto lanza el error 404
        ViviendaEntity vivienda = viviendasBusinessLayer.obtenerEntidad(dtoIn.getViviendaId());
        String correo = dtoIn.getCorreo().trim().toLowerCase();
        validarCorreoUnico(correo, 0);

        ResidenteEntity entity = residenteRepository.save(new ResidenteEntity(
                dtoIn.getNombre().trim(),
                dtoIn.getApellidos().trim(),
                correo,
                dtoIn.getTelefono(),
                vivienda));

        return new IdDto(entity.getId(), "Residente registrado");
    }

    @Transactional
    public IdDto actualizar(int id, ResidenteDtoIn dtoIn) {
        ResidenteEntity entity = obtenerEntidad(id);
        ViviendaEntity vivienda = viviendasBusinessLayer.obtenerEntidad(dtoIn.getViviendaId());
        String correo = dtoIn.getCorreo().trim().toLowerCase();
        validarCorreoUnico(correo, id);

        entity.setNombre(dtoIn.getNombre().trim());
        entity.setApellidos(dtoIn.getApellidos().trim());
        entity.setCorreo(correo);
        entity.setTelefono(dtoIn.getTelefono());
        entity.setVivienda(vivienda);
        residenteRepository.save(entity);

        return new IdDto(id, "Residente actualizado");
    }

    @Transactional
    public IdDto borrar(int id) {
        ResidenteEntity entity = obtenerEntidad(id);

        if (visitanteRepository.existsByResidente_IdAndActivoTrue(id)) {
            throw new ReglaNegocioException(
                    "No se puede borrar al residente porque tiene visitantes autorizados");
        }

        entity.setActivo(false);
        residenteRepository.save(entity);

        // Si ya no vive aquí, su cuenta tampoco debe seguir funcionando
        usuarioRepository.findByResidente_IdAndActivoTrue(id).forEach(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        });

        return new IdDto(id, "Residente borrado");
    }

    /** Dos residentes no pueden compartir correo (ahí llegarán las notificaciones). */
    private void validarCorreoUnico(String correo, int idActual) {
        if (residenteRepository.existsByCorreoIgnoreCaseAndActivoTrueAndIdNot(correo, idActual)) {
            throw new ReglaNegocioException("Ya hay un residente registrado con el correo " + correo);
        }
    }
}
