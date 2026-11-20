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
        // Los nombres de usuario se guardan en minúsculas: "Laura" y "laura" son la misma cuenta
        String nombre = usuario == null ? "" : usuario.trim().toLowerCase();

        UsuarioEntity entity = usuarioRepository.findByUsuarioAndActivoTrue(nombre)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña incorrectos"));

        // Una cuenta de residente deja de servir si la administración borró al residente
        if (entity.getResidente() != null && !entity.getResidente().isActivo()) {
            throw new UsernameNotFoundException("Usuario o contraseña incorrectos");
        }

        return User.withUsername(entity.getUsuario())
                .password(entity.getContrasena())
                .roles(entity.getRol())
                .build();
    }
}
