/** Shared shape for dev + production builds (write-env rewrites prod before ng build). */
export interface AppEnvironment {
  production: boolean;
  apiBaseUrl: string;
  appVersion: string;
}
