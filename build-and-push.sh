#!/usr/bin/env bash
exec "$(dirname "$0")/ops/docker/build-and-push.sh" "$@"
