FROM node:22-alpine AS build
WORKDIR /app

ENV NODE_OPTIONS=--max-old-space-size=6144

COPY package*.json ./
RUN npm ci

COPY . .

# Browser calls API on host port mapped to backend (see root docker-compose.yml).
ARG NG_API_BASE_URL=http://127.0.0.1:8091/api/v1
ENV NG_API_BASE_URL=${NG_API_BASE_URL}
# Do not set CI=true: scripts/write-env.cjs exits 1 in CI when URL is missing.
RUN npm run build:production

FROM nginx:1.27-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q -O- http://127.0.0.1/ >/dev/null || exit 1
