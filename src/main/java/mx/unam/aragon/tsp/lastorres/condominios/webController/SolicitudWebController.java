package mx.unam.aragon.tsp.lastorres.condominios.webController;

import java.nio.charset.StandardCharsets;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.SolicitudesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ArchivoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.RechazoDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;

/**
 * Revisión de solicitudes de cuenta de residente (solo administrador).
 */
@Controller
@RequestMapping("/solicitudes")
public class SolicitudWebController {

    private final SolicitudesBusinessLayer solicitudesBusinessLayer;

    public SolicitudWebController(SolicitudesBusinessLayer solicitudesBusinessLayer) {
        this.solicitudesBusinessLayer = solicitudesBusinessLayer;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("pendientes", solicitudesBusinessLayer.obtenerPendientes());
        model.addAttribute("revisadas", solicitudesBusinessLayer.obtenerRevisadas());
        return "solicitudes/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable int id, Model model) {
        model.addAttribute("rechazo", new RechazoDtoIn());
        return vistaDetalle(id, model);
    }

    /** Muestra el comprobante en el navegador (PDF o imagen). */
    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> comprobante(@PathVariable int id) {
        ArchivoDto archivo = solicitudesBusinessLayer.obtenerComprobante(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.getTipo()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(archivo.getNombre(), StandardCharsets.UTF_8).build().toString())
                .cacheControl(CacheControl.noStore())
                .body(archivo.getDatos());
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(@PathVariable int id, Authentication autenticacion, RedirectAttributes redirect) {
        try {
            redirect.addFlashAttribute("mensaje", solicitudesBusinessLayer.aprobar(id, autenticacion.getName()).getMensaje());
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/solicitudes/" + id;
        }
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable int id, @Valid @ModelAttribute("rechazo") RechazoDtoIn dtoIn,
            BindingResult errores, Authentication autenticacion, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return vistaDetalle(id, model);
        }
        try {
            redirect.addFlashAttribute("mensaje",
                    solicitudesBusinessLayer.rechazar(id, dtoIn.getMotivo(), autenticacion.getName()).getMensaje());
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/solicitudes/" + id;
        }
        return "redirect:/solicitudes";
    }

    private String vistaDetalle(int id, Model model) {
        model.addAttribute("solicitud", solicitudesBusinessLayer.obtenerPorId(id));
        return "solicitudes/detalle";
    }
}
