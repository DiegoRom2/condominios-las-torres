package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Departamento dentro del condominio Las Torres (lo que regresa la API).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViviendaDto {
    private int id;
    private String torre;
    private String numero;
    private int piso;

    /** Texto amigable, por ejemplo "Torre A - 101". */
    public String getEtiqueta() {
        return "Torre " + torre + " - " + numero;
    }
}
