package mx.unam.aragon.tsp.lastorres.condominios.webController;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.SolicitudesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;

/**
 * Cosas comunes a todas las pantallas: quién inició sesión (para el menú)
 * y qué mostrar cuando se pide un registro que no existe.
 */
@ControllerAdvice(basePackages = "mx.unam.aragon.tsp.lastorres.condominios.webController")
public class WebAdvice {

    private final SolicitudesBusinessLayer solicitudesBusinessLayer;

    public WebAdvice(SolicitudesBusinessLayer solicitudesBusinessLayer) {
        this.solicitudesBusinessLayer = solicitudesBusinessLayer;
    }

    /** Nombre del usuario con sesión iniciada; vacío en la pantalla de login. */
    @ModelAttribute("usuarioActual")
    public String usuarioActual(Authentication autenticacion) {
        return autenticacion == null ? "" : autenticacion.getName();
    }

    /** true si quien inició sesión es administrador (muestra el menú completo). */
    @ModelAttribute("esAdmin")
    public boolean esAdmin(Authentication autenticacion) {
        return tieneRol(autenticacion, "ADMIN");
    }

    @ModelAttribute("esResidente")
    public boolean esResidente(Authentication autenticacion) {
        return tieneRol(autenticacion, "RESIDENTE");
    }

    /** Nombre del rol para mostrarlo en la barra. */
    @ModelAttribute("rolActual")
    public String rolActual(Authentication autenticacion) {
        if (tieneRol(autenticacion, "ADMIN")) {
            return "Administrador";
        }
        if (tieneRol(autenticacion, "RESIDENTE")) {
            return "Residente";
        }
        return tieneRol(autenticacion, "GUARDIA") ? "Guardia" : "";
    }

    /** Número de solicitudes por revisar, para el aviso del menú (solo administrador). */
    @ModelAttribute("solicitudesPendientes")
    public long solicitudesPendientes(Authentication autenticacion) {
        return tieneRol(autenticacion, "ADMIN") ? solicitudesBusinessLayer.contarPendientes() : 0;
    }

    public static boolean tieneRol(Authentication autenticacion, String rol) {
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .anyMatch(r -> ("ROLE_" + rol).equals(r.getAuthority()));
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
