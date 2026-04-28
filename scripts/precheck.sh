#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

log "precheck: verifying tools..."
require_cmd aws terraform kubectl helm skopeo jq gh

log "precheck: verifying AWS caller identity..."
aws sts get-caller-identity >/dev/null || fail "aws sts get-caller-identity failed — are sandbox credentials active?"

EXPECTED_REGION="${AWS_REGION:-us-east-1}"
CURRENT_REGION="$(aws configure get region 2>/dev/null || echo "$EXPECTED_REGION")"
[ "$CURRENT_REGION" = "$EXPECTED_REGION" ] || warn "AWS region is '$CURRENT_REGION', expected '$EXPECTED_REGION'. Continuing anyway."

log "precheck: verifying GHCR images exist for main..."
GHCR_USER="${GHCR_USER:?GHCR_USER env var not set (export the GitHub owner/user)}"
SHA="${SHA:-$(gh api "repos/{owner}/{repo}/commits/master" --jq .sha)}"
export SHA
for image in "ghcr.io/${GHCR_USER}/futsal-backend:${SHA}" "ghcr.io/${GHCR_USER}/futsal-frontend:${SHA}"; do
  docker manifest inspect "$image" >/dev/null 2>&1 \
    || skopeo inspect "docker://$image" >/dev/null 2>&1 \
    || fail "image not found: $image — check CI build status"
done

log "precheck: ok. target SHA: $SHA"
