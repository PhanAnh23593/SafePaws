#!/usr/bin/env bash
# Run on EC2 from any directory: sudo bash deploy/update.sh [--https]
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
if [[ $# -gt 1 || ( $# -eq 1 && "$1" != "--https" ) ]]; then
  echo "Usage: sudo bash deploy/update.sh [--https]" >&2
  exit 2
fi
compose=(docker compose --env-file .env.aws -f compose.aws.yml)
if [[ "${1:-}" == "--https" ]]; then
  compose+=(--profile https)
fi
"${compose[@]}" config --quiet
# Complete the build before interrupting the currently running API.
"${compose[@]}" build auth
"${compose[@]}" up -d --wait postgres redis
"${compose[@]}" stop auth
umask 077
mkdir -p backups
backup="backups/auth-$(date -u +%Y%m%dT%H%M%SZ).dump"
"${compose[@]}" exec -T postgres pg_dump -U zippy_auth -d auth_db -Fc > "$backup"
test -s "$backup"
# V2 and V3 are repeatable. Initialization of a new volume is handled by V1.
for migration in V2__auth_security.sql V3__token_sessions.sql; do
  "${compose[@]}" exec -T postgres psql -U zippy_auth -d auth_db -v ON_ERROR_STOP=1 \
    < "src/main/resources/db/schema/$migration"
done
"${compose[@]}" up -d --no-build
for attempt in {1..60}; do
  if curl --fail --silent --max-time 3 http://127.0.0.1:8080/actuator/health/readiness > /dev/null; then
    echo "Deployment ready. Database backup: $backup"
    exit 0
  fi
  sleep 2
done
echo "Readiness failed. Inspect auth logs; database backup: $backup" >&2
"${compose[@]}" logs --tail=80 auth
exit 1
