package mx.unam.aragon.tsp.lastorres.condominios.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Archivo listo para descargarse o verse en el navegador.
 */
@Getter
@AllArgsConstructor
public class ArchivoDto {
    private String nombre;
    private String tipo;
    private byte[] datos;
}
