package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persona externa autorizada por un residente para entrar al condominio.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VisitanteDto {
    private int id;
    private String nombre;
    private String apellidos;
    /** Placas del auto, si llega en vehículo (puede ir vacío). */
    private String placas;
    /** Residente que autoriza la visita. */
    private int residenteId;
    private String residente;
    private String vivienda;
}
