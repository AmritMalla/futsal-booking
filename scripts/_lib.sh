#!/usr/bin/env bash
# shellcheck shell=bash
# Shared helpers for futsal deploy scripts. Source this from other scripts:
#   # shellcheck source=scripts/_lib.sh
#   . "$(dirname "$0")/_lib.sh"

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export REPO_ROOT

_start_ts=$(date +%s)

_elapsed() {
  local now
  now=$(date +%s)
  printf '%02d:%02d' $(( (now - _start_ts) / 60 )) $(( (now - _start_ts) % 60 ))
}

log()   { printf '[%s] %s\n' "$(_elapsed)" "$*"; }
warn()  { printf '[%s] WARN  %s\n' "$(_elapsed)" "$*" >&2; }
fail()  { printf '[%s] ERROR %s\n' "$(_elapsed)" "$*" >&2; exit 1; }

require_cmd() {
  for cmd in "$@"; do
    command -v "$cmd" >/dev/null 2>&1 || fail "required command not found: $cmd"
  done
}

# tfout <output_name>  — read a terraform output from infra/terraform/.
tfout() {
  (cd "$REPO_ROOT/infra/terraform" && terraform output -raw "$1")
}

# Wait for a kubectl condition with a timeout (seconds).
# wait_for <description> <timeout> <cmd...>
wait_for() {
  local desc="$1" timeout="$2"
  shift 2
  local deadline=$(( $(date +%s) + timeout ))
  until "$@" >/dev/null 2>&1; do
    if [ "$(date +%s)" -ge "$deadline" ]; then
      fail "timed out after ${timeout}s waiting for: $desc"
    fi
    sleep 5
  done
}
