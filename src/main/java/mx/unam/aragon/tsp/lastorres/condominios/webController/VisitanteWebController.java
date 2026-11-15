package mx.unam.aragon.tsp.lastorres.condominios.webController;

import java.util.List;

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
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.VisitantesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ResidentesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDto;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;

/**
 * Pantallas de visitantes autorizados (solo administrador).
 */
@Controller
@RequestMapping("/visitantes")
public class VisitanteWebController {

    private final VisitantesBusinessLayer visitantesBusinessLayer;
    private final ResidentesBusinessLayer residentesBusinessLayer;

    public VisitanteWebController(VisitantesBusinessLayer visitantesBusinessLayer, ResidentesBusinessLayer residentesBusinessLayer) {
        this.visitantesBusinessLayer = visitantesBusinessLayer;
        this.residentesBusinessLayer = residentesBusinessLayer;
    }

    /** Lista de residentes para el selector "autorizado por". */
    @ModelAttribute("residentes")
    public List<ResidenteDto> residentes() {
        return residentesBusinessLayer.obtenerTodos();
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("visitantesLista", visitantesBusinessLayer.obtenerTodos());
        return "visitantes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("visitante", new VisitanteDtoIn());
        return formulario(model, "Nuevo visitante", "/visitantes");
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("visitante") VisitanteDtoIn dtoIn, BindingResult errores,
            Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return formulario(model, "Nuevo visitante", "/visitantes");
        }
        try {
            visitantesBusinessLayer.agregar(dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Nuevo visitante", "/visitantes");
        }
        redirect.addFlashAttribute("mensaje", "Visitante registrado");
        return "redirect:/visitantes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        VisitanteDto dto = visitantesBusinessLayer.obtenerPorId(id);
        model.addAttribute("visitante", new VisitanteDtoIn(dto.getNombre(), dto.getApellidos(), dto.getPlacas(), dto.getResidenteId()));
        return formulario(model, "Editar visitante", "/visitantes/" + id);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable int id, @Valid @ModelAttribute("visitante") VisitanteDtoIn dtoIn,
            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return formulario(model, "Editar visitante", "/visitantes/" + id);
        }
        try {
            visitantesBusinessLayer.actualizar(id, dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Editar visitante", "/visitantes/" + id);
        }
        redirect.addFlashAttribute("mensaje", "Visitante actualizado");
        return "redirect:/visitantes";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            visitantesBusinessLayer.borrar(id);
            redirect.addFlashAttribute("mensaje", "Visitante borrado");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/visitantes";
    }

    private String formulario(Model model, String titulo, String accion) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        return "visitantes/formulario";
    }
}
