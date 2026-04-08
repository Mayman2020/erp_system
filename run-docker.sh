#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "Checking Docker availability..."
if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: Docker is not installed or not in PATH."
  echo "Install Docker Desktop for Windows and restart your terminal."
  exit 1
fi

echo "Checking Docker Compose availability..."
if ! docker compose version >/dev/null 2>&1; then
  echo "ERROR: docker compose is not available."
  echo "Make sure Docker Desktop is installed and Compose is enabled."
  exit 1
fi

echo "Checking Docker compose configuration..."

if [ -f .env ]; then
  echo "Loading .env file"
  set -o allexport
  # shellcheck disable=SC1091
  source .env
  set +o allexport
else
  echo "Warning: .env file not found. Using defaults from docker-compose.yml and .env.example if needed."
fi

echo "Validating docker-compose..."
docker compose config >/dev/null

echo "Docker compose is valid. Starting local Docker stack..."

docker compose up --build
