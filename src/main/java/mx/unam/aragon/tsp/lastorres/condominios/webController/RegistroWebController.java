package mx.unam.aragon.tsp.lastorres.condominios.webController;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.SolicitudesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ViviendasBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.RegistroDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;

/**
 * Formulario público para pedir una cuenta de residente.
 */
@Controller
@RequestMapping("/registro")
public class RegistroWebController {

    private final SolicitudesBusinessLayer solicitudesBusinessLayer;
    private final ViviendasBusinessLayer viviendasBusinessLayer;

    public RegistroWebController(SolicitudesBusinessLayer solicitudesBusinessLayer,
            ViviendasBusinessLayer viviendasBusinessLayer) {
        this.solicitudesBusinessLayer = solicitudesBusinessLayer;
        this.viviendasBusinessLayer = viviendasBusinessLayer;
    }

    /** Viviendas para el selector del formulario. */
    @ModelAttribute("viviendas")
    public List<ViviendaDto> viviendas() {
        return viviendasBusinessLayer.obtenerTodas();
    }

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("registro", new RegistroDtoIn());
        return "registro";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("registro") RegistroDtoIn dtoIn, BindingResult errores,
            @RequestParam(name = "comprobante", required = false) MultipartFile comprobante, Model model) {
        if (errores.hasErrors()) {
            return "registro";
        }
        try {
            byte[] datos = comprobante == null ? null : comprobante.getBytes();
            String nombre = comprobante == null ? null : comprobante.getOriginalFilename();
            solicitudesBusinessLayer.registrar(dtoIn, nombre, datos);
        } catch (ReglaNegocioException | NoEncontradoException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        } catch (IOException e) {
            model.addAttribute("error", "No se pudo leer el archivo; intenta de nuevo");
            return "registro";
        }
        return "redirect:/login?registrado";
    }
}
