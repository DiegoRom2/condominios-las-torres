package mx.unam.aragon.tsp.lastorres.condominios.businessLayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.unam.aragon.tsp.lastorres.condominios.core.services.INotificacionService;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ArchivoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.RegistroDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.SolicitudDto;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ArchivoEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ResidenteEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.SolicitudRegistroEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.UsuarioEntity;
import mx.unam.aragon.tsp.lastorres.condominios.entities.ViviendaEntity;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.NoEncontradoException;
import mx.unam.aragon.tsp.lastorres.condominios.exceptions.ReglaNegocioException;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.Archivos;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.Correos;
import mx.unam.aragon.tsp.lastorres.condominios.helpers.Mappers;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IArchivoRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IResidenteRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.ISolicitudRegistroRepository;
import mx.unam.aragon.tsp.lastorres.condominios.repositories.IUsuarioRepository;

/**
 * Registro de residentes con aprobación:
 *  1. La persona llena el formulario público y sube su comprobante de domicilio (queda PENDIENTE).
 *  2. La administración revisa el comprobante y aprueba o rechaza.
 *  3. Al aprobar se crea su cuenta con rol RESIDENTE; en ambos casos se le avisa por correo.
 */
@Service
public class SolicitudesBusinessLayer {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");

    private final ISolicitudRegistroRepository solicitudRepository;
    private final IArchivoRepository archivoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IResidenteRepository residenteRepository;
    private final ViviendasBusinessLayer viviendasBusinessLayer;
    private final PasswordEncoder passwordEncoder;
    private final INotificacionService notificacionService;
    private final String urlSistema;

    public SolicitudesBusinessLayer(ISolicitudRegistroRepository solicitudRepository,
            IArchivoRepository archivoRepository,
            IUsuarioRepository usuarioRepository,
            IResidenteRepository residenteRepository,
            ViviendasBusinessLayer viviendasBusinessLayer,
            PasswordEncoder passwordEncoder,
            INotificacionService notificacionService,
            @Value("${app.url:http://localhost:8081}") String urlSistema) {
        this.solicitudRepository = solicitudRepository;
        this.archivoRepository = archivoRepository;
        this.usuarioRepository = usuarioRepository;
        this.residenteRepository = residenteRepository;
        this.viviendasBusinessLayer = viviendasBusinessLayer;
        this.passwordEncoder = passwordEncoder;
        this.notificacionService = notificacionService;
        this.urlSistema = urlSistema.endsWith("/") ? urlSistema.substring(0, urlSistema.length() - 1) : urlSistema;
    }

    // ------------------------------------------------------------------
    // 1. Registro (formulario público)
    // ------------------------------------------------------------------

    @Transactional
    public IdDto registrar(RegistroDtoIn dtoIn, String nombreArchivo, byte[] datosArchivo) {
        if (!dtoIn.getContrasena().equals(dtoIn.getConfirmarContrasena())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }
        ViviendaEntity vivienda = viviendasBusinessLayer.obtenerEntidad(dtoIn.getViviendaId());
        String usuario = dtoIn.getUsuario().trim().toLowerCase();
        String correo = dtoIn.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByUsuarioIgnoreCase(usuario)
                || solicitudRepository.existsByUsuarioIgnoreCaseAndEstado(usuario, SolicitudRegistroEntity.PENDIENTE)) {
            throw new ReglaNegocioException("El usuario '" + usuario + "' ya está ocupado; elige otro");
        }
        if (solicitudRepository.existsByCorreoIgnoreCaseAndEstado(correo, SolicitudRegistroEntity.PENDIENTE)) {
            throw new ReglaNegocioException("Ya hay una solicitud en revisión con ese correo");
        }
        residenteRepository.findFirstByCorreoIgnoreCaseAndActivoTrue(correo).ifPresent(residente -> {
            if (usuarioRepository.existsByResidente_IdAndActivoTrue(residente.getId())) {
                throw new ReglaNegocioException("Ya existe una cuenta para ese correo; inicia sesión con ella");
            }
        });

        String tipo = validarComprobante(datosArchivo);
        ArchivoEntity archivo = archivoRepository.save(new ArchivoEntity(
                Archivos.limpiarNombre(nombreArchivo, tipo), tipo, datosArchivo, ahora()));

        SolicitudRegistroEntity solicitud = new SolicitudRegistroEntity();
        solicitud.setNombre(dtoIn.getNombre().trim());
        solicitud.setApellidos(dtoIn.getApellidos().trim());
        solicitud.setCorreo(correo);
        solicitud.setTelefono(dtoIn.getTelefono());
        solicitud.setVivienda(vivienda);
        solicitud.setUsuario(usuario);
        solicitud.setContrasena(passwordEncoder.encode(dtoIn.getContrasena()));
        solicitud.setComprobanteId(archivo.getId());
        solicitud.setComprobanteNombre(archivo.getNombre());
        solicitud.setFechaSolicitud(ahora());
        solicitud = solicitudRepository.save(solicitud);

        // No se manda correo aquí a propósito: el formulario es público, y si mandara correos
        // cualquiera podría usarlo para enviar mensajes a direcciones ajenas.
        return new IdDto(solicitud.getId(), "Solicitud enviada");
    }

    /** Revisa que el comprobante exista, pese 2 MB o menos y sea realmente PDF, JPG o PNG. */
    private String validarComprobante(byte[] datos) {
        if (datos == null || datos.length == 0) {
            throw new ReglaNegocioException("Adjunta tu comprobante de domicilio (PDF, JPG o PNG)");
        }
        if (datos.length > Archivos.TAMANO_MAXIMO) {
            throw new ReglaNegocioException("El comprobante no puede pesar más de 2 MB");
        }
        String tipo = Archivos.detectarTipo(datos);
        if (tipo == null) {
            throw new ReglaNegocioException("El comprobante debe ser un archivo PDF, JPG o PNG");
        }
        return tipo;
    }

    // ------------------------------------------------------------------
    // 2. Revisión (administración)
    // ------------------------------------------------------------------

    public List<SolicitudDto> obtenerPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudAsc(SolicitudRegistroEntity.PENDIENTE).stream()
                .map(this::toDto)
                .toList();
    }

    public List<SolicitudDto> obtenerRevisadas() {
        return solicitudRepository.findByEstadoNotOrderByFechaRevisionDesc(SolicitudRegistroEntity.PENDIENTE).stream()
                .map(this::toDto)
                .toList();
    }

    public long contarPendientes() {
        return solicitudRepository.countByEstado(SolicitudRegistroEntity.PENDIENTE);
    }

    public SolicitudDto obtenerPorId(int id) {
        SolicitudRegistroEntity solicitud = obtenerEntidad(id);
        SolicitudDto dto = toDto(solicitud);
        if (solicitud.getEstado().equals(SolicitudRegistroEntity.PENDIENTE)) {
            residenteRepository.findFirstByCorreoIgnoreCaseAndActivoTrue(solicitud.getCorreo())
                    .ifPresent(r -> dto.setResidenteExistente(
                            r.getNombre() + " " + r.getApellidos() + " — " + Mappers.etiqueta(r.getVivienda())));
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public ArchivoDto obtenerComprobante(int id) {
        SolicitudRegistroEntity solicitud = obtenerEntidad(id);
        ArchivoEntity archivo = archivoRepository.findById(solicitud.getComprobanteId())
                .orElseThrow(() -> new NoEncontradoException("No se encontró el comprobante de la solicitud " + id));
        return new ArchivoDto(archivo.getNombre(), archivo.getTipo(), archivo.getDatos());
    }

    @Transactional
    public IdDto aprobar(int id, String revisadoPor) {
        SolicitudRegistroEntity solicitud = obtenerPendiente(id);

        if (usuarioRepository.existsByUsuarioIgnoreCase(solicitud.getUsuario())) {
            throw new ReglaNegocioException("El usuario '" + solicitud.getUsuario()
                    + "' ya lo tomó otra cuenta; rechaza esta solicitud para que la persona elija otro");
        }

        // Si la administración ya había registrado a esta persona (mismo correo), se usa ese registro;
        // si no, se crea el residente con los datos de la solicitud.
        ResidenteEntity residente = residenteRepository.findFirstByCorreoIgnoreCaseAndActivoTrue(solicitud.getCorreo())
                .orElse(null);
        if (residente != null) {
            if (usuarioRepository.existsByResidente_IdAndActivoTrue(residente.getId())) {
                throw new ReglaNegocioException("Ese residente ya tiene una cuenta activa");
            }
        } else {
            if (!solicitud.getVivienda().isActivo()) {
                throw new ReglaNegocioException("La vivienda de esta solicitud ya no existe; recházala");
            }
            residente = residenteRepository.save(new ResidenteEntity(
                    solicitud.getNombre(), solicitud.getApellidos(), solicitud.getCorreo(),
                    solicitud.getTelefono(), solicitud.getVivienda()));
        }

        UsuarioEntity usuario = new UsuarioEntity(solicitud.getUsuario(), solicitud.getContrasena(),
                recortar(solicitud.getNombre() + " " + solicitud.getApellidos(), 80), "RESIDENTE");
        usuario.setResidente(residente);
        usuarioRepository.save(usuario);

        solicitud.setEstado(SolicitudRegistroEntity.APROBADA);
        solicitud.setFechaRevision(ahora());
        solicitud.setRevisadoPor(revisadoPor);
        solicitudRepository.save(solicitud);

        boolean enviado = notificacionService.enviarCorreo(solicitud.getCorreo(), solicitud.getNombre(),
                "Tu cuenta en Las Torres fue aprobada",
                Correos.plantilla("¡Tu cuenta fue aprobada!", """
                        <p>Hola %s:</p>
                        <p>La administración revisó tu solicitud para <strong>%s</strong> y la aprobó.</p>
                        <p>Ya puedes iniciar sesión con tu usuario <strong>%s</strong> y la contraseña que elegiste.</p>
                        <p><a href="%s/login" style="background:#1f4e79;color:#fff;padding:10px 18px;border-radius:8px;text-decoration:none">Iniciar sesión</a></p>
                        """.formatted(Correos.seguro(solicitud.getNombre()),
                        Correos.seguro(Mappers.etiqueta(solicitud.getVivienda())),
                        Correos.seguro(solicitud.getUsuario()), urlSistema)));

        return new IdDto(solicitud.getId(), "Solicitud aprobada: se creó la cuenta de " + solicitud.getUsuario()
                + avisoCorreo(enviado, solicitud.getCorreo()));
    }

    @Transactional
    public IdDto rechazar(int id, String motivo, String revisadoPor) {
        SolicitudRegistroEntity solicitud = obtenerPendiente(id);

        solicitud.setEstado(SolicitudRegistroEntity.RECHAZADA);
        solicitud.setMotivoRechazo(motivo.trim());
        solicitud.setFechaRevision(ahora());
        solicitud.setRevisadoPor(revisadoPor);
        solicitudRepository.save(solicitud);

        boolean enviado = notificacionService.enviarCorreo(solicitud.getCorreo(), solicitud.getNombre(),
                "Tu solicitud en Las Torres fue rechazada",
                Correos.plantilla("Tu solicitud no fue aprobada", """
                        <p>Hola %s:</p>
                        <p>La administración revisó tu solicitud para <strong>%s</strong> y no pudo aprobarla.</p>
                        <p><strong>Motivo:</strong> %s</p>
                        <p>Si crees que es un error, comunícate con la administración o envía una nueva solicitud en
                        <a href="%s/registro">%s/registro</a>.</p>
                        """.formatted(Correos.seguro(solicitud.getNombre()),
                        Correos.seguro(Mappers.etiqueta(solicitud.getVivienda())),
                        Correos.seguro(solicitud.getMotivoRechazo()), urlSistema, urlSistema)));

        return new IdDto(solicitud.getId(), "Solicitud rechazada" + avisoCorreo(enviado, solicitud.getCorreo()));
    }

    // ------------------------------------------------------------------

    private SolicitudRegistroEntity obtenerEntidad(int id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la solicitud con id " + id));
    }

    private SolicitudRegistroEntity obtenerPendiente(int id) {
        SolicitudRegistroEntity solicitud = obtenerEntidad(id);
        if (!solicitud.getEstado().equals(SolicitudRegistroEntity.PENDIENTE)) {
            throw new ReglaNegocioException("Esta solicitud ya fue revisada (" + solicitud.getEstado().toLowerCase() + ")");
        }
        return solicitud;
    }

    private SolicitudDto toDto(SolicitudRegistroEntity e) {
        return new SolicitudDto(e.getId(), e.getNombre(), e.getApellidos(), e.getCorreo(), e.getTelefono(),
                Mappers.etiqueta(e.getVivienda()), e.getUsuario(), e.getEstado(), e.getFechaSolicitud(),
                e.getFechaRevision(), e.getRevisadoPor(), e.getMotivoRechazo(), e.getComprobanteNombre(), null);
    }

    private String avisoCorreo(boolean enviado, String correo) {
        return enviado
                ? ". Se notificó por correo a " + correo + "."
                : ". (No se envió correo: las notificaciones no están configuradas o fallaron.)";
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }

    private static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA_HORARIA);
    }
}
