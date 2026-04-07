# syntax=docker/dockerfile:1.6
#
# Two ways to get the nginx image:
# 1) Default (last stage `runtime`): full Node build inside Docker — for local `docker compose build`.
# 2) Target `runtime-ci`: copy pre-built `dist/` from build context — for GitHub Actions after `npm run build:production` on the runner (avoids OOM / flaky ng inside buildx).

FROM node:20-bookworm-slim AS build
WORKDIR /app

ENV NODE_OPTIONS=--max-old-space-size=12288
ENV NG_BUILD_MAX_WORKERS=1

COPY package*.json ./
RUN npm ci

COPY . .

ARG NG_API_BASE_URL=http://127.0.0.1:8091/api/v1
ENV NG_API_BASE_URL=${NG_API_BASE_URL}

RUN npm run build:production || npm run build -- --configuration production

# --- CI / pre-built bundle (use: docker build --target runtime-ci) ---
FROM nginx:1.27-alpine AS runtime-ci
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY dist/erp-system-frontend/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q -O- http://127.0.0.1/ >/dev/null || exit 1

# --- Default: build inside Docker ---
FROM nginx:1.27-alpine AS runtime
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q -O- http://127.0.0.1/ >/dev/null || exit 1
