package mx.unam.aragon.tsp.lastorres.condominios.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla solicitudes_registro: personas que piden una cuenta de residente.
 * Quedan PENDIENTES hasta que la administración revisa su comprobante y las aprueba o rechaza.
 */
@Entity
@Table(name = "solicitudes_registro")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudRegistroEntity {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(nullable = false, length = 10)
    private String telefono;

    /** Vivienda en la que dice vivir. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "vivienda_id", nullable = false)
    private ViviendaEntity vivienda;

    /** Nombre de usuario que eligió para iniciar sesión. */
    @Column(nullable = false, length = 40)
    private String usuario;

    /** Contraseña ya cifrada con BCrypt. */
    @Column(nullable = false, length = 100)
    private String contrasena;

    /** Comprobante de domicilio (id en la tabla archivos). */
    @Column(name = "comprobante_id", nullable = false)
    private Integer comprobanteId;

    @Column(name = "comprobante_nombre", nullable = false, length = 100)
    private String comprobanteNombre;

    /** PENDIENTE, APROBADA o RECHAZADA. */
    @Column(nullable = false, length = 20)
    private String estado = PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    /** Usuario administrador que la revisó. */
    @Column(name = "revisado_por", length = 40)
    private String revisadoPor;

    @Column(name = "motivo_rechazo", length = 200)
    private String motivoRechazo;
}
