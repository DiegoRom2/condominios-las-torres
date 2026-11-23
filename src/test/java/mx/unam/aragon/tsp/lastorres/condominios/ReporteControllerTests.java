package mx.unam.aragon.tsp.lastorres.condominios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import mx.unam.aragon.tsp.lastorres.condominios.entities.UsuarioEntity;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IUsuarioRepository;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:reportes_test;DB_CLOSE_DELAY=-1",
                "ADMIN_PASSWORD=admin-test",
                "GUARDIA_PASSWORD=guardia-test"
        })
class ReporteControllerTests {

    private static final String CONTENT_TYPE_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @LocalServerPort
    private int port;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RestClient restClient;

    @BeforeEach
    void preparar() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(status -> true, (request, response) -> {
                    // Permite inspeccionar 401/403 directamente en ResponseEntity
                })
                .build();

        if (!usuarioRepository.existsByUsuarioIgnoreCase("residente_test")) {
            usuarioRepository.save(new UsuarioEntity(
                    "residente_test",
                    passwordEncoder.encode("residente-test"),
                    "Residente de prueba",
                    "RESIDENTE"));
        }
    }

    @Test
    void bitacoraPdfConAdminDevuelvePdfValido() {
        ResponseEntity<byte[]> respuesta = peticionGet("/api/v1/reportes/bitacora/pdf", "admin", "admin-test");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getHeaders().getContentType());
        assertEquals("application/pdf", respuesta.getHeaders().getContentType().toString());
        assertTrue(encabezadoDisposicion(respuesta).contains("bitacora-accesos.pdf"));
        assertEsPdf(respuesta.getBody());
    }

    @Test
    void bitacoraPdfSoloDentroConGuardiaDevuelvePdfValido() {
        ResponseEntity<byte[]> respuesta = peticionGet(
                "/api/v1/reportes/bitacora/pdf?soloDentro=true", "guardia", "guardia-test");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertTrue(encabezadoDisposicion(respuesta).contains("bitacora-dentro.pdf"));
        assertEsPdf(respuesta.getBody());
    }

    @Test
    void bitacoraExcelConGuardiaDevuelveXlsxValido() {
        ResponseEntity<byte[]> respuesta = peticionGet("/api/v1/reportes/bitacora/excel", "guardia", "guardia-test");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getHeaders().getContentType());
        assertEquals(CONTENT_TYPE_XLSX, respuesta.getHeaders().getContentType().toString());
        assertTrue(encabezadoDisposicion(respuesta).contains("bitacora-accesos.xlsx"));
        assertEsXlsx(respuesta.getBody());
    }

    @Test
    void visitantesPdfYExcelConAdminDevuelvenArchivosValidos() {
        ResponseEntity<byte[]> pdf = peticionGet("/api/v1/reportes/visitantes/pdf", "admin", "admin-test");
        assertEquals(HttpStatus.OK, pdf.getStatusCode());
        assertTrue(encabezadoDisposicion(pdf).contains("visitantes-autorizados.pdf"));
        assertEsPdf(pdf.getBody());

        ResponseEntity<byte[]> excel = peticionGet("/api/v1/reportes/visitantes/excel", "admin", "admin-test");
        assertEquals(HttpStatus.OK, excel.getStatusCode());
        assertNotNull(excel.getHeaders().getContentType());
        assertEquals(CONTENT_TYPE_XLSX, excel.getHeaders().getContentType().toString());
        assertTrue(encabezadoDisposicion(excel).contains("visitantes-autorizados.xlsx"));
        assertEsXlsx(excel.getBody());
    }

    @Test
    void reportesSinAutenticacionResponde401() {
        ResponseEntity<byte[]> respuesta = restClient.get()
                .uri("/api/v1/reportes/bitacora/pdf")
                .retrieve()
                .toEntity(byte[].class);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void reportesConRolResidenteResponde403() {
        ResponseEntity<byte[]> respuesta = peticionGet(
                "/api/v1/reportes/bitacora/pdf", "residente_test", "residente-test");

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    private ResponseEntity<byte[]> peticionGet(String ruta, String usuario, String contrasena) {
        String token = Base64.getEncoder()
                .encodeToString((usuario + ":" + contrasena).getBytes(StandardCharsets.UTF_8));
        return restClient.get()
                .uri(ruta)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + token)
                .retrieve()
                .toEntity(byte[].class);
    }

    private String encabezadoDisposicion(ResponseEntity<byte[]> respuesta) {
        String valor = respuesta.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(valor);
        return valor;
    }

    private void assertEsPdf(byte[] cuerpo) {
        assertNotNull(cuerpo);
        assertTrue(cuerpo.length > 100, "El PDF debe tener contenido");
        String firma = new String(cuerpo, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", firma);
    }

    private void assertEsXlsx(byte[] cuerpo) {
        assertNotNull(cuerpo);
        assertTrue(cuerpo.length > 100, "El archivo Excel debe tener contenido");
        assertEquals('P', (char) cuerpo[0]);
        assertEquals('K', (char) cuerpo[1]);
    }
}
