package mx.unam.aragon.tsp.lastorres.condominios.core.services;

/**
 * Contrato para la generación de reportes descargables en PDF y Excel (.xlsx).
 */
public interface IReporteService {

    /** Genera la bitácora de accesos en formato PDF. */
    byte[] generarBitacoraPdf(boolean soloDentro);

    /** Genera la bitácora de accesos en formato Excel (.xlsx). */
    byte[] generarBitacoraExcel(boolean soloDentro);

    /** Genera el catálogo de visitantes autorizados en formato PDF. */
    byte[] generarVisitantesPdf();

    /** Genera el catálogo de visitantes autorizados en formato Excel (.xlsx). */
    byte[] generarVisitantesExcel();
}
