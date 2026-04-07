/** Shared shape for dev + production builds (write-env rewrites prod before ng build). */
export interface AppEnvironment {
  production: boolean;
  /** Full API root including context path, e.g. https://xxx.up.railway.app/api/v1 */
  apiUrl: string;
  appVersion: string;
}
