package mx.unam.aragon.tsp.lastorres.condominios.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.UsuarioEntity;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IUsuarioRepository;

/**
 * Datos de quien inició sesión, para las pantallas del residente.
 */
@Component
public class SesionActual {

    private final IUsuarioRepository usuarioRepository;

    public SesionActual(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Id del residente dueño de la cuenta con sesión iniciada.
     * Siempre se toma de la sesión, nunca de lo que mande el navegador.
     */
    public int residenteId(Authentication autenticacion) {
        return usuarioRepository.findByUsuarioAndActivoTrue(autenticacion.getName())
                .map(UsuarioEntity::getResidente)
                .filter(ResidenteEntity::isActivo)
                .map(ResidenteEntity::getId)
                .orElseThrow(() -> new NoEncontradoException("Tu cuenta no está ligada a ningún residente"));
    }
}
