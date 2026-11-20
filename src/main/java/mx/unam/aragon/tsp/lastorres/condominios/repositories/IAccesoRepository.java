package mx.unam.aragon.tsp.lastorres.condominios.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.unam.aragon.tsp.lastorres.condominios.entities.AccesoEntity;

public interface IAccesoRepository extends JpaRepository<AccesoEntity, Integer> {

    /** Bitácora completa, del más reciente al más antiguo. */
    List<AccesoEntity> findAllByOrderByFechaEntradaDesc();

    /** Accesos sin salida registrada: quién está dentro ahora. */
    List<AccesoEntity> findByFechaSalidaIsNullOrderByFechaEntradaDesc();

    /** Accesos de los visitantes de un residente, del más reciente al más antiguo. */
    List<AccesoEntity> findByVisitante_Residente_IdOrderByFechaEntradaDesc(Integer residenteId);

    /** ¿El visitante está dentro del condominio? */
    boolean existsByVisitante_IdAndFechaSalidaIsNull(Integer visitanteId);
}
