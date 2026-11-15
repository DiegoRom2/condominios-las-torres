package mx.unam.aragon.tsp.lastorres.condominios.webController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ViviendasBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;

/**
 * Pantallas de viviendas (solo administrador).
 */
@Controller
@RequestMapping("/viviendas")
public class ViviendaWebController {

    private final ViviendasBusinessLayer viviendasBusinessLayer;

    public ViviendaWebController(ViviendasBusinessLayer viviendasBusinessLayer) {
        this.viviendasBusinessLayer = viviendasBusinessLayer;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("viviendas", viviendasBusinessLayer.obtenerTodas());
        return "viviendas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("vivienda", new ViviendaDtoIn());
        return formulario(model, "Nueva vivienda", "/viviendas");
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("vivienda") ViviendaDtoIn dtoIn, BindingResult errores,
            Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return formulario(model, "Nueva vivienda", "/viviendas");
        }
        try {
            viviendasBusinessLayer.agregar(dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Nueva vivienda", "/viviendas");
        }
        redirect.addFlashAttribute("mensaje", "Vivienda registrada");
        return "redirect:/viviendas";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        ViviendaDto dto = viviendasBusinessLayer.obtenerPorId(id);
        model.addAttribute("vivienda", new ViviendaDtoIn(dto.getTorre(), dto.getNumero(), dto.getPiso()));
        return formulario(model, "Editar vivienda", "/viviendas/" + id);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable int id, @Valid @ModelAttribute("vivienda") ViviendaDtoIn dtoIn,
            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return formulario(model, "Editar vivienda", "/viviendas/" + id);
        }
        try {
            viviendasBusinessLayer.actualizar(id, dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Editar vivienda", "/viviendas/" + id);
        }
        redirect.addFlashAttribute("mensaje", "Vivienda actualizada");
        return "redirect:/viviendas";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            viviendasBusinessLayer.borrar(id);
            redirect.addFlashAttribute("mensaje", "Vivienda borrada");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/viviendas";
    }

    private String formulario(Model model, String titulo, String accion) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        return "viviendas/formulario";
    }
}
