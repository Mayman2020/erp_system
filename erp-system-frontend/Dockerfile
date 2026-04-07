FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG NG_API_BASE_URL
ENV NG_API_BASE_URL=${NG_API_BASE_URL}
ENV CI=true
RUN npm run build:production

FROM nginx:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/erp-system-frontend/browser /usr/share/nginx/html

EXPOSE 80
