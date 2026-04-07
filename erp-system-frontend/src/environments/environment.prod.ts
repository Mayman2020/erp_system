/**
 * For Netlify/Vercel, NG_API_BASE_URL is applied by scripts/write-env.cjs before build.
 * Example: https://your-api.onrender.com/api/v1 (must match server.servlet.context-path).
 */
import type { AppEnvironment } from './environment.types';

export const environment: AppEnvironment = {
  production: true,
  apiBaseUrl: 'https://REPLACE-WITH-YOUR-API-HOST/api/v1',
  appVersion: '21.0.0'
};
