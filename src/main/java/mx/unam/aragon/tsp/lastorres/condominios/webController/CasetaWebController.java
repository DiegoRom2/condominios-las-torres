package mx.unam.aragon.tsp.lastorres.condominios.webController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.AccesosBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.VisitantesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;

/**
 * Pantallas de la caseta de vigilancia (guardia y administrador):
 * registrar entradas y salidas, y consultar la bitácora.
 */
@Controller
public class CasetaWebController {

    private final AccesosBusinessLayer accesosBusinessLayer;
    private final VisitantesBusinessLayer visitantesBusinessLayer;

    public CasetaWebController(AccesosBusinessLayer accesosBusinessLayer,
            VisitantesBusinessLayer visitantesBusinessLayer) {
        this.accesosBusinessLayer = accesosBusinessLayer;
        this.visitantesBusinessLayer = visitantesBusinessLayer;
    }

    @GetMapping("/caseta")
    public String caseta(Model model) {
        model.addAttribute("entrada", new AccesoDtoIn());
        return vistaCaseta(model);
    }

    @PostMapping("/caseta/entrada")
    public String registrarEntrada(@Valid @ModelAttribute("entrada") AccesoDtoIn dtoIn, BindingResult errores,
            Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return vistaCaseta(model);
        }
        try {
            accesosBusinessLayer.registrarEntrada(dtoIn);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return vistaCaseta(model);
        }
        redirect.addFlashAttribute("mensaje", "Entrada registrada");
        return "redirect:/caseta";
    }

    @PostMapping("/caseta/{id}/salida")
    public String registrarSalida(@PathVariable int id, RedirectAttributes redirect) {
        try {
            accesosBusinessLayer.registrarSalida(id);
            redirect.addFlashAttribute("mensaje", "Salida registrada");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/caseta";
    }

    @GetMapping("/bitacora")
    public String bitacora(Model model) {
        model.addAttribute("accesos", accesosBusinessLayer.obtenerTodos());
        return "caseta/bitacora";
    }

    private String vistaCaseta(Model model) {
        model.addAttribute("visitantes", visitantesBusinessLayer.obtenerTodos());
        model.addAttribute("dentro", accesosBusinessLayer.obtenerDentro());
        return "caseta/caseta";
    }
}
