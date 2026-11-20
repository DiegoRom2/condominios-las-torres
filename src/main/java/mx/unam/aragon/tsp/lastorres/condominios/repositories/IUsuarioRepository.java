package mx.unam.aragon.tsp.lastorres.condominios.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.unam.aragon.tsp.lastorres.condominios.entities.UsuarioEntity;

public interface IUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    Optional<UsuarioEntity> findByUsuarioAndActivoTrue(String usuario);

    /** ¿Ya existe una cuenta con ese nombre de usuario (activa o no)? */
    boolean existsByUsuarioIgnoreCase(String usuario);

    /** ¿El residente ya tiene una cuenta activa? */
    boolean existsByResidente_IdAndActivoTrue(Integer residenteId);

    /** Cuentas activas ligadas a un residente (para desactivarlas si se borra el residente). */
    List<UsuarioEntity> findByResidente_IdAndActivoTrue(Integer residenteId);
}
