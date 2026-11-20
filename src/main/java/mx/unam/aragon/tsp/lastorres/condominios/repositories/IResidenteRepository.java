package mx.unam.aragon.tsp.lastorres.condominios.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;

public interface IResidenteRepository extends JpaRepository<ResidenteEntity, Integer> {

    List<ResidenteEntity> findByActivoTrueOrderByIdAsc();

    Optional<ResidenteEntity> findByIdAndActivoTrue(Integer id);

    /** ¿Ya hay otro residente activo con ese correo? */
    boolean existsByCorreoIgnoreCaseAndActivoTrueAndIdNot(String correo, Integer id);

    /** Residente activo con ese correo, si existe. */
    Optional<ResidenteEntity> findFirstByCorreoIgnoreCaseAndActivoTrue(String correo);

    /** ¿La vivienda tiene residentes activos? */
    boolean existsByVivienda_IdAndActivoTrue(Integer viviendaId);
}
