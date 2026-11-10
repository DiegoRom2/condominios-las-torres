package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lo que captura el guardia al registrar una entrada.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccesoDtoIn {

    @NotNull(message = "El visitante es obligatorio")
    private Integer visitanteId;

    @NotBlank(message = "El motivo de la visita es obligatorio")
    @Size(max = 120, message = "El motivo no puede tener más de 120 caracteres")
    private String motivo;
}
