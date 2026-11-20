package mx.unam.aragon.tsp.lastorres.condominios.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.unam.aragon.tsp.lastorres.condominios.entities.ArchivoEntity;

public interface IArchivoRepository extends JpaRepository<ArchivoEntity, Integer> {
}
