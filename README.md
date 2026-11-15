# Condominios Las Torres — Control de acceso

API REST en **Java 21 + Spring Boot** para controlar quién entra y sale del condominio **Las Torres**: viviendas, residentes, visitantes autorizados y la bitácora de la caseta de vigilancia.

> **Avance 3:** interfaz web con inicio de sesión y roles (Thymeleaf + Spring Security), base de datos **PostgreSQL en Neon** y despliegue en **Render**.

**Demo en línea:** https://condominios-las-torres.onrender.com (el plan gratuito tarda cerca de un minuto en despertar)

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

## Usuarios y roles

El sistema pide iniciar sesión. Hay dos roles:

| Rol | Usuario inicial | Qué puede hacer |
|---|---|---|
| Administrador | `admin` | Todo: viviendas, residentes, visitantes, caseta, bitácora y Swagger |
| Guardia | `guardia` | Caseta: registrar entradas y salidas, y consultar la bitácora |

Las contraseñas se guardan cifradas con BCrypt. Los dos usuarios iniciales se crean la primera vez que arranca la aplicación, con las contraseñas de las variables de entorno `ADMIN_PASSWORD` y `GUARDIA_PASSWORD`. Si no están definidas, se genera una contraseña al azar que aparece una sola vez en el log de arranque.

La API (`/api/v1/...`) usa los mismos usuarios con autenticación HTTP Basic.

## Estructura del proyecto

```
src/main/java/mx/unam/aragon/tsp/lastorres/condominios
├── controllers      → endpoints REST (/api/v1/...)
├── webController    → pantallas web (Thymeleaf)
├── seguridad        → inicio de sesión y permisos por rol (Spring Security)
├── businessLayer    → reglas de negocio de cada módulo
├── dtos             → XDto (respuesta) y XDtoIn (entrada con validaciones)
├── entities         → tablas de la base de datos (JPA)
├── repositories     → acceso a la base de datos (JpaRepository)
├── exceptions       → NoEncontradoException (404), ReglaNegocioException (409)
└── helpers          → Mappers, DatosIniciales y ManejadorDeErrores
```

## ¿Cómo se echa a andar?

Requisitos: **JDK 21** y una base de datos PostgreSQL (el proyecto usa [Neon](https://neon.tech)).

Las credenciales **no** están en el código: se leen de tres variables de entorno.

| Variable | Ejemplo |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST/neondb?sslmode=require` |
| `DB_USER` | `neondb_owner` |
| `DB_PASSWORD` | la contraseña de la base |

En Windows se configuran una sola vez con `setx DB_URL "..."` (y lo mismo para las otras dos); después hay que reabrir la terminal o VS Code.

Al arrancar por primera vez, Hibernate crea las tablas y se cargan datos de ejemplo.

```bash
# Linux / Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

El sistema queda en `http://localhost:8081` (otro puerto: variable de entorno `PORT`).

- Swagger para probar desde el navegador: `http://localhost:8081/swagger-ui.html`
- Colección de Postman: `postman/LasTorres.postman_collection.json` (en sus variables se ponen `usuario` y `contrasena`)

### Con Docker

```bash
docker build -t condominios-las-torres .
docker run -p 8081:8081 -e DB_URL="..." -e DB_USER="..." -e DB_PASSWORD="..." condominios-las-torres
```

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

| Integrantes |
|---|
| Ovando Sánchez Paulina |
| Romero Gonzalez Diego |
| Román Zarza Samuel Iván |
| Yañez Medina Victor Enrique |
