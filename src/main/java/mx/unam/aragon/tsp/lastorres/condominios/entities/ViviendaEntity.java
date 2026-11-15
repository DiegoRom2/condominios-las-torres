package mx.unam.aragon.tsp.lastorres.condominios.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla viviendas: los departamentos del condominio Las Torres.
 */
@Entity
@Table(name = "viviendas")
@Getter
@Setter
@NoArgsConstructor
public class ViviendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 10)
    private String torre;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(nullable = false)
    private int piso;

    /** Borrado lógico: al borrar solo se desactiva, para no perder el historial. */
    @Column(nullable = false)
    private boolean activo = true;

    public ViviendaEntity(String torre, String numero, int piso) {
        this.torre = torre;
        this.numero = numero;
        this.piso = piso;
    }
}
