/**
 * For Netlify/Vercel, NG_API_BASE_URL is applied by scripts/write-env.cjs before build.
 * Example: https://your-api.onrender.com/api/v1 (must match server.servlet.context-path).
 */
export const environment = {
  production: true,
  apiBaseUrl: 'https://REPLACE-WITH-YOUR-API-HOST/api/v1'
};
