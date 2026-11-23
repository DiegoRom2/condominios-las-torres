package mx.unam.aragon.tsp.lastorres.condominios.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.AccesosBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.VisitantesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.core.services.IReporteService;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;

/**
 * Genera reportes en PDF (OpenPDF) y Excel .xlsx (fastexcel) reutilizando
 * las capas de negocio existentes en modo solo lectura.
 */
@Service
public class ReporteService implements IReporteService {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Paleta alineada con estilos.css (#1f4e79, #f3f5f8, #dbe1e8, #5d6b7a)
    private static final Color COLOR_PRIMARIO = new Color(31, 78, 121);
    private static final Color COLOR_FILA_PAR = new Color(243, 245, 248);
    private static final Color COLOR_BORDE = new Color(219, 225, 232);
    private static final Color COLOR_TEXTO_SUAVE = new Color(93, 107, 122);
    private static final String HEX_PRIMARIO = "1F4E79";
    private static final String HEX_FILA_PAR = "F3F5F8";
    private static final String HEX_BLANCO = "FFFFFF";

    private final AccesosBusinessLayer accesosBusinessLayer;
    private final VisitantesBusinessLayer visitantesBusinessLayer;

    public ReporteService(AccesosBusinessLayer accesosBusinessLayer,
            VisitantesBusinessLayer visitantesBusinessLayer) {
        this.accesosBusinessLayer = accesosBusinessLayer;
        this.visitantesBusinessLayer = visitantesBusinessLayer;
    }

    @Override
    public byte[] generarBitacoraPdf(boolean soloDentro) {
        List<AccesoDto> accesos = soloDentro
                ? accesosBusinessLayer.obtenerDentro()
                : accesosBusinessLayer.obtenerTodos();

        String titulo = soloDentro
                ? "Bitácora de Accesos — Visitantes Dentro"
                : "Bitácora de Accesos — Historial Completo";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document(PageSize.A4.rotate(), 32, 32, 32, 32)) {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            agregarEncabezadoPdf(doc, titulo, "Total de registros: " + accesos.size());

            if (accesos.isEmpty()) {
                agregarMensajeVacioPdf(doc, "No hay accesos registrados para mostrar.");
            } else {
                PdfPTable tabla = new PdfPTable(new float[] { 2.4f, 1.5f, 2.5f, 1.6f, 1.6f, 1.0f });
                tabla.setWidthPercentage(100);
                tabla.setHeaderRows(1);

                agregarEncabezadosTablaPdf(tabla, "Visitante", "Vivienda", "Motivo", "Entrada", "Salida", "Estado");

                for (int i = 0; i < accesos.size(); i++) {
                    AccesoDto a = accesos.get(i);
                    Color fondo = (i % 2 == 1) ? COLOR_FILA_PAR : Color.WHITE;
                    agregarCeldaPdf(tabla, textoSeguro(a.getVisitante()), fondo, Element.ALIGN_LEFT);
                    agregarCeldaPdf(tabla, textoSeguro(a.getVivienda()), fondo, Element.ALIGN_LEFT);
                    agregarCeldaPdf(tabla, textoSeguro(a.getMotivo()), fondo, Element.ALIGN_LEFT);
                    agregarCeldaPdf(tabla, formatearFecha(a.getFechaEntrada()), fondo, Element.ALIGN_CENTER);
                    agregarCeldaPdf(tabla, formatearFecha(a.getFechaSalida()), fondo, Element.ALIGN_CENTER);
                    agregarCeldaPdf(tabla, a.isDentro() ? "Dentro" : "Salió", fondo, Element.ALIGN_CENTER);
                }

                doc.add(tabla);
            }
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] generarBitacoraExcel(boolean soloDentro) {
        List<AccesoDto> accesos = soloDentro
                ? accesosBusinessLayer.obtenerDentro()
                : accesosBusinessLayer.obtenerTodos();

        String subtitulo = soloDentro
                ? "Visitantes dentro del condominio"
                : "Historial completo de accesos";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Workbook wb = new Workbook(baos, "Condominios Las Torres", "1.0")) {
            Worksheet ws = wb.newWorksheet("Bitácora");

            ws.width(0, 28);
            ws.width(1, 18);
            ws.width(2, 32);
            ws.width(3, 20);
            ws.width(4, 20);
            ws.width(5, 14);

            escribirEncabezadoExcel(ws, "Condominio Las Torres — " + subtitulo, 6);

            String[] columnas = { "Visitante", "Vivienda", "Motivo", "Entrada", "Salida", "Estado" };
            escribirCabeceraTablaExcel(ws, 3, columnas);

            int fila = 4;
            for (int i = 0; i < accesos.size(); i++) {
                AccesoDto a = accesos.get(i);
                ws.value(fila, 0, textoSeguro(a.getVisitante()));
                ws.value(fila, 1, textoSeguro(a.getVivienda()));
                ws.value(fila, 2, textoSeguro(a.getMotivo()));
                ws.value(fila, 3, formatearFecha(a.getFechaEntrada()));
                ws.value(fila, 4, formatearFecha(a.getFechaSalida()));
                ws.value(fila, 5, a.isDentro() ? "Dentro" : "Salió");

                if (i % 2 == 1) {
                    ws.range(fila, 0, fila, 5).style().fillColor(HEX_FILA_PAR).set();
                }
                fila++;
            }

            ws.value(fila + 1, 0, "Total de registros: " + accesos.size());
            ws.style(fila + 1, 0).bold().set();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el reporte Excel de la bitácora", e);
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] generarVisitantesPdf() {
        List<VisitanteDto> visitantes = visitantesBusinessLayer.obtenerTodos();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document(PageSize.A4, 32, 32, 32, 32)) {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            agregarEncabezadoPdf(doc, "Visitantes Autorizados", "Total de visitantes activos: " + visitantes.size());

            if (visitantes.isEmpty()) {
                agregarMensajeVacioPdf(doc, "No hay visitantes autorizados registrados.");
            } else {
                PdfPTable tabla = new PdfPTable(new float[] { 0.8f, 2.6f, 1.3f, 2.4f, 1.7f });
                tabla.setWidthPercentage(100);
                tabla.setHeaderRows(1);

                agregarEncabezadosTablaPdf(tabla, "ID", "Nombre completo", "Placas", "Autorizado por", "Vivienda");

                for (int i = 0; i < visitantes.size(); i++) {
                    VisitanteDto v = visitantes.get(i);
                    Color fondo = (i % 2 == 1) ? COLOR_FILA_PAR : Color.WHITE;
                    String nombreCompleto = (textoSeguro(v.getNombre()) + " " + textoSeguro(v.getApellidos())).trim();
                    String placas = (v.getPlacas() == null || v.getPlacas().isBlank()) ? "—" : v.getPlacas();

                    agregarCeldaPdf(tabla, String.valueOf(v.getId()), fondo, Element.ALIGN_CENTER);
                    agregarCeldaPdf(tabla, nombreCompleto, fondo, Element.ALIGN_LEFT);
                    agregarCeldaPdf(tabla, placas, fondo, Element.ALIGN_CENTER);
                    agregarCeldaPdf(tabla, textoSeguro(v.getResidente()), fondo, Element.ALIGN_LEFT);
                    agregarCeldaPdf(tabla, textoSeguro(v.getVivienda()), fondo, Element.ALIGN_LEFT);
                }

                doc.add(tabla);
            }
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] generarVisitantesExcel() {
        List<VisitanteDto> visitantes = visitantesBusinessLayer.obtenerTodos();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Workbook wb = new Workbook(baos, "Condominios Las Torres", "1.0")) {
            Worksheet ws = wb.newWorksheet("Visitantes");

            ws.width(0, 10);
            ws.width(1, 30);
            ws.width(2, 15);
            ws.width(3, 28);
            ws.width(4, 20);

            escribirEncabezadoExcel(ws, "Condominio Las Torres — Visitantes Autorizados", 5);

            String[] columnas = { "ID", "Nombre completo", "Placas", "Autorizado por", "Vivienda" };
            escribirCabeceraTablaExcel(ws, 3, columnas);

            int fila = 4;
            for (int i = 0; i < visitantes.size(); i++) {
                VisitanteDto v = visitantes.get(i);
                String nombreCompleto = (textoSeguro(v.getNombre()) + " " + textoSeguro(v.getApellidos())).trim();
                String placas = (v.getPlacas() == null || v.getPlacas().isBlank()) ? "—" : v.getPlacas();

                ws.value(fila, 0, v.getId());
                ws.value(fila, 1, nombreCompleto);
                ws.value(fila, 2, placas);
                ws.value(fila, 3, textoSeguro(v.getResidente()));
                ws.value(fila, 4, textoSeguro(v.getVivienda()));

                if (i % 2 == 1) {
                    ws.range(fila, 0, fila, 4).style().fillColor(HEX_FILA_PAR).set();
                }
                fila++;
            }

            ws.value(fila + 1, 0, "Total de visitantes: " + visitantes.size());
            ws.style(fila + 1, 0).bold().set();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el reporte Excel de visitantes", e);
        }
        return baos.toByteArray();
    }

    private void agregarEncabezadoPdf(Document doc, String titulo, String resumen) {
        Font fuenteMarca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXTO_SUAVE);
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_PRIMARIO);
        Font fuenteMeta = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXTO_SUAVE);

        Paragraph marca = new Paragraph("CONDOMINIO LAS TORRES — CONTROL DE ACCESO", fuenteMarca);
        marca.setSpacingAfter(2);
        doc.add(marca);

        Paragraph pTitulo = new Paragraph(titulo, fuenteTitulo);
        pTitulo.setSpacingAfter(4);
        doc.add(pTitulo);

        String generado = "Generado el " + LocalDateTime.now(ZONA_HORARIA).format(FORMATO_FECHA)
                + " (CDMX)  |  " + resumen;
        Paragraph meta = new Paragraph(generado, fuenteMeta);
        meta.setSpacingAfter(14);
        doc.add(meta);
    }

    private void agregarMensajeVacioPdf(Document doc, String mensaje) {
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, COLOR_TEXTO_SUAVE);
        Paragraph p = new Paragraph(mensaje, fuente);
        p.setSpacingBefore(12);
        doc.add(p);
    }

    private void agregarEncabezadosTablaPdf(PdfPTable tabla, String... encabezados) {
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Color.WHITE);
        for (String texto : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
            celda.setBackgroundColor(COLOR_PRIMARIO);
            celda.setBorderColor(COLOR_PRIMARIO);
            celda.setPaddingTop(7);
            celda.setPaddingBottom(7);
            celda.setPaddingLeft(6);
            celda.setPaddingRight(6);
            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.addCell(celda);
        }
    }

    private void agregarCeldaPdf(PdfPTable tabla, String texto, Color fondo, int alineacion) {
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(COLOR_BORDE);
        celda.setPaddingTop(6);
        celda.setPaddingBottom(6);
        celda.setPaddingLeft(6);
        celda.setPaddingRight(6);
        celda.setHorizontalAlignment(alineacion);
        tabla.addCell(celda);
    }

    private void escribirEncabezadoExcel(Worksheet ws, String titulo, int totalColumnas) {
        ws.value(0, 0, titulo);
        ws.range(0, 0, 0, totalColumnas - 1).merge();
        ws.style(0, 0).bold().fontSize(13).fontColor(HEX_PRIMARIO).set();

        String generado = "Generado el " + LocalDateTime.now(ZONA_HORARIA).format(FORMATO_FECHA) + " (CDMX)";
        ws.value(1, 0, generado);
        ws.range(1, 0, 1, totalColumnas - 1).merge();
        ws.style(1, 0).italic().fontSize(10).set();
    }

    private void escribirCabeceraTablaExcel(Worksheet ws, int fila, String[] columnas) {
        for (int c = 0; c < columnas.length; c++) {
            ws.value(fila, c, columnas[c]);
        }
        ws.range(fila, 0, fila, columnas.length - 1)
                .style()
                .bold()
                .fillColor(HEX_PRIMARIO)
                .fontColor(HEX_BLANCO)
                .set();
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "—";
    }

    private String textoSeguro(String texto) {
        return texto != null ? texto : "";
    }
}
