# Condominios Las Torres — Control de acceso

API REST en **Java 21 + Spring Boot** para controlar quién entra y sale del condominio **Las Torres**: viviendas, residentes, visitantes autorizados y la bitácora de la caseta de vigilancia.

> **Avance 1:** estructura del proyecto con DTOs, controladores y servicios (business layer).
> Por ahora los datos viven en memoria (`helpers/DatosEnMemoria`) y se reinician al apagar la app.
> En el siguiente avance se conecta la base de datos PostgreSQL en Neon.

## ¿Qué hace?

- Registra las **viviendas** (torre, número y piso).
- Registra a los **residentes** de cada vivienda.
- Cada residente da de alta a sus **visitantes autorizados**.
- El guardia registra la **entrada** y la **salida** de cada visitante y puede consultar **quién está dentro** en este momento.

### Reglas de negocio

- No puede haber dos viviendas con la misma torre y número.
- No se borra una vivienda con residentes, ni un residente con visitantes autorizados.
- Dos residentes no pueden compartir correo.
- Solo un residente existente puede autorizar visitantes.
- Un visitante no puede entrar dos veces sin haber salido, ni registrar dos salidas del mismo acceso.
- No se borra un visitante mientras está dentro del condominio.

## Estructura del proyecto

```
src/main/java/mx/unam/aragon/tsp/lastorres/condominios
├── controllers      → endpoints REST (/api/v1/...)
├── businessLayer    → reglas de negocio de cada módulo
├── dtos             → XDto (respuesta) y XDtoIn (entrada con validaciones)
├── exceptions       → NoEncontradoException (404), ReglaNegocioException (409)
└── helpers          → DatosEnMemoria y ManejadorDeErrores
```

## ¿Cómo se echa a andar?

Requisitos: **JDK 21**.

```bash
# Linux / Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

La API queda en `http://localhost:8081` (otro puerto: variable de entorno `PORT`).

- Swagger para probar desde el navegador: `http://localhost:8081/swagger-ui.html`
- Colección de Postman: `postman/LasTorres.postman_collection.json`

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/viviendas` | Lista de viviendas |
| GET | `/api/v1/viviendas/{id}` | Una vivienda |
| POST | `/api/v1/viviendas` | Crear vivienda |
| PUT | `/api/v1/viviendas/{id}` | Actualizar vivienda |
| DELETE | `/api/v1/viviendas/{id}` | Borrar vivienda |
| GET / POST | `/api/v1/residentes` | Listar / crear residentes |
| GET / PUT / DELETE | `/api/v1/residentes/{id}` | Consultar / actualizar / borrar |
| GET / POST | `/api/v1/visitantes` | Listar / crear visitantes |
| GET / PUT / DELETE | `/api/v1/visitantes/{id}` | Consultar / actualizar / borrar |
| GET | `/api/v1/accesos` | Bitácora completa (más reciente primero) |
| GET | `/api/v1/accesos/dentro` | Visitantes dentro en este momento |
| GET | `/api/v1/accesos/{id}` | Un acceso |
| POST | `/api/v1/accesos/entrada` | Registrar entrada |
| PUT | `/api/v1/accesos/{id}/salida` | Registrar salida |

### Ejemplos de cuerpo (JSON)

```json
// POST /api/v1/residentes
{ "nombre": "María", "apellidos": "Pérez Gómez", "correo": "maria@ejemplo.com", "telefono": "5512345678", "viviendaId": 2 }

// POST /api/v1/accesos/entrada
{ "visitanteId": 2, "motivo": "Entrega de paquete" }
```

### Respuesta de error

```json
{ "estatus": 400, "mensaje": "Los datos enviados no son válidos", "detalles": ["correo: El correo no tiene un formato válido"] }
```

## Equipo

**Las Torres** — UNAM, FES Aragón.
