package mx.unam.aragon.tsp.lastorres.condominios.helpers;

import org.springframework.web.util.HtmlUtils;

/**
 * Arma el HTML de los correos que envía el sistema.
 */
public final class Correos {

    private Correos() {
    }

    /**
     * Escapa un texto escrito por un usuario antes de meterlo al HTML del correo,
     * para que nadie pueda inyectar etiquetas o enlaces falsos.
     */
    public static String seguro(String texto) {
        return texto == null ? "" : HtmlUtils.htmlEscape(texto);
    }

    /** Plantilla común: encabezado con el nombre del condominio y el contenido (ya escapado). */
    public static String plantilla(String titulo, String contenidoHtml) {
        return """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:auto;color:#1c2733">
                  <div style="background:#1f4e79;color:#fff;padding:16px 24px;border-radius:10px 10px 0 0">
                    <strong style="font-size:18px">Condominio Las Torres</strong>
                  </div>
                  <div style="border:1px solid #dbe1e8;border-top:none;padding:24px;border-radius:0 0 10px 10px">
                    <h2 style="margin-top:0;font-size:20px">%s</h2>
                    %s
                    <p style="color:#5d6b7a;font-size:12px;margin-top:32px">
                      Este es un mensaje automático del sistema de control de acceso. No respondas a este correo.
                    </p>
                  </div>
                </div>
                """.formatted(seguro(titulo), contenidoHtml);
    }
}
