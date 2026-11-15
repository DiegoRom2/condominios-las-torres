package mx.unam.aragon.tsp.lastorres.condominios.helpers;

import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.entities.AccesoEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.VisitanteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ViviendaEntity;

/**
 * Convierte las entities (tablas) en DTOs (lo que regresa la API).
 */
public class Mappers {

    public static ViviendaDto toDto(ViviendaEntity entity) {
        return new ViviendaDto(
                entity.getId(),
                entity.getTorre(),
                entity.getNumero(),
                entity.getPiso());
    }

    public static ResidenteDto toDto(ResidenteEntity entity) {
        ViviendaEntity vivienda = entity.getVivienda();
        return new ResidenteDto(
                entity.getId(),
                entity.getNombre(),
                entity.getApellidos(),
                entity.getCorreo(),
                entity.getTelefono(),
                vivienda.getId(),
                etiqueta(vivienda));
    }

    public static VisitanteDto toDto(VisitanteEntity entity) {
        ResidenteEntity residente = entity.getResidente();
        return new VisitanteDto(
                entity.getId(),
                entity.getNombre(),
                entity.getApellidos(),
                entity.getPlacas(),
                residente.getId(),
                residente.getNombre() + " " + residente.getApellidos(),
                etiqueta(residente.getVivienda()));
    }

    public static AccesoDto toDto(AccesoEntity entity) {
        return new AccesoDto(
                entity.getId(),
                entity.getVisitante().getId(),
                entity.getNombreVisitante(),
                entity.getVivienda(),
                entity.getMotivo(),
                entity.getFechaEntrada(),
                entity.getFechaSalida());
    }

    /** Texto amigable de una vivienda, por ejemplo "Torre A - 101". */
    public static String etiqueta(ViviendaEntity vivienda) {
        return "Torre " + vivienda.getTorre() + " - " + vivienda.getNumero();
    }
}
