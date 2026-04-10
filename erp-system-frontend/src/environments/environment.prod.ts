/**
 * Production default: same-origin `/api/v1` — nginx in the frontend container proxies to Spring Boot.
 * CI/Docker may override via `NG_API_BASE_URL` (see scripts/write-env.cjs) for split UI/API hosting.
 */
import type { AppEnvironment } from './environment.types';

export const environment: AppEnvironment = {
  production: true,
  apiUrl: '/api/v1',
  appVersion: '21.0.0'
};
