# syntax=docker/dockerfile:1
FROM node:20-bullseye-slim AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
ARG NG_API_BASE_URL=http://localhost:8080/api/v1
ENV NG_API_BASE_URL=${NG_API_BASE_URL}
ENV CI=true
RUN npm run build

FROM nginx:alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
# Angular "application" builder output (v17+)
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
