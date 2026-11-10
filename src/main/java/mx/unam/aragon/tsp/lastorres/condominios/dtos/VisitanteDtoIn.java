package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VisitanteDtoIn {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede tener más de 60 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80, message = "Los apellidos no pueden tener más de 80 caracteres")
    private String apellidos;

    @Size(max = 10, message = "Las placas no pueden tener más de 10 caracteres")
    private String placas;

    @NotNull(message = "El residente que autoriza es obligatorio")
    private Integer residenteId;
}
