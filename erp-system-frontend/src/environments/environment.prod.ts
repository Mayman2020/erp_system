/**
 * Production default: same-origin or relative `/api/v1`.
 * Docker / CI: `npm run build` runs `write-env.cjs` — set NG_API_BASE_URL to the public API root
 * (e.g. http://93.127.141.227:10080/api/v1 when the UI and API are on different ports).
 */
import type { AppEnvironment } from './environment.types';

export const environment: AppEnvironment = {
  production: true,
  apiUrl: '/api/v1',
  appVersion: '21.0.0'
};
