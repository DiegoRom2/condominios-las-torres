package mx.unam.aragon.tsp.lastorres.condominios.controllers;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.unam.aragon.tsp.lastorres.condominios.core.services.IReporteService;

/**
 * Exportación de reportes en PDF y Excel (.xlsx) para administración y caseta.
 * Protegido automáticamente por SecurityConfig (GET /api/** -> ADMIN y GUARDIA).
 */
@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Exportación de reportes en PDF y Excel (.xlsx)")
public class ReporteController {

    private static final MediaType MEDIA_TYPE_XLSX = MediaType
            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final IReporteService reporteService;

    public ReporteController(IReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/bitacora/pdf")
    @Operation(summary = "Exportar bitácora de accesos en PDF")
    public ResponseEntity<byte[]> bitacoraPdf(@RequestParam(defaultValue = "false") boolean soloDentro) {
        byte[] archivo = reporteService.generarBitacoraPdf(soloDentro);
        String nombre = soloDentro ? "bitacora-dentro.pdf" : "bitacora-accesos.pdf";
        return construirDescarga(archivo, MediaType.APPLICATION_PDF, nombre);
    }

    @GetMapping("/bitacora/excel")
    @Operation(summary = "Exportar bitácora de accesos en Excel (.xlsx)")
    public ResponseEntity<byte[]> bitacoraExcel(@RequestParam(defaultValue = "false") boolean soloDentro) {
        byte[] archivo = reporteService.generarBitacoraExcel(soloDentro);
        String nombre = soloDentro ? "bitacora-dentro.xlsx" : "bitacora-accesos.xlsx";
        return construirDescarga(archivo, MEDIA_TYPE_XLSX, nombre);
    }

    @GetMapping("/visitantes/pdf")
    @Operation(summary = "Exportar catálogo de visitantes autorizados en PDF")
    public ResponseEntity<byte[]> visitantesPdf() {
        byte[] archivo = reporteService.generarVisitantesPdf();
        return construirDescarga(archivo, MediaType.APPLICATION_PDF, "visitantes-autorizados.pdf");
    }

    @GetMapping("/visitantes/excel")
    @Operation(summary = "Exportar catálogo de visitantes autorizados en Excel (.xlsx)")
    public ResponseEntity<byte[]> visitantesExcel() {
        byte[] archivo = reporteService.generarVisitantesExcel();
        return construirDescarga(archivo, MEDIA_TYPE_XLSX, "visitantes-autorizados.xlsx");
    }

    private ResponseEntity<byte[]> construirDescarga(byte[] contenido, MediaType tipo, String nombreArchivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(tipo);
        headers.setContentDisposition(ContentDisposition.attachment().filename(nombreArchivo).build());
        headers.setCacheControl(CacheControl.noStore());
        return ResponseEntity.ok().headers(headers).body(contenido);
    }
}
