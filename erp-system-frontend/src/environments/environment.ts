// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import type { AppEnvironment } from './environment.types';

export const environment: AppEnvironment = {
  production: false,
  /**
   * Dev: relative URL so `ng serve` proxy (ops/frontend-run/proxy.conf.json) forwards to Spring Boot on 8081.
   * Production builds use the same path when the UI is served behind a reverse proxy with the API.
   */
  apiUrl: '/api/v1',
  /** Shown in shell footer (aligned with package.json for releases). */
  appVersion: '21.0.0'
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
