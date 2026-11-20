package mx.unam.aragon.tsp.lastorres.condominios.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.unam.aragon.tsp.lastorres.condominios.entities.SolicitudRegistroEntity;

public interface ISolicitudRegistroRepository extends JpaRepository<SolicitudRegistroEntity, Integer> {

    /** Solicitudes en un estado, de la más antigua a la más nueva (las primeras en llegar se atienden primero). */
    List<SolicitudRegistroEntity> findByEstadoOrderByFechaSolicitudAsc(String estado);

    /** Historial: solicitudes que ya no están en ese estado, de la revisada más recientemente a la más antigua. */
    List<SolicitudRegistroEntity> findByEstadoNotOrderByFechaRevisionDesc(String estado);

    long countByEstado(String estado);

    boolean existsByUsuarioIgnoreCaseAndEstado(String usuario, String estado);

    boolean existsByCorreoIgnoreCaseAndEstado(String correo, String estado);

    /** La solicitud más reciente de un nombre de usuario (para el mensaje del login). */
    Optional<SolicitudRegistroEntity> findFirstByUsuarioIgnoreCaseOrderByFechaSolicitudDesc(String usuario);
}
