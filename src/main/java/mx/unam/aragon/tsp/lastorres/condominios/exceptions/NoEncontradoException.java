package mx.unam.aragon.tsp.lastorres.condominios.exceptions;

/**
 * Se lanza cuando se busca un registro que no existe (responde 404).
 */
public class NoEncontradoException extends RuntimeException {
    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
