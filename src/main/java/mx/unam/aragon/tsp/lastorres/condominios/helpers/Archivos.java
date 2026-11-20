package mx.unam.aragon.tsp.lastorres.condominios.helpers;

/**
 * Revisiones de los archivos que suben los usuarios.
 *
 * El tipo NO se toma de la extensión ni de lo que dice el navegador (eso se puede falsificar):
 * se revisan los primeros bytes del archivo, que son la "firma" de cada formato.
 */
public final class Archivos {

    /** 2 MB. */
    public static final int TAMANO_MAXIMO = 2 * 1024 * 1024;

    private Archivos() {
    }

    /** Regresa application/pdf, image/png o image/jpeg; null si no es ninguno de esos. */
    public static String detectarTipo(byte[] datos) {
        if (empiezaCon(datos, 0x25, 0x50, 0x44, 0x46, 0x2D)) {             // %PDF-
            return "application/pdf";
        }
        if (empiezaCon(datos, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) { // PNG
            return "image/png";
        }
        if (empiezaCon(datos, 0xFF, 0xD8, 0xFF)) {                          // JPEG
            return "image/jpeg";
        }
        return null;
    }

    /**
     * Deja el nombre del archivo sin rutas ni caracteres raros, y con la extensión correcta según su tipo real.
     */
    public static String limpiarNombre(String original, String tipo) {
        String extension = switch (tipo) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            default -> ".jpg";
        };
        String nombre = original == null ? "" : original;
        nombre = nombre.substring(Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\')) + 1);
        int punto = nombre.lastIndexOf('.');
        if (punto > 0) {
            nombre = nombre.substring(0, punto);
        }
        nombre = nombre.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
        if (nombre.isEmpty()) {
            nombre = "comprobante";
        }
        if (nombre.length() > 80) {
            nombre = nombre.substring(0, 80);
        }
        return nombre + extension;
    }

    private static boolean empiezaCon(byte[] datos, int... firma) {
        if (datos == null || datos.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((datos[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
