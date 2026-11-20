package mx.unam.aragon.tsp.lastorres.condominios.services;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import mx.unam.aragon.tsp.lastorres.condominios.core.services.INotificacionService;

/**
 * Envía correos con la API HTTP de Brevo (https://www.brevo.com).
 *
 * No se usa SMTP porque el plan gratuito de Render bloquea los puertos de correo (25, 465 y 587);
 * una llamada HTTPS sí está permitida.
 *
 * Si faltan las variables BREVO_API_KEY o MAIL_FROM, el correo no se envía:
 * solo se escribe en el log, para poder probar el sistema sin configurar nada.
 */
@Service
public class NotificacionService implements INotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String remitente;
    private final String nombreRemitente;
    private final RestClient restClient;

    public NotificacionService(
            @Value("${notificaciones.brevo.api-key:}") String apiKey,
            @Value("${notificaciones.remitente:}") String remitente,
            @Value("${notificaciones.remitente-nombre:Condominio Las Torres}") String nombreRemitente) {
        this.apiKey = apiKey;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;

        // Si Brevo no responde, mejor rendirse rápido que dejar al usuario esperando
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(fabrica).build();
    }

    public boolean estaConfigurado() {
        return !apiKey.isBlank() && !remitente.isBlank();
    }

    @Override
    public boolean enviarCorreo(String destino, String nombreDestino, String asunto, String html) {
        if (!estaConfigurado()) {
            log.warn("Correo NO enviado (faltan BREVO_API_KEY y MAIL_FROM). Para: {} | Asunto: {}", destino, asunto);
            return false;
        }
        try {
            Map<String, Object> cuerpo = Map.of(
                    "sender", Map.of("name", nombreRemitente, "email", remitente),
                    "to", List.of(Map.of("email", destino, "name", nombreDestino)),
                    "subject", asunto,
                    "htmlContent", html);

            restClient.post()
                    .uri(URL_BREVO)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Correo enviado a {}: {}", destino, asunto);
            return true;
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
            return false;
        }
    }
}
