# Demo Auto Deploy (Docker + Push)

This repository is configured to auto-deploy on every push to `main` using:

- Docker images pushed to `ghcr.io`
- GitHub Actions workflow: `.github/workflows/cd-demo.yml`
- Remote server deploy via SSH + `docker compose`

## What was added

- `docker-compose.yml` — local full stack: `docker compose up --build` (UI default `http://localhost:8080`, API `http://localhost:8091/api/v1`)
- `erp-system-frontend/Dockerfile`
- `erp-system-frontend/nginx/default.conf`
- `erp-system-frontend/.dockerignore`
- `docker-compose.prod.yml`
- `.github/workflows/cd-demo.yml`

## GHCR image names

- Backend: `ghcr.io/<owner-lowercase>/erp-backend`
- Frontend (Docker summary title / image): `ghcr.io/<owner-lowercase>/erp-system-dubai`

## Required GitHub Secrets

Set these in `Settings -> Secrets and variables -> Actions`:

- `NG_API_BASE_URL` (example: `https://api.example.com/api/v1`) — **required for a real demo**: the frontend bakes this at build time. If unset, CI uses a localhost fallback so the Docker build still passes; set the secret before relying on the deployed UI.
- `DEPLOY_HOST`
- `DEPLOY_PORT` (optional, default 22)
- `DEPLOY_USER`
- `DEPLOY_SSH_KEY` (private key contents)
- `DEPLOY_PATH` (example: `/opt/erp-demo`)
- `GHCR_USERNAME` (GitHub username with package read access)
- `GHCR_TOKEN` (classic PAT with `read:packages`, and if needed `write:packages`)
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS` (frontend public URL)

## Server prerequisites

- Docker + Docker Compose plugin installed
- SSH access with provided key
- Outbound access to `ghcr.io`
- Ports open:
  - `80` for frontend
  - `8091` for backend API (optional if proxied through nginx)

## Deployment flow

1. Push to `main`
2. Workflow builds backend/frontend images
3. Images are pushed to GHCR
4. `docker-compose.prod.yml` is copied to server
5. Server pulls latest images and restarts containers
