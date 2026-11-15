package mx.unam.aragon.tsp.lastorres.condominios.seguridad;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import mx.unam.aragon.tsp.lastorres.condominios.entities.UsuarioEntity;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IUsuarioRepository;

/**
 * Le dice a Spring Security cómo buscar a un usuario en nuestra tabla usuarios.
 */
@Service
public class UsuarioDetallesService implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    public UsuarioDetallesService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        UsuarioEntity entity = usuarioRepository.findByUsuarioAndActivoTrue(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña incorrectos"));

        return User.withUsername(entity.getUsuario())
                .password(entity.getContrasena())
                .roles(entity.getRol())
                .build();
    }
}
