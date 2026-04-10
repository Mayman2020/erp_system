# ERP system — deployment and CI/CD

This repository includes a **Spring Boot** API (`erp-system-backend`), an **Angular** UI (`erp-system-frontend`), **Docker** images, **GitHub Actions** (build, test, push to GHCR), and hooks for **Railway** and **AWS**.

## Architecture summary

| Component | Stack | Default URL (local compose) |
|-----------|--------|------------------------------|
| API | Java 17, Spring Boot 3 | `http://localhost:8080/api/v1` |
| UI | Angular 21, nginx | `http://localhost:8081` |
| DB | PostgreSQL 16 | `localhost:5432` |

## Health endpoints

| Path | Purpose |
|------|---------|
| `GET /health` | JSON `{"status":"UP"}` at **Tomcat root** (Railway / load balancers). |
| `GET /actuator/health` | Same at root; full Actuator payload at **`/api/v1/actuator/health`**. |

## Environment variables (production API)

Prefer **standard Spring datasource** variables (12-factor). Railway-style variables still work.

| Variable | Description |
|----------|-------------|
| `PORT` | HTTP port (Railway injects this). |
| `SPRING_PROFILES_ACTIVE` | Use `prod` in production. |
| `SPRING_DATASOURCE_URL` | JDBC URL (e.g. `jdbc:postgresql://host:5432/db`). |
| `SPRING_DATASOURCE_USERNAME` | DB user. |
| `SPRING_DATASOURCE_PASSWORD` | DB password. |
| `DB_URL`, `DB_USER`, `DB_PASS` | Alternate names (still supported). |
| `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` | Railway Postgres plugin (supported). |
| `JWT_SECRET` | **Required in production** — long random string. |

Do **not** commit real secrets. Use platform secret stores or GitHub Actions secrets.

## Local Docker Compose

From the repository root:

```bash
docker compose up --build
```

- API: `http://localhost:8080/api/v1`
- UI: `http://localhost:8081` (built with `NG_API_BASE_URL=http://localhost:8080/api/v1`)

Override JWT for local runs:

```bash
JWT_SECRET=dev-secret-change-me docker compose up --build
```

## Backend image (Dockerfile)

- **Build**: Eclipse Temurin JDK 17 + `./mvnw` (official `openjdk:*-slim` is deprecated).
- **Runtime**: JRE 17, JAR at `/app/app.jar`, `ENV PORT=8080`.

Build manually:

```bash
cd erp-system-backend
docker build -t erp-backend:local .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=... -e SPRING_DATASOURCE_USERNAME=... -e SPRING_DATASOURCE_PASSWORD=... \
  -e JWT_SECRET=... \
  erp-backend:local
```

## Frontend image (Dockerfile)

Production build runs `scripts/write-env.cjs` (requires `NG_API_BASE_URL` when `CI=true`).

```bash
cd erp-system-frontend
docker build --build-arg NG_API_BASE_URL=https://your-api.example.com/api/v1 -t erp-frontend:local .
docker run --rm -p 8081:80 erp-frontend:local
```

## GitHub Actions (`.github/workflows/deploy.yml`)

**Triggers**: push to `main`, `workflow_dispatch`.

**Jobs**

1. **Backend** — `./mvnw -B verify` (compile + unit tests).
2. **Frontend** — `npm ci`, `npm run build` (needs `NG_API_BASE_URL` when `CI=true`; uses repository secret or falls back to `http://localhost:8080/api/v1`).
3. **container-images** (only on `main`) — build and push:
   - `ghcr.io/<owner_lower>/<repo_lower>-backend:latest` (+ `:sha`)
   - `ghcr.io/<owner_lower>/<repo_lower>-frontend:latest` (+ `:sha`)

**Permissions**: `packages: write` for GHCR.

### Repository secrets (recommended)

| Secret | Purpose |
|--------|---------|
| `NG_API_BASE_URL` | Public API root for Angular production build, e.g. `https://your-service.up.railway.app/api/v1`. |
| `RAILWAY_DEPLOY_HOOK_URL` | Optional POST webhook to trigger a Railway deployment after images are pushed. |
| `AWS_EC2_HOST`, `AWS_EC2_USER`, `AWS_EC2_SSH_KEY` | EC2 SSH deploy. |
| `AWS_SPRING_DATASOURCE_*`, `JWT_SECRET` | Runtime env for EC2 container. |
| `GHCR_PULL_TOKEN`, `GHCR_USERNAME` | PAT with `read:packages` if GHCR images are private. |

### Repository variables

| Variable | Purpose |
|----------|---------|
| `AWS_EC2_DEPLOY_ENABLED` | Set to `true` to enable the optional EC2 SSH deploy job. |

## Railway

- Config: `erp-system-backend/railway.json` (Dockerfile builder, healthcheck `/health`).
- Connect the GitHub repo in Railway for **automatic deploys on push**, or add **`RAILWAY_DEPLOY_HOOK_URL`** in GitHub to trigger after GHCR push.
- Ensure **`PORT`** is not overridden incorrectly; the app reads `server.port=${PORT:8080}`.

## AWS EC2 (manual script)

1. Install Docker on the instance; open port **8080** (or put nginx/ALB in front).
2. Copy `scripts/deploy-ec2-docker.sh` to the server and `chmod +x`.
3. Export variables and run:

```bash
export BACKEND_IMAGE=ghcr.io/yourorg/yourrepo-backend:latest
export SPRING_DATASOURCE_URL=jdbc:postgresql://...
export SPRING_DATASOURCE_USERNAME=...
export SPRING_DATASOURCE_PASSWORD=...
export JWT_SECRET=...
export GHCR_USERNAME=your-github-username
export GHCR_TOKEN=ghp_xxx   # PAT read:packages if image is private
./deploy-ec2-docker.sh
```

## AWS ECS (sample)

See `infra/aws/ecs-task-definition.sample.json` as a starting point for Fargate. Wire Secrets Manager ARNs, IAM roles, target group health check on `/health`, and point the task image at your GHCR backend image.

## Troubleshooting

- **Healthcheck “service unavailable”**: nothing listening on `PORT` — check container logs, JAR path, and JVM exit codes.
- **Frontend calls wrong API**: prefer same-origin `/api/v1` (nginx in the frontend image proxies to `backend:8080`). If you bake an absolute `NG_API_BASE_URL`, it must use the **public** host:port your users reach (not the VPS inner port). Many NAT panels map public `10027` → inner `10080`, so `http://PUBLIC_IP:10080` from a browser is wrong unless that port is published publicly.
- **Database connection timeouts**: Hikari `initialization-fail-timeout` is extended in `application-prod.yml`; verify JDBC URL and network from the container.
