#!/usr/bin/env bash
# Cursor Cloud Agent install — runs once per environment build and is snapshotted.
# Prefetch the full reactor's Maven dependencies (plugin + examples) into ~/.m2 so
# agents boot without a cold download. Never bake credentials; never start servers.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

HASH_DIR="${HOME}/.cache/graphqlcodegen-cloud-env"
HASH_FILE="${HASH_DIR}/pom.sha256"
mkdir -p "${HASH_DIR}"

log() { printf 'graphqlcodegen-cloud-env install: %s\n' "$*"; }

prefer_java_17_home() {
  # Prefer the image/CI JDK when a newer system JDK is also installed.
  local candidate="${JAVA_HOME:-}"
  if [[ -z "${candidate}" || ! -x "${candidate}/bin/java" ]]; then
    for candidate in \
      /usr/lib/jvm/java-17-openjdk-amd64 \
      /usr/lib/jvm/java-17-openjdk \
      /usr/lib/jvm/temurin-17-jdk-amd64; do
      if [[ -x "${candidate}/bin/java" ]]; then
        export JAVA_HOME="${candidate}"
        export PATH="${JAVA_HOME}/bin:${PATH}"
        return 0
      fi
    done
  elif [[ -x "${candidate}/bin/java" ]]; then
    export PATH="${candidate}/bin:${PATH}"
  fi
}

require_java_17() {
  prefer_java_17_home
  if ! command -v java >/dev/null 2>&1; then
    echo "error: java not on PATH; expected JDK 17 from the environment image" >&2
    exit 1
  fi
  local ver major
  ver="$(java -version 2>&1 | head -n 1 || true)"
  # Extract major from e.g. openjdk version "17.0.20.1" 2026-08-18
  major="$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n 1)"
  if [[ "${major}" != "17" ]]; then
    echo "error: expected Java 17, got: ${ver}" >&2
    echo "hint: JAVA_HOME=${JAVA_HOME:-<unset>} PATH=${PATH}" >&2
    exit 1
  fi
  log "java OK (${ver})"
}

# Stable fingerprint of every reactor POM + the Maven wrapper pin. start.sh warns
# when an agent branch drifts from the snapshot that recorded this hash.
record_pom_hash() {
  # LC_ALL=C keeps sort order stable across locales.
  local fingerprint
  fingerprint="$(
    {
      find . -name pom.xml -print
      printf '%s\n' ./.mvn/wrapper/maven-wrapper.properties
    } | LC_ALL=C sort -u | while IFS= read -r f; do
      # Skip missing optional paths without failing the hash.
      if [[ -f "${f}" ]]; then
        # Strip leading ./ so sha256sum's path column (and thus the digest) is
        # stable regardless of whether find emitted ./pom.xml or pom.xml.
        sha256sum "${f#./}"
      fi
    done | sha256sum | awk '{print $1}'
  )"
  printf '%s\n' "${fingerprint}" >"${HASH_FILE}"
  log "recorded pom hash ${fingerprint} -> ${HASH_FILE}"
}

prefetch_maven() {
  if [[ ! -x ./mvnw ]]; then
    echo "error: ./mvnw missing; always use the repo wrapper (Maven 3.9.6)" >&2
    exit 1
  fi

  # First invocation downloads the wrapper's pinned Maven distribution (3.9.6).
  log "warming Maven wrapper (./mvnw -v)"
  ./mvnw -v

  # Populate ~/.m2 for the whole reactor (plugin + examples). go-offline pulls
  # dependency + plugin artifacts; resolve-plugins covers plugin-extension gaps
  # go-offline sometimes misses on multi-module reactors.
  log "prefetching reactor dependencies (dependency:go-offline)"
  ./mvnw -B -ntp dependency:go-offline

  log "resolving plugin artifacts (dependency:resolve-plugins)"
  ./mvnw -B -ntp dependency:resolve-plugins

  log "Maven local repository warm at ${HOME}/.m2/repository"
}

require_java_17
record_pom_hash
prefetch_maven
log "done"
