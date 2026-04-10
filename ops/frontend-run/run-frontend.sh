#!/usr/bin/env bash
set -euo pipefail
OPS_FRONTEND="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${OPS_FRONTEND}/../.." && pwd)"
cd "${REPO_ROOT}/erp-system-frontend"
npm run start
