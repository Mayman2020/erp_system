import type { AppEnvironment } from './environment.types';

function resolveApiUrl(): string {
  if (typeof window !== 'undefined') {
    const runtime = (window as Window & { __ERP_API_URL__?: string }).__ERP_API_URL__?.trim();
    if (runtime) {
      return runtime;
    }
  }
  return '/api/v1';
}

export const environment: AppEnvironment = {
  production: false,
  apiUrl: resolveApiUrl(),
  appVersion: '21.0.0'
};
