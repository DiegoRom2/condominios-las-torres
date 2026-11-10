package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persona que vive en una vivienda del condominio.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResidenteDto {
    private int id;
    private String nombre;
    private String apellidos;
    private String correo;
    private String telefono;
    private int viviendaId;
    /** Ejemplo: "Torre A - 101" */
    private String vivienda;
}
