package mx.unam.aragon.tsp.lastorres.condominios.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.unam.aragon.tsp.lastorres.condominios.entities.ViviendaEntity;

public interface IViviendaRepository extends JpaRepository<ViviendaEntity, Integer> {

    List<ViviendaEntity> findByActivoTrueOrderByIdAsc();

    Optional<ViviendaEntity> findByIdAndActivoTrue(Integer id);

    /** ¿Ya hay otra vivienda activa con la misma torre y número? */
    boolean existsByTorreIgnoreCaseAndNumeroIgnoreCaseAndActivoTrueAndIdNot(String torre, String numero, Integer id);
}
