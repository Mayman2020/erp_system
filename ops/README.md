# Operations layout (`ops/`)

| Folder | Purpose |
|--------|---------|
| **`common/`** | Shared references for Docker + dev + docs (ports, URLs, sample nginx). |
| **`docker/`** | Compose files, Hub build/push scripts, local stack runners (`run-docker.*`). |
| **`environment/`** | `.env.example` — copy to `.env` here or at repo root for secrets. |
| **`frontend-run/`** | Dev proxy + scripts to start Angular (`run-frontend.*`). |

## Quick commands

- **Full stack (Docker):** from repo root `docker compose up --build` or `ops\docker\run-docker.bat`
- **Angular dev:** `ops\frontend-run\run-frontend.ps1` (or `.sh`)
- **Build & push Hub images:** `ops\docker\build-and-push.ps1`
- **Optional backend build from repo root:**  
  `docker build -f ops/docker/Dockerfile.backend-from-root -t mayman2020/erp-backend:latest .`

Module Dockerfiles stay in `erp-system-backend/` and `erp-system-frontend/` (build contexts).
