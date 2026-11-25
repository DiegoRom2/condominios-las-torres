import http from 'k6/http';
import encoding from 'k6/encoding';
import { check, group, sleep } from 'k6';

// Configuración por defecto apuntando a Render; se puede sobrescribir con -e
const BASE_URL = (__ENV.BASE_URL || 'https://condominios-las-torres.onrender.com').replace(/\/+$/, '');
const K6_USER = __ENV.K6_USER || 'admin';
// La contraseña NUNCA se escribe en el código: se pasa al ejecutar, con -e K6_PASSWORD=...
const K6_PASSWORD = __ENV.K6_PASSWORD;
if (!K6_PASSWORD) {
  throw new Error('Falta la contraseña: ejecuta k6 con -e K6_PASSWORD=tu_contrasena');
}

const AUTH_HEADER = `Basic ${encoding.b64encode(`${K6_USER}:${K6_PASSWORD}`)}`;

const JSON_PARAMS = {
  headers: {
    Authorization: AUTH_HEADER,
    Accept: 'application/json',
  },
};

const BINARY_PARAMS = {
  headers: {
    Authorization: AUTH_HEADER,
  },
  responseType: 'binary',
};

// Etapas calibradas para no saturar Neon (hikari.maximum-pool-size=5) ni Render (512 MB RAM)
export const options = {
  setupTimeout: '120s',
  stages: [
    { duration: '15s', target: 5 },  // Subida gradual
    { duration: '30s', target: 10 }, // Carga sostenida
    { duration: '15s', target: 0 },  // Bajada controlada
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],        // Menos de 1% de errores
    http_req_duration: ['p(95)<2500'],     // 95% de peticiones debajo de 2.5s
    checks: ['rate>0.99'],                 // Al menos 99% de checks exitosos
  },
};

/**
 * Despierta el servicio en Render (plan gratuito puede tardar ~1 min en arrancar)
 * antes de medir las etapas de carga.
 */
export function setup() {
  const res = http.get(`${BASE_URL}/api/v1/viviendas`, {
    headers: JSON_PARAMS.headers,
    timeout: '90s',
  });
  check(res, {
    'setup: API disponible y autenticada (200)': (r) => r.status === 200,
  });
}

export default function () {
  group('Catálogos principales', () => {
    const resViviendas = http.get(`${BASE_URL}/api/v1/viviendas`, JSON_PARAMS);
    check(resViviendas, {
      'GET /api/v1/viviendas -> 200': (r) => r.status === 200,
      'viviendas devuelve arreglo JSON': (r) => Array.isArray(r.json()),
    });

    const resResidentes = http.get(`${BASE_URL}/api/v1/residentes`, JSON_PARAMS);
    check(resResidentes, {
      'GET /api/v1/residentes -> 200': (r) => r.status === 200,
      'residentes devuelve arreglo JSON': (r) => Array.isArray(r.json()),
    });

    const resVisitantes = http.get(`${BASE_URL}/api/v1/visitantes`, JSON_PARAMS);
    check(resVisitantes, {
      'GET /api/v1/visitantes -> 200': (r) => r.status === 200,
      'visitantes devuelve arreglo JSON': (r) => Array.isArray(r.json()),
    });
  });

  group('Caseta y bitácora', () => {
    const resAccesos = http.get(`${BASE_URL}/api/v1/accesos`, JSON_PARAMS);
    check(resAccesos, {
      'GET /api/v1/accesos -> 200': (r) => r.status === 200,
      'accesos devuelve arreglo JSON': (r) => Array.isArray(r.json()),
    });

    const resDentro = http.get(`${BASE_URL}/api/v1/accesos/dentro`, JSON_PARAMS);
    check(resDentro, {
      'GET /api/v1/accesos/dentro -> 200': (r) => r.status === 200,
      'accesos/dentro devuelve arreglo JSON': (r) => Array.isArray(r.json()),
    });
  });

  group('Exportación de reportes (PDF y Excel)', () => {
    const resBitacoraPdf = http.get(`${BASE_URL}/api/v1/reportes/bitacora/pdf`, BINARY_PARAMS);
    check(resBitacoraPdf, {
      'GET /api/v1/reportes/bitacora/pdf -> 200': (r) => r.status === 200,
      'bitácora PDF no vacío': (r) => r.body && r.body.byteLength > 100,
    });

    const resBitacoraExcel = http.get(`${BASE_URL}/api/v1/reportes/bitacora/excel`, BINARY_PARAMS);
    check(resBitacoraExcel, {
      'GET /api/v1/reportes/bitacora/excel -> 200': (r) => r.status === 200,
      'bitácora Excel no vacío': (r) => r.body && r.body.byteLength > 100,
    });

    const resVisitantesPdf = http.get(`${BASE_URL}/api/v1/reportes/visitantes/pdf`, BINARY_PARAMS);
    check(resVisitantesPdf, {
      'GET /api/v1/reportes/visitantes/pdf -> 200': (r) => r.status === 200,
      'visitantes PDF no vacío': (r) => r.body && r.body.byteLength > 100,
    });

    const resVisitantesExcel = http.get(`${BASE_URL}/api/v1/reportes/visitantes/excel`, BINARY_PARAMS);
    check(resVisitantesExcel, {
      'GET /api/v1/reportes/visitantes/excel -> 200': (r) => r.status === 200,
      'visitantes Excel no vacío': (r) => r.body && r.body.byteLength > 100,
    });
  });

  sleep(1);
}
