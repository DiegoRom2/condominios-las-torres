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
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ResidentesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ViviendasBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;

/**
 * Pantallas de residentes (solo administrador).
 */
@Controller
@RequestMapping("/residentes")
public class ResidenteWebController {

    private final ResidentesBusinessLayer residentesBusinessLayer;
    private final ViviendasBusinessLayer viviendasBusinessLayer;

    public ResidenteWebController(ResidentesBusinessLayer residentesBusinessLayer, ViviendasBusinessLayer viviendasBusinessLayer) {
        this.residentesBusinessLayer = residentesBusinessLayer;
        this.viviendasBusinessLayer = viviendasBusinessLayer;
    }

    /** Lista de viviendas para el selector del formulario. */
    @ModelAttribute("viviendas")
    public List<ViviendaDto> viviendas() {
        return viviendasBusinessLayer.obtenerTodas();
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("residentesLista", residentesBusinessLayer.obtenerTodos());
        return "residentes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("residente", new ResidenteDtoIn());
        return formulario(model, "Nuevo residente", "/residentes");
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("residente") ResidenteDtoIn dtoIn, BindingResult errores,
            Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return formulario(model, "Nuevo residente", "/residentes");
        }
        try {
            residentesBusinessLayer.agregar(dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Nuevo residente", "/residentes");
        }
        redirect.addFlashAttribute("mensaje", "Residente registrado");
        return "redirect:/residentes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        ResidenteDto dto = residentesBusinessLayer.obtenerPorId(id);
        model.addAttribute("residente", new ResidenteDtoIn(dto.getNombre(), dto.getApellidos(), dto.getCorreo(), dto.getTelefono(), dto.getViviendaId()));
        return formulario(model, "Editar residente", "/residentes/" + id);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable int id, @Valid @ModelAttribute("residente") ResidenteDtoIn dtoIn,
            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return formulario(model, "Editar residente", "/residentes/" + id);
        }
        try {
            residentesBusinessLayer.actualizar(id, dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "Editar residente", "/residentes/" + id);
        }
        redirect.addFlashAttribute("mensaje", "Residente actualizado");
        return "redirect:/residentes";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            residentesBusinessLayer.borrar(id);
            redirect.addFlashAttribute("mensaje", "Residente borrado");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/residentes";
    }

    private String formulario(Model model, String titulo, String accion) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        return "residentes/formulario";
    }
}
