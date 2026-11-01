#!/usr/bin/env bash
# Cursor Cloud Agent start — runs detached on every agent boot.
# This repo is a Maven plugin + example harness: no long-running services to
# launch. Only warn when the snapshotted pom fingerprint looks stale.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

HASH_DIR="${HOME}/.cache/graphqlcodegen-cloud-env"
HASH_FILE="${HASH_DIR}/pom.sha256"

log() { printf 'graphqlcodegen-cloud-env start: %s\n' "$*"; }

current_pom_hash() {
  {
    find . -name pom.xml -print
    printf '%s\n' ./.mvn/wrapper/maven-wrapper.properties
  } | LC_ALL=C sort -u | while IFS= read -r f; do
    if [[ -f "${f}" ]]; then
      # Strip leading ./ so sha256sum's path column (and thus the digest) is
      # stable regardless of whether find emitted ./pom.xml or pom.xml.
      sha256sum "${f#./}"
    fi
  done | sha256sum | awk '{print $1}'
}

if [[ -f "${HASH_FILE}" ]]; then
  expected="$(tr -d '[:space:]' <"${HASH_FILE}")"
  actual="$(current_pom_hash)"
  if [[ "${expected}" != "${actual}" ]]; then
    log "WARNING: pom/wrapper fingerprint changed since the environment snapshot."
    log "  snapshot=${expected}"
    log "  current =${actual}"
    log "  Dependency versions may have drifted; re-run: bash .cursor/install.sh"
  else
    log "pom hash matches snapshot (${actual})"
  fi
else
  log "WARNING: no pom hash at ${HASH_FILE} (install did not record one)."
  log "  Run: bash .cursor/install.sh"
fi

# Intentionally empty of services — agents start Maven themselves via ./mvnw.
log "no runtime services to start"
exit 0
