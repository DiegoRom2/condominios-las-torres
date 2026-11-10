package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.util.List;

import org.springframework.stereotype.Service;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.DatosEnMemoria;

/**
 * Reglas de negocio de los visitantes autorizados.
 */
@Service
public class VisitantesBusinessLayer {

    private final DatosEnMemoria datos;
    private final ResidentesBusinessLayer residentesBusinessLayer;

    public VisitantesBusinessLayer(DatosEnMemoria datos, ResidentesBusinessLayer residentesBusinessLayer) {
        this.datos = datos;
        this.residentesBusinessLayer = residentesBusinessLayer;
    }

    public List<VisitanteDto> obtenerTodos() {
        return datos.getVisitantes().stream()
                .map(this::completarResidente)
                .toList();
    }

    public VisitanteDto obtenerPorId(int id) {
        VisitanteDto visitante = datos.getVisitantes().stream()
                .filter(v -> v.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("No existe el visitante con id " + id));
        return completarResidente(visitante);
    }

    public IdDto agregar(VisitanteDtoIn dtoIn) {
        // Solo un residente existente puede autorizar visitas
        residentesBusinessLayer.obtenerPorId(dtoIn.getResidenteId());

        int id = datos.getSiguienteIdVisitante().getAndIncrement();
        datos.getVisitantes().add(new VisitanteDto(
                id,
                dtoIn.getNombre().trim(),
                dtoIn.getApellidos().trim(),
                limpiarPlacas(dtoIn.getPlacas()),
                dtoIn.getResidenteId(),
                null,
                null));

        return new IdDto(id, "Visitante registrado");
    }

    public IdDto actualizar(int id, VisitanteDtoIn dtoIn) {
        VisitanteDto visitante = obtenerPorId(id);
        residentesBusinessLayer.obtenerPorId(dtoIn.getResidenteId());

        visitante.setNombre(dtoIn.getNombre().trim());
        visitante.setApellidos(dtoIn.getApellidos().trim());
        visitante.setPlacas(limpiarPlacas(dtoIn.getPlacas()));
        visitante.setResidenteId(dtoIn.getResidenteId());

        return new IdDto(id, "Visitante actualizado");
    }

    public IdDto borrar(int id) {
        VisitanteDto visitante = obtenerPorId(id);

        boolean estaDentro = datos.getAccesos().stream()
                .anyMatch(a -> a.getVisitanteId() == id && a.isDentro());
        if (estaDentro) {
            throw new ReglaNegocioException(
                    "No se puede borrar al visitante mientras está dentro del condominio; registra primero su salida");
        }

        datos.getVisitantes().remove(visitante);
        return new IdDto(id, "Visitante borrado");
    }

    private String limpiarPlacas(String placas) {
        return placas == null ? "" : placas.trim().toUpperCase();
    }

    /** Agrega el nombre del residente que autoriza y su vivienda. */
    private VisitanteDto completarResidente(VisitanteDto visitante) {
        datos.getResidentes().stream()
                .filter(r -> r.getId() == visitante.getResidenteId())
                .findFirst()
                .ifPresent(residente -> {
                    visitante.setResidente(residente.getNombre() + " " + residente.getApellidos());
                    datos.getViviendas().stream()
                            .filter(v -> v.getId() == residente.getViviendaId())
                            .findFirst()
                            .ifPresent(v -> visitante.setVivienda(v.getEtiqueta()));
                });
        return visitante;
    }
}
