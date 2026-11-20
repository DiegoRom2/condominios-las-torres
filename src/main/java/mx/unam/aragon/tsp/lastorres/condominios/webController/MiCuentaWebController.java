package mx.unam.aragon.tsp.lastorres.condominios.webController;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.AccesosBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.VisitantesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.seguridad.SesionActual;

/**
 * Pantallas del residente: sus visitantes autorizados y el historial de sus visitas.
 *
 * El residente que autoriza SIEMPRE es el de la sesión: el campo residenteId del formulario
 * se ignora, para que nadie pueda registrar visitantes a nombre de otro.
 */
@Controller
public class MiCuentaWebController {

    private final VisitantesBusinessLayer visitantesBusinessLayer;
    private final AccesosBusinessLayer accesosBusinessLayer;
    private final SesionActual sesionActual;

    public MiCuentaWebController(VisitantesBusinessLayer visitantesBusinessLayer,
            AccesosBusinessLayer accesosBusinessLayer, SesionActual sesionActual) {
        this.visitantesBusinessLayer = visitantesBusinessLayer;
        this.accesosBusinessLayer = accesosBusinessLayer;
        this.sesionActual = sesionActual;
    }

    /** El formulario nunca puede cambiar quién autoriza. */
    @InitBinder("visitante")
    public void protegerResidente(WebDataBinder binder) {
        binder.setDisallowedFields("residenteId");
    }

    /** Antes de leer el formulario, el visitante ya trae como autorizante al residente de la sesión. */
    @ModelAttribute("visitante")
    public VisitanteDtoIn visitanteDelResidente(Authentication autenticacion) {
        VisitanteDtoIn dtoIn = new VisitanteDtoIn();
        dtoIn.setResidenteId(sesionActual.residenteId(autenticacion));
        return dtoIn;
    }

    @GetMapping("/mis-visitantes")
    public String visitantes(Authentication autenticacion, Model model) {
        model.addAttribute("visitantesLista",
                visitantesBusinessLayer.obtenerDeResidente(sesionActual.residenteId(autenticacion)));
        return "residente/visitantes";
    }

    @GetMapping("/mis-visitantes/nuevo")
    public String nuevo(Model model) {
        return formulario(model, "Nuevo visitante", "/mis-visitantes");
    }

    @PostMapping("/mis-visitantes")
    public String guardar(@Valid @ModelAttribute("visitante") VisitanteDtoIn dtoIn, BindingResult errores,
            Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return formulario(model, "Nuevo visitante", "/mis-visitantes");
        }
        try {
            visitantesBusinessLayer.agregar(dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Nuevo visitante", "/mis-visitantes");
        }
        redirect.addFlashAttribute("mensaje", "Visitante autorizado");
        return "redirect:/mis-visitantes";
    }

    @GetMapping("/mis-visitantes/{id}/editar")
    public String editar(@PathVariable int id, Authentication autenticacion, Model model) {
        int residenteId = sesionActual.residenteId(autenticacion);
        VisitanteDto dto = visitantesBusinessLayer.obtenerPropio(id, residenteId);
        model.addAttribute("visitante", new VisitanteDtoIn(dto.getNombre(), dto.getApellidos(), dto.getPlacas(), residenteId));
        return formulario(model, "Editar visitante", "/mis-visitantes/" + id);
    }

    @PostMapping("/mis-visitantes/{id}")
    public String actualizar(@PathVariable int id, @Valid @ModelAttribute("visitante") VisitanteDtoIn dtoIn,
            BindingResult errores, Authentication autenticacion, Model model, RedirectAttributes redirect) {
        visitantesBusinessLayer.obtenerPropio(id, sesionActual.residenteId(autenticacion));
        if (errores.hasErrors()) {
            return formulario(model, "Editar visitante", "/mis-visitantes/" + id);
        }
        try {
            visitantesBusinessLayer.actualizar(id, dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Editar visitante", "/mis-visitantes/" + id);
        }
        redirect.addFlashAttribute("mensaje", "Visitante actualizado");
        return "redirect:/mis-visitantes";
    }

    @PostMapping("/mis-visitantes/{id}/borrar")
    public String borrar(@PathVariable int id, Authentication autenticacion, RedirectAttributes redirect) {
        visitantesBusinessLayer.obtenerPropio(id, sesionActual.residenteId(autenticacion));
        try {
            visitantesBusinessLayer.borrar(id);
            redirect.addFlashAttribute("mensaje", "Visitante borrado");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mis-visitantes";
    }

    @GetMapping("/mis-visitas")
    public String visitas(Authentication autenticacion, Model model) {
        model.addAttribute("accesos", accesosBusinessLayer.obtenerDeResidente(sesionActual.residenteId(autenticacion)));
        return "residente/visitas";
    }

    private String formulario(Model model, String titulo, String accion) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        return "residente/formulario";
    }
}
