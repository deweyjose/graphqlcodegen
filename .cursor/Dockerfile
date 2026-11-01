# Cursor Cloud Agent base image for deweyjose/graphqlcodegen.
# Pins the toolchain agents need; does NOT COPY the repository (Cursor checks out
# the requested revision separately). Keep this lean — no IDEs, no credentials.
#
# Toolchain pins (must stay in sync with the repo / CI):
#   - JDK 17  (java.version in plugin + example poms; actions/setup-java temurin 17)
#   - Maven is NOT installed system-wide — agents always use ./mvnw, which downloads
#     Apache Maven 3.9.6 (see .mvn/wrapper/maven-wrapper.properties).

FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    LANG=C.UTF-8

# git + curl: required on fully custom images. python3: offline schemaUrl serving
# for the example harness (see AGENTS.md). unzip: Maven wrapper zip extraction.
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        git \
        openjdk-17-jdk-headless \
        python3 \
        unzip \
    && rm -rf /var/lib/apt/lists/* \
    && test -x "$JAVA_HOME/bin/java" \
    && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q 'version "17'

# ubuntu:24.04 already has an `ubuntu` user; do not recreate it.
# Put JDK 17 first on PATH for login shells (bash -lc used by install/start).
ENV PATH="${JAVA_HOME}/bin:/usr/local/bin:/usr/bin:/bin"

USER ubuntu
WORKDIR /home/ubuntu
