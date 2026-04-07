/**
 * Production: set `apiUrl` to your Railway API root (full URL including /api/v1), or inject at build time
 * via NG_API_BASE_URL (see scripts/write-env.cjs — used on Vercel).
 */
import type { AppEnvironment } from './environment.types';

export const environment: AppEnvironment = {
  production: true,
  apiUrl: 'REPLACE_WITH_RAILWAY_URL',
  appVersion: '21.0.0'
};
