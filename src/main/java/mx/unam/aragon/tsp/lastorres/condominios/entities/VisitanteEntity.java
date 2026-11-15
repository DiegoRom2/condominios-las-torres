package mx.unam.aragon.tsp.lastorres.condominios.entities;

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
 * Tabla visitantes: personas externas autorizadas por un residente.
 */
@Entity
@Table(name = "visitantes")
@Getter
@Setter
@NoArgsConstructor
public class VisitanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellidos;

    /** Placas del auto; vacío si llega a pie. */
    @Column(nullable = false, length = 10)
    private String placas = "";

    /** Residente que autoriza la visita. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "residente_id", nullable = false)
    private ResidenteEntity residente;

    @Column(nullable = false)
    private boolean activo = true;

    public VisitanteEntity(String nombre, String apellidos, String placas, ResidenteEntity residente) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.placas = placas;
        this.residente = residente;
    }
}
