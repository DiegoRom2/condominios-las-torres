package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.util.List;

import org.springframework.stereotype.Service;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.DatosEnMemoria;

/**
 * Reglas de negocio de los residentes.
 */
@Service
public class ResidentesBusinessLayer {

    private final DatosEnMemoria datos;
    private final ViviendasBusinessLayer viviendasBusinessLayer;

    public ResidentesBusinessLayer(DatosEnMemoria datos, ViviendasBusinessLayer viviendasBusinessLayer) {
        this.datos = datos;
        this.viviendasBusinessLayer = viviendasBusinessLayer;
    }

    public List<ResidenteDto> obtenerTodos() {
        return datos.getResidentes().stream()
                .map(this::completarVivienda)
                .toList();
    }

    public ResidenteDto obtenerPorId(int id) {
        ResidenteDto residente = datos.getResidentes().stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("No existe el residente con id " + id));
        return completarVivienda(residente);
    }

    public IdDto agregar(ResidenteDtoIn dtoIn) {
        // Si la vivienda no existe, esto lanza el error 404
        viviendasBusinessLayer.obtenerPorId(dtoIn.getViviendaId());
        validarCorreoUnico(dtoIn.getCorreo(), 0);

        int id = datos.getSiguienteIdResidente().getAndIncrement();
        datos.getResidentes().add(new ResidenteDto(
                id,
                dtoIn.getNombre().trim(),
                dtoIn.getApellidos().trim(),
                dtoIn.getCorreo().trim().toLowerCase(),
                dtoIn.getTelefono(),
                dtoIn.getViviendaId(),
                null));

        return new IdDto(id, "Residente registrado");
    }

    public IdDto actualizar(int id, ResidenteDtoIn dtoIn) {
        ResidenteDto residente = obtenerPorId(id);
        viviendasBusinessLayer.obtenerPorId(dtoIn.getViviendaId());
        validarCorreoUnico(dtoIn.getCorreo(), id);

        residente.setNombre(dtoIn.getNombre().trim());
        residente.setApellidos(dtoIn.getApellidos().trim());
        residente.setCorreo(dtoIn.getCorreo().trim().toLowerCase());
        residente.setTelefono(dtoIn.getTelefono());
        residente.setViviendaId(dtoIn.getViviendaId());

        return new IdDto(id, "Residente actualizado");
    }

    public IdDto borrar(int id) {
        ResidenteDto residente = obtenerPorId(id);

        boolean tieneVisitantes = datos.getVisitantes().stream()
                .anyMatch(v -> v.getResidenteId() == id);
        if (tieneVisitantes) {
            throw new ReglaNegocioException(
                    "No se puede borrar al residente porque tiene visitantes autorizados");
        }

        datos.getResidentes().remove(residente);
        return new IdDto(id, "Residente borrado");
    }

    /** Dos residentes no pueden compartir correo (ahí llegarán las notificaciones). */
    private void validarCorreoUnico(String correo, int idActual) {
        boolean repetido = datos.getResidentes().stream()
                .anyMatch(r -> r.getId() != idActual && r.getCorreo().equalsIgnoreCase(correo.trim()));
        if (repetido) {
            throw new ReglaNegocioException("Ya hay un residente registrado con el correo " + correo);
        }
    }

    /** Agrega el texto "Torre A - 101" para que la respuesta sea más clara. */
    private ResidenteDto completarVivienda(ResidenteDto residente) {
        datos.getViviendas().stream()
                .filter(v -> v.getId() == residente.getViviendaId())
                .findFirst()
                .ifPresent(v -> residente.setVivienda(v.getEtiqueta()));
        return residente;
    }
}
