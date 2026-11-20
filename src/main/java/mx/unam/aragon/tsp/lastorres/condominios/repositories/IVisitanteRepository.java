package mx.unam.aragon.tsp.lastorres.condominios.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.unam.aragon.tsp.lastorres.condominios.entities.VisitanteEntity;

public interface IVisitanteRepository extends JpaRepository<VisitanteEntity, Integer> {

    List<VisitanteEntity> findByActivoTrueOrderByIdAsc();

    Optional<VisitanteEntity> findByIdAndActivoTrue(Integer id);

    /** Visitantes autorizados por un residente. */
    List<VisitanteEntity> findByResidente_IdAndActivoTrueOrderByIdAsc(Integer residenteId);

    /** ¿El residente tiene visitantes activos autorizados? */
    boolean existsByResidente_IdAndActivoTrue(Integer residenteId);
}
