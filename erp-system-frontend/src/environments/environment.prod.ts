/**
 * Production: absolute API root so the browser calls the backend host:port (not the SPA origin).
 * Dev keeps relative `/api/v1` + proxy in `environment.ts`.
 */
import type { AppEnvironment } from './environment.types';

export const environment: AppEnvironment = {
  production: true,
  apiUrl: 'http://93.127.141.227:10080/api/v1',
  appVersion: '21.0.0'
};
