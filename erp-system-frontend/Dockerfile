FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# Default matches local docker-compose backend port mapping; override in CI with --build-arg.
ARG NG_API_BASE_URL=http://127.0.0.1:8091/api/v1
ENV NG_API_BASE_URL=${NG_API_BASE_URL}
# Do not set CI=true here: write-env.cjs would hard-fail when the URL is empty.
RUN npm run build:production

FROM nginx:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html

EXPOSE 80
