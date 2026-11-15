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
 * Tabla usuarios: personas que pueden iniciar sesión en el sistema.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre con el que inicia sesión, por ejemplo "admin". */
    @Column(nullable = false, unique = true, length = 40)
    private String usuario;

    /** Contraseña cifrada con BCrypt; nunca se guarda en texto plano. */
    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(nullable = false, length = 80)
    private String nombre;

    /** ADMIN o GUARDIA. */
    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    public UsuarioEntity(String usuario, String contrasena, String nombre, String rol) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.rol = rol;
    }
}
