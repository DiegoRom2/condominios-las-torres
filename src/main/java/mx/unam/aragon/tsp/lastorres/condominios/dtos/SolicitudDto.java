package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Solicitud de cuenta de residente, como la ve la administración.
 * Nunca incluye la contraseña.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudDto {
    private int id;
    private String nombre;
    private String apellidos;
    private String correo;
    private String telefono;
    private String vivienda;
    private String usuario;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRevision;
    private String revisadoPor;
    private String motivoRechazo;
    private String comprobanteNombre;
    /** Si ya existe un residente con ese correo, a quién se ligará la cuenta al aprobar. */
    private String residenteExistente;

    public boolean isPendiente() {
        return "PENDIENTE".equals(estado);
    }
}
