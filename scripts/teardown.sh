#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

log "teardown: helm uninstall..."
helm uninstall futsal -n futsal --ignore-not-found=true || true
helm uninstall platform -n platform --ignore-not-found=true || true

log "teardown: waiting for LoadBalancer deletion (up to 2 min)..."
for _ in {1..24}; do
  if ! kubectl -n platform get svc platform-ingress-nginx-controller >/dev/null 2>&1; then
    break
  fi
  sleep 5
done

log "teardown: terraform destroy..."
(cd "$REPO_ROOT/infra/terraform" && terraform destroy -auto-approve)

log "teardown: done. (sandbox auto-cleanup will handle any leftovers.)"
