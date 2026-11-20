package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Motivo que escribe la administración al rechazar una solicitud (se le envía por correo al solicitante).
 */
@Getter
@Setter
@NoArgsConstructor
public class RechazoDtoIn {

    @NotBlank(message = "Escribe el motivo del rechazo")
    @Size(max = 200, message = "El motivo no puede tener más de 200 caracteres")
    private String motivo;
}
