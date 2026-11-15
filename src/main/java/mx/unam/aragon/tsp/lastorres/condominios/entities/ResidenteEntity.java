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
 * Tabla residentes: personas que viven en una vivienda.
 */
@Entity
@Table(name = "residentes")
@Getter
@Setter
@NoArgsConstructor
public class ResidenteEntity {

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

    /** Muchos residentes pueden vivir en la misma vivienda. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "vivienda_id", nullable = false)
    private ViviendaEntity vivienda;

    @Column(nullable = false)
    private boolean activo = true;

    public ResidenteEntity(String nombre, String apellidos, String correo, String telefono, ViviendaEntity vivienda) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.vivienda = vivienda;
    }
}
