#!/usr/bin/env bash
# Pull the latest backend image from GHCR and replace the running container on EC2 (or any Docker host).
# Copy to the server (e.g. /opt/erp/deploy-ec2-docker.sh), chmod +x, then set env vars and run.
#
# Required env:
#   BACKEND_IMAGE   e.g. ghcr.io/myorg/myrepo-backend:latest
# Optional (for private GHCR):
#   GHCR_USERNAME, GHCR_TOKEN   (PAT with read:packages)
# Required for app runtime:
#   SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
#   JWT_SECRET
set -euo pipefail

: "${BACKEND_IMAGE:?Set BACKEND_IMAGE (e.g. ghcr.io/org/repo-backend:latest)}"
: "${SPRING_DATASOURCE_URL:?}"
: "${SPRING_DATASOURCE_USERNAME:?}"
: "${SPRING_DATASOURCE_PASSWORD:?}"
: "${JWT_SECRET:?}"

if [[ -n "${GHCR_TOKEN:-}" ]] && [[ -n "${GHCR_USERNAME:-}" ]]; then
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
fi

docker pull "$BACKEND_IMAGE"
docker stop erp-backend 2>/dev/null || true
docker rm erp-backend 2>/dev/null || true

docker run -d \
  --name erp-backend \
  --restart unless-stopped \
  -p 8080:8080 \
  -e PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
  -e SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
  -e SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
  -e JWT_SECRET="$JWT_SECRET" \
  "$BACKEND_IMAGE"

echo "Started erp-backend from $BACKEND_IMAGE"
