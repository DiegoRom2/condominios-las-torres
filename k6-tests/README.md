# Pruebas de carga con K6 — Condominios Las Torres

Script de pruebas de carga en modo solo lectura para validar rendimiento y estabilidad de los endpoints principales de la API (incluyendo la exportación de reportes en PDF y Excel) sin alterar ni contaminar los datos de la base en Neon.

## Requisitos

Instalar [Grafana k6](https://grafana.com/docs/k6/latest/set-up/install-k6/):

```bash
# macOS (Homebrew)
brew install k6

# Windows (Winget)
winget install k6 --source winget
```

## Ejecución

### 1. Contra Render (valores por defecto)

Por defecto `load-test.js` apunta a `https://condominios-las-torres.onrender.com` con HTTP Basic (`admin` / `Diego2026`):

```bash
k6 run k6-tests/load-test.js
```

> **Nota sobre Render (plan gratuito):** El servicio puede tardar ~1 minuto en despertar tras inactividad. El script incluye una fase `setup()` con timeout de 90 s para despertar la instancia antes de medir las etapas de carga.

### 2. Contra entorno local (`localhost:8081`)

Sobrescribe las variables de entorno con `-e`:

```bash
k6 run \
  -e BASE_URL=http://localhost:8081 \
  -e K6_USER=admin \
  -e K6_PASSWORD=tu_contrasena \
  k6-tests/load-test.js
```

### 3. Prueba rápida de humo (1 usuario virtual, 1 iteración)

```bash
k6 run --vus 1 --iterations 1 k6-tests/load-test.js
```

## Endpoints evaluados

- `GET /api/v1/viviendas`
- `GET /api/v1/residentes`
- `GET /api/v1/visitantes`
- `GET /api/v1/accesos`
- `GET /api/v1/accesos/dentro`
- `GET /api/v1/reportes/bitacora/pdf`
- `GET /api/v1/reportes/bitacora/excel`
- `GET /api/v1/reportes/visitantes/pdf`
- `GET /api/v1/reportes/visitantes/excel`

## Consideraciones de infraestructura

- **Neon PostgreSQL:** `application.properties` limita el pool a `spring.datasource.hikari.maximum-pool-size=5`.
- **Render (plan gratuito):** 512 MB de RAM (`-XX:MaxRAMPercentage=75`).
- Por eso las etapas del script escalan de forma controlada hasta **10 VUs** con pausas de 1 s (`sleep(1)`).
