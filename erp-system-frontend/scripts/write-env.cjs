'use strict';
/**
 * Injects NG_API_BASE_URL into environment.prod.ts before `ng build --configuration production`.
 * Vercel: set NG_API_BASE_URL to your Railway API root, e.g. https://your-service.up.railway.app/api/v1
 */
const fs = require('fs');
const path = require('path');

const url = process.env.NG_API_BASE_URL || process.env.API_BASE_URL;
const pkgPath = path.join(__dirname, '..', 'package.json');
const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
const appVersion = String(pkg.version || '0.0.0').replace(/'/g, "\\'");
const outPath = path.join(__dirname, '..', 'src', 'environments', 'environment.prod.ts');

const isCi =
  process.env.CI === 'true' ||
  process.env.NETLIFY === 'true' ||
  process.env.VERCEL === '1';

if (!url) {
  if (isCi) {
    console.error(
      'NG_API_BASE_URL (or API_BASE_URL) must be set in CI so the production build points at your deployed API.'
    );
    process.exit(1);
  }
  console.log('NG_API_BASE_URL not set; leaving src/environments/environment.prod.ts unchanged.');
  process.exit(0);
}

const escaped = String(url).replace(/\\/g, '\\\\').replace(/'/g, "\\'");
const content = `/**
 * Production: injected by scripts/write-env.cjs from NG_API_BASE_URL at build time.
 */
import type { AppEnvironment } from './environment.types';

export const environment: AppEnvironment = {
  production: true,
  apiUrl: '${escaped}',
  appVersion: '${appVersion}'
};
`;
fs.writeFileSync(outPath, content, 'utf8');
console.log('Wrote', outPath);
