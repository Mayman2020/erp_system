# syntax=docker/dockerfile:1.6
#
# Single production path (local + CI): install deps → production Angular build → nginx.
# No runner-built dist, no --target flags. Same Dockerfile everywhere.

FROM node:20-bookworm-slim AS build
WORKDIR /app

# GitHub / local: avoid write-env.cjs treating the container as "strict CI" with empty URL.
ENV CI=false
ENV NODE_OPTIONS=--max-old-space-size=6144
ENV NG_BUILD_MAX_WORKERS=1

COPY package*.json ./
RUN npm ci

COPY . .

ARG NG_API_BASE_URL=http://127.0.0.1:8091/api/v1
ENV NG_API_BASE_URL=${NG_API_BASE_URL}

RUN npm run build:production

FROM nginx:1.27-alpine AS runtime
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q -O- http://127.0.0.1/ >/dev/null || exit 1
