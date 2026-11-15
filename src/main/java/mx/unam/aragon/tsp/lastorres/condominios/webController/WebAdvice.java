package mx.unam.aragon.tsp.lastorres.condominios.webController;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;

/**
 * Cosas comunes a todas las pantallas: quién inició sesión (para el menú)
 * y qué mostrar cuando se pide un registro que no existe.
 */
@ControllerAdvice(basePackages = "mx.unam.aragon.tsp.lastorres.condominios.webController")
public class WebAdvice {

    /** Nombre del usuario con sesión iniciada; vacío en la pantalla de login. */
    @ModelAttribute("usuarioActual")
    public String usuarioActual(Authentication autenticacion) {
        return autenticacion == null ? "" : autenticacion.getName();
    }

    /** true si quien inició sesión es administrador (muestra el menú completo). */
    @ModelAttribute("esAdmin")
    public boolean esAdmin(Authentication autenticacion) {
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAuthority()));
    }

    @ExceptionHandler(NoEncontradoException.class)
    public ModelAndView noEncontrado(NoEncontradoException e) {
        ModelAndView vista = new ModelAndView("error");
        vista.setStatus(HttpStatus.NOT_FOUND);
        vista.addObject("status", 404);
        vista.addObject("mensaje", e.getMessage());
        return vista;
    }
}
