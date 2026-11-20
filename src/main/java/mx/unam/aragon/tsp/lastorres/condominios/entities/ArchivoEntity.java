package mx.unam.aragon.tsp.lastorres.condominios.entities;

import java.time.LocalDateTime;

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
 * Tabla archivos: documentos que suben los usuarios (por ahora, los comprobantes de domicilio).
 * El contenido se guarda en la base de datos porque el disco de Render se borra en cada despliegue.
 */
@Entity
@Table(name = "archivos")
@Getter
@Setter
@NoArgsConstructor
public class ArchivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Tipo detectado por el sistema: application/pdf, image/png o image/jpeg. */
    @Column(nullable = false, length = 50)
    private String tipo;

    /** Tamaño en bytes. */
    @Column(nullable = false)
    private int tamano;

    @Column(nullable = false, length = 10_000_000)
    private byte[] datos;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    public ArchivoEntity(String nombre, String tipo, byte[] datos, LocalDateTime fechaSubida) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.datos = datos;
        this.tamano = datos.length;
        this.fechaSubida = fechaSubida;
    }
}
