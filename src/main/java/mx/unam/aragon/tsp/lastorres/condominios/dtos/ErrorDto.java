package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta cuando algo sale mal (datos inválidos, registro inexistente, etc.).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDto {
    private int estatus;
    private String mensaje;
    private List<String> detalles;
}
