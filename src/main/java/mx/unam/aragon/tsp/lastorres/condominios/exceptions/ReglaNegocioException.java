package mx.unam.aragon.tsp.lastorres.condominios.exceptions;

/**
 * Se lanza cuando la petición rompe una regla del condominio,
 * por ejemplo registrar dos veces la entrada de alguien que ya está dentro (responde 409).
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
