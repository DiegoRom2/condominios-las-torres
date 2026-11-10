package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.util.List;

import org.springframework.stereotype.Service;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.DatosEnMemoria;

/**
 * Reglas de negocio de las viviendas (departamentos) de Las Torres.
 */
@Service
public class ViviendasBusinessLayer {

    private final DatosEnMemoria datos;

    public ViviendasBusinessLayer(DatosEnMemoria datos) {
        this.datos = datos;
    }

    public List<ViviendaDto> obtenerTodas() {
        return datos.getViviendas();
    }

    public ViviendaDto obtenerPorId(int id) {
        return datos.getViviendas().stream()
                .filter(v -> v.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("No existe la vivienda con id " + id));
    }

    public IdDto agregar(ViviendaDtoIn dtoIn) {
        String torre = dtoIn.getTorre().trim().toUpperCase();
        String numero = dtoIn.getNumero().trim();
        validarQueNoExista(torre, numero, 0);

        int id = datos.getSiguienteIdVivienda().getAndIncrement();
        datos.getViviendas().add(new ViviendaDto(id, torre, numero, dtoIn.getPiso()));

        return new IdDto(id, "Vivienda registrada");
    }

    public IdDto actualizar(int id, ViviendaDtoIn dtoIn) {
        ViviendaDto vivienda = obtenerPorId(id);
        String torre = dtoIn.getTorre().trim().toUpperCase();
        String numero = dtoIn.getNumero().trim();
        validarQueNoExista(torre, numero, id);

        vivienda.setTorre(torre);
        vivienda.setNumero(numero);
        vivienda.setPiso(dtoIn.getPiso());

        return new IdDto(id, "Vivienda actualizada");
    }

    public IdDto borrar(int id) {
        ViviendaDto vivienda = obtenerPorId(id);

        boolean tieneResidentes = datos.getResidentes().stream()
                .anyMatch(r -> r.getViviendaId() == id);
        if (tieneResidentes) {
            throw new ReglaNegocioException(
                    "No se puede borrar " + vivienda.getEtiqueta() + " porque tiene residentes registrados");
        }

        datos.getViviendas().remove(vivienda);
        return new IdDto(id, "Vivienda borrada");
    }

    /** No puede haber dos departamentos con la misma torre y número. */
    private void validarQueNoExista(String torre, String numero, int idActual) {
        boolean duplicada = datos.getViviendas().stream()
                .anyMatch(v -> v.getId() != idActual
                        && v.getTorre().equalsIgnoreCase(torre)
                        && v.getNumero().equalsIgnoreCase(numero));
        if (duplicada) {
            throw new ReglaNegocioException("Ya existe la vivienda Torre " + torre + " - " + numero);
        }
    }
}
