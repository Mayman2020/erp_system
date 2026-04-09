/** Shared shape for dev + production builds (write-env rewrites prod before ng build). */
export interface AppEnvironment {
  production: boolean;
  /** API root including context path (absolute in prod; relative `/api/v1` in dev with ng serve proxy). */
  apiUrl: string;
  appVersion: string;
}
