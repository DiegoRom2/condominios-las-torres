package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta genérica al crear, actualizar o borrar un registro.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdDto {
    private int id;
    private String mensaje;
}
