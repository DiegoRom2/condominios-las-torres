package mx.unam.aragon.tsp.lastorres.condominios.core.services;

/**
 * Envío de notificaciones por correo.
 */
public interface INotificacionService {

    /**
     * Envía un correo en formato HTML. Nunca lanza excepción: si el envío falla,
     * solo se registra en el log, para que la operación principal no se caiga.
     *
     * @return true si el correo se envió
     */
    boolean enviarCorreo(String destino, String nombreDestino, String asunto, String html);
}
