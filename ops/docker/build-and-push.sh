#!/usr/bin/env bash
# Build and push ERP backend + frontend images to Docker Hub.
# Run from anywhere; script resolves repository root automatically.

set -euo pipefail

readonly DOCKER_USER="${DOCKER_USER:-mayman2020}"
readonly IMAGE_TAG="${IMAGE_TAG:-latest}"
readonly NG_API_BASE_URL="${NG_API_BASE_URL:-/api/v1}"

readonly BACKEND_IMAGE="${DOCKER_USER}/erp-backend:${IMAGE_TAG}"
readonly FRONTEND_IMAGE="${DOCKER_USER}/erp-frontend:${IMAGE_TAG}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

log() { printf '\n[%s] %s\n' "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" "$*"; }
die() { echo "ERROR: $*" >&2; exit 1; }

command -v docker >/dev/null 2>&1 || die "docker not found in PATH"

cd "${REPO_ROOT}" || die "cannot cd to ${REPO_ROOT}"

if [[ -n "${DOCKERHUB_TOKEN:-}" ]]; then
  log "Logging in to Docker Hub as ${DOCKER_USER} (DOCKERHUB_TOKEN)..."
  printf '%s' "${DOCKERHUB_TOKEN}" | docker login -u "${DOCKER_USER}" --password-stdin
elif [[ -n "${DOCKER_PASSWORD:-}" ]]; then
  log "Logging in to Docker Hub as ${DOCKER_USER} (DOCKER_PASSWORD)..."
  printf '%s' "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USER}" --password-stdin
fi

log "Repository root: ${REPO_ROOT}"
log "IMAGE_TAG=${IMAGE_TAG}"
log "NG_API_BASE_URL=${NG_API_BASE_URL}"
log "Backend image:  ${BACKEND_IMAGE}"
log "Frontend image: ${FRONTEND_IMAGE}"

log "Building backend..."
docker build \
  -t "${BACKEND_IMAGE}" \
  -f "${REPO_ROOT}/erp-system-backend/Dockerfile" \
  "${REPO_ROOT}/erp-system-backend"

log "Pushing backend..."
docker push "${BACKEND_IMAGE}"

log "Building frontend (baking API URL into the bundle)..."
docker build \
  --build-arg "NG_API_BASE_URL=${NG_API_BASE_URL}" \
  -t "${FRONTEND_IMAGE}" \
  -f "${REPO_ROOT}/erp-system-frontend/Dockerfile" \
  "${REPO_ROOT}/erp-system-frontend"

log "Pushing frontend..."
docker push "${FRONTEND_IMAGE}"

log "Done. Images pushed:"
log "  ${BACKEND_IMAGE}"
log "  ${FRONTEND_IMAGE}"
