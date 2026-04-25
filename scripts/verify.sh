#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

: "${HOST:?HOST env var required (public futsal hostname, e.g. futsal-1-2-3-4.nip.io)}"

BASE="https://$HOST"

log "verify: frontend serves HTML..."
curl -fsS "$BASE/" | grep -qi '<!doctype html>' || fail "frontend root did not return HTML"

log "verify: backend liveness..."
curl -fsS "$BASE/actuator/health/liveness" | grep -q '"status":"UP"' || fail "liveness not UP"

log "verify: backend readiness..."
curl -fsS "$BASE/actuator/health/readiness" | grep -q '"status":"UP"' || fail "readiness not UP"

log "verify: TLS chain..."
echo | openssl s_client -connect "${HOST}:443" -servername "$HOST" -verify_return_error >/dev/null 2>&1 \
  || fail "TLS verification failed for $HOST"

log "verify: public API shape (grounds list)..."
curl -fsS "$BASE/api/v1/grounds" >/dev/null || fail "grounds listing endpoint failed"

log "verify: running integration test suite against $BASE..."
(
  cd "$REPO_ROOT"
  ./mvnw -B -Dtest.base-url="$BASE" test -Dgroups=deployed || {
    warn "full integration suite failed against deployed URL. See output above."
    exit 1
  }
)

log "verify: all checks passed."
