# Build on Debian slim (glibc): Angular 21 + esbuild/sass are more reliable than Alpine/musl in CI.
FROM node:20-bookworm-slim AS build
WORKDIR /app

ENV NODE_OPTIONS=--max-old-space-size=8192
# Reduce parallel workers to lower peak memory in constrained builders (Docker Hub, small runners).
ENV NG_BUILD_MAX_WORKERS=2

COPY package*.json ./
RUN npm ci

COPY . .

ARG NG_API_BASE_URL=http://127.0.0.1:8091/api/v1
ENV NG_API_BASE_URL=${NG_API_BASE_URL}
# Do not set CI=true: write-env.cjs exits 1 in CI when URL is missing.
# Fallback: plain `ng build` with production configuration if the primary script path fails.
RUN npm run build:production || npm run build -- --configuration production

FROM nginx:1.27-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q -O- http://127.0.0.1/ >/dev/null || exit 1
