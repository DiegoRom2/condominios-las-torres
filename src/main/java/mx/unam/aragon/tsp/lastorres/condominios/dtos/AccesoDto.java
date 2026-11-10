package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de una entrada (y su salida) de un visitante en la caseta.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccesoDto {
    private int id;
    private int visitanteId;
    private String visitante;
    private String vivienda;
    private String motivo;
    private LocalDateTime fechaEntrada;
    /** Vacío mientras el visitante sigue dentro. */
    private LocalDateTime fechaSalida;

    public boolean isDentro() {
        return fechaSalida == null;
    }
}
