package mx.unam.aragon.tsp.lastorres.condominios.webController;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.AccesosBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ResidentesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.VisitantesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ViviendasBusinessLayer;

/**
 * Pantalla de login y pantalla de inicio (resumen del condominio).
 */
@Controller
public class InicioWebController {

    private final ViviendasBusinessLayer viviendasBusinessLayer;
    private final ResidentesBusinessLayer residentesBusinessLayer;
    private final VisitantesBusinessLayer visitantesBusinessLayer;
    private final AccesosBusinessLayer accesosBusinessLayer;

    public InicioWebController(ViviendasBusinessLayer viviendasBusinessLayer,
            ResidentesBusinessLayer residentesBusinessLayer,
            VisitantesBusinessLayer visitantesBusinessLayer,
            AccesosBusinessLayer accesosBusinessLayer) {
        this.viviendasBusinessLayer = viviendasBusinessLayer;
        this.residentesBusinessLayer = residentesBusinessLayer;
        this.visitantesBusinessLayer = visitantesBusinessLayer;
        this.accesosBusinessLayer = accesosBusinessLayer;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String inicio(Authentication autenticacion, Model model) {
        // El residente no ve el resumen del condominio, solo lo suyo
        if (WebAdvice.tieneRol(autenticacion, "RESIDENTE")) {
            return "redirect:/mis-visitantes";
        }
        model.addAttribute("totalViviendas", viviendasBusinessLayer.obtenerTodas().size());
        model.addAttribute("totalResidentes", residentesBusinessLayer.obtenerTodos().size());
        model.addAttribute("totalVisitantes", visitantesBusinessLayer.obtenerTodos().size());
        model.addAttribute("dentro", accesosBusinessLayer.obtenerDentro());
        return "inicio";
    }
}
