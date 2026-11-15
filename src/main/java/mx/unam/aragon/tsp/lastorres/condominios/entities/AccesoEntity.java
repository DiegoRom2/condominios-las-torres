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
 * Tabla accesos: bitácora de entradas y salidas en la caseta.
 */
@Entity
@Table(name = "accesos")
@Getter
@Setter
@NoArgsConstructor
public class AccesoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visitante_id", nullable = false)
    private VisitanteEntity visitante;

    /*
     * Se guarda una "foto" del nombre y la vivienda al momento de entrar,
     * para que la bitácora no cambie si después se editan esos datos.
     */
    @Column(name = "nombre_visitante", nullable = false, length = 150)
    private String nombreVisitante;

    @Column(nullable = false, length = 30)
    private String vivienda;

    @Column(nullable = false, length = 120)
    private String motivo;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDateTime fechaEntrada;

    /** Vacía mientras el visitante sigue dentro. */
    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    public AccesoEntity(VisitanteEntity visitante, String nombreVisitante, String vivienda,
            String motivo, LocalDateTime fechaEntrada) {
        this.visitante = visitante;
        this.nombreVisitante = nombreVisitante;
        this.vivienda = vivienda;
        this.motivo = motivo;
        this.fechaEntrada = fechaEntrada;
    }
}
