#!/usr/bin/env bash
set -euo pipefail

# Docker Hub (defaults in docker-compose.prod.yml): e.g.
#   export BACKEND_IMAGE=mayman2020/erp-backend:latest FRONTEND_IMAGE=mayman2020/erp-frontend:latest
#   echo "$DOCKERHUB_TOKEN" | docker login -u mayman2020 --password-stdin

: "${BACKEND_IMAGE:?Set BACKEND_IMAGE=ghcr.io/OWNER/erp-system-backend:latest}"
: "${FRONTEND_IMAGE:?Set FRONTEND_IMAGE=ghcr.io/OWNER/erp-system-frontend:latest}"
: "${SPRING_DATASOURCE_URL:?}"
: "${SPRING_DATASOURCE_USERNAME:?}"
: "${SPRING_DATASOURCE_PASSWORD:?}"
: "${JWT_SECRET:?}"
: "${POSTGRES_USER:?}"
: "${POSTGRES_PASSWORD:?}"
: "${POSTGRES_DB:?}"

if [[ -n "${GHCR_USERNAME:-}" ]] && [[ -n "${GHCR_TOKEN:-}" ]]; then
  echo "Logging in to GHCR..."
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
fi

if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "docker compose or docker-compose is required." >&2
  exit 1
fi

echo "Pulling production images..."
"${COMPOSE_CMD[@]}" -f docker-compose.prod.yml pull

echo "Stopping old services..."
"${COMPOSE_CMD[@]}" -f docker-compose.prod.yml down --remove-orphans || true

echo "Starting production stack..."
"${COMPOSE_CMD[@]}" -f docker-compose.prod.yml up -d

echo "Deployment complete."
"${COMPOSE_CMD[@]}" -f docker-compose.prod.yml ps
