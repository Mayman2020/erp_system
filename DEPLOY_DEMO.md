# Production deploy (Docker + GHCR + VPS)

This repository deploys on every push to `main` using:

- Docker images pushed to `ghcr.io`
- GitHub Actions workflow: `.github/workflows/ci-cd.yml`
- Remote server deploy via SSH + `docker compose`

Pull requests to `main` run a Docker build only (no registry push, no deploy).

## What was added

- `docker-compose.yml` — local full stack: `docker compose up --build` (UI default `http://localhost:8080`, API `http://localhost:8091/api/v1`)
- `erp-system-frontend/Dockerfile`
- `erp-system-frontend/nginx/default.conf`
- `erp-system-frontend/.dockerignore`
- `docker-compose.prod.yml`
- `.github/workflows/ci-cd.yml`

## GHCR image names

- Backend: `ghcr.io/<owner-lowercase>/erp-backend`
- Frontend (Docker summary title / image): `ghcr.io/<owner-lowercase>/erp-system-dubai`

## Frontend build (single path)

There is **one** path: `erp-system-frontend/Dockerfile` runs `npm ci` + `npm run build:production` inside the image. GitHub Actions does **not** build Angular on the runner first. On `main`, **`NG_API_BASE_URL` is required** (repository **variable** or **secret**) so the production image calls your real API. Prefer a **variable** for a public URL (no need to hide it).

## Required GitHub Secrets

Set these in `Settings -> Secrets and variables -> Actions`:

- `NG_API_BASE_URL` (example: `https://your-host:8091/api/v1`) — **required** for pushes to `main`; baked into the Angular build. Add under **Variables** (recommended) or **Secrets** with the same name.
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
- `CORS_ALLOWED_ORIGINS` (must include the browser origin of your public frontend, e.g. `https://your-domain`)

Optional (recommended once DNS/firewall are stable):

- `DEPLOY_PUBLIC_FRONTEND_URL` — e.g. `https://your-domain/` — checked from the GitHub runner after deploy
- `DEPLOY_PUBLIC_HEALTH_URL` — full URL returning `OK`, e.g. `https://your-host:8091/api/v1/health`

## Server prerequisites

- Docker + Docker Compose plugin installed
- SSH access with provided key
- Outbound access to `ghcr.io`
- Ports open:
  - `80` for frontend
  - `8091` for backend API (optional if proxied through nginx)

## Deployment flow

1. Push to `main`
2. Workflow builds backend/frontend images and pushes to GHCR (`latest` + SHA tags)
3. `docker-compose.prod.yml` is copied to the server
4. Server logs in to GHCR, pulls images, runs `docker compose up -d`
5. Workflow waits for backend `http://127.0.0.1:8091/api/v1/health` and frontend on port `80` on the server
6. If optional public URL secrets are set, the runner curls those endpoints as an extra check

Manual runs: **Actions → CI/CD → Run workflow**. Enable **skip deploy** to build and push images only.

## If the `deploy` job fails (build is green)

1. **SSH user + Docker** — The deploy user must run `docker` without sudo (or use a user in group `docker`). Check on the server: `docker info` and `docker compose version`.
2. **GHCR login** — `GHCR_TOKEN` must be a classic PAT with `read:packages` (and access to this repo’s packages). `GHCR_USERNAME` is usually your GitHub username.
3. **`DEPLOY_PATH`** — The workflow creates this directory before `scp`. Use an absolute path (e.g. `/opt/erp-demo`).
4. **Firewall / port** — `DEPLOY_PORT` if not 22; security group must allow GitHub Actions to reach SSH (or use a self-hosted runner in the same network).
5. **Package visibility** — If GHCR images are private, the PAT must belong to an account that can pull them.
