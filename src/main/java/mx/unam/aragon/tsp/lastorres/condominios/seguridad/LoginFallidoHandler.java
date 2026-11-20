package mx.unam.aragon.tsp.lastorres.condominios.seguridad;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.unam.aragon.tsp.lastorres.condominios.entities.SolicitudRegistroEntity;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.ISolicitudRegistroRepository;

/**
 * Qué mostrar cuando un inicio de sesión falla.
 *
 * Si la persona tiene una solicitud de registro y escribió bien la contraseña que eligió,
 * se le dice si su cuenta sigue en revisión o si fue rechazada. Se revisa la contraseña
 * para que nadie pueda averiguar qué usuarios existen probando nombres.
 */
@Component
public class LoginFallidoHandler implements AuthenticationFailureHandler {

    private final ISolicitudRegistroRepository solicitudRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginFallidoHandler(ISolicitudRegistroRepository solicitudRepository, PasswordEncoder passwordEncoder) {
        this.solicitudRepository = solicitudRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException excepcion) throws IOException {
        String usuario = request.getParameter("username");
        String contrasena = request.getParameter("password");
        String destino = "/login?error";

        if (usuario != null && contrasena != null && !usuario.isBlank()) {
            var solicitud = solicitudRepository.findFirstByUsuarioIgnoreCaseOrderByFechaSolicitudDesc(usuario.trim());
            if (solicitud.isPresent() && passwordEncoder.matches(contrasena, solicitud.get().getContrasena())) {
                String estado = solicitud.get().getEstado();
                if (SolicitudRegistroEntity.PENDIENTE.equals(estado)) {
                    destino = "/login?pendiente";
                } else if (SolicitudRegistroEntity.RECHAZADA.equals(estado)) {
                    destino = "/login?rechazada";
                }
            }
        }
        response.sendRedirect(request.getContextPath() + destino);
    }
}
