package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos que manda el usuario para crear o actualizar una vivienda.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViviendaDtoIn {

    @NotBlank(message = "La torre es obligatoria")
    @Size(max = 10, message = "La torre no puede tener más de 10 caracteres")
    private String torre;

    @NotBlank(message = "El número de departamento es obligatorio")
    @Size(max = 10, message = "El número no puede tener más de 10 caracteres")
    private String numero;

    @Min(value = 0, message = "El piso no puede ser negativo")
    @Max(value = 50, message = "El piso no puede ser mayor a 50")
    private int piso;
}
