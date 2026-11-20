package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lo que llena una persona en el formulario público para pedir su cuenta de residente.
 * El comprobante de domicilio llega aparte, como archivo.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegistroDtoIn {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede tener más de 60 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80, message = "Los apellidos no pueden tener más de 80 caracteres")
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 120, message = "El correo no puede tener más de 120 caracteres")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
    private String telefono;

    @NotNull(message = "Elige tu vivienda")
    private Integer viviendaId;

    @NotBlank(message = "El usuario es obligatorio")
    @Pattern(regexp = "[a-zA-Z0-9._-]{4,40}",
            message = "El usuario debe tener de 4 a 40 caracteres: letras, números, punto, guion o guion bajo")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    private String contrasena;

    @NotBlank(message = "Confirma tu contraseña")
    private String confirmarContrasena;
}
