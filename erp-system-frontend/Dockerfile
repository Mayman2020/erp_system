# syntax=docker/dockerfile:1
FROM node:20-bullseye-slim AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# Same-origin `/api/v1` when UI nginx proxies to service `backend` (see nginx/default.conf).
# Override for split deploy, e.g. --build-arg NG_API_BASE_URL=https://api.example.com/api/v1
ARG NG_API_BASE_URL=/api/v1
ENV NG_API_BASE_URL=${NG_API_BASE_URL}
ENV CI=true
RUN npm run build

FROM nginx:alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
# Angular "application" builder output (v17+)
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
