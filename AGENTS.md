# AGENTS.md

Guidance for AI agents making changes in this repository. Read this before editing — it
encodes the architecture, the conventions CI enforces, and the gotchas that are easy to miss.

## What this project is

A **Maven plugin** (`io.github.deweyjose:graphqlcodegen-maven-plugin`) that wraps Netflix
DGS codegen (`graphql-dgs-codegen-core`) so projects can generate GraphQL Java/Kotlin types
and clients from `.graphqls` schemas during the build.

- **Multi-module Maven reactor.** Root `pom.xml` is a `pom`-packaging **aggregator** with two
  default modules: the plugin (**`graphqlcodegen-maven-plugin/`**, the published artifact) and the
  vendored example harness (**`examples/graphqlcodegen-example`**). Both build by default, so
  `./mvnw install` runs the plugin's unit tests AND the example tests. Spring Boot lives only in
  the example modules — the plugin module stays Spring-Boot-free (enforced by maven-enforcer) and
  release is scoped to the plugin module. Java **17**.
- The plugin exposes one goal: **`generate`** (`@Mojo(name = "generate")`), bound by default
  to the `generate-sources` phase.
- Consumers configure it entirely through Maven `<configuration>` parameters.

## Build, test, format

Always use the Maven wrapper (`./mvnw`), never a system `mvn` — it pins the build version.

```bash
./mvnw -B -ntp install       # WHOLE reactor: plugin unit tests + example tests (the default)
./mvnw -B -ntp verify -pl graphqlcodegen-maven-plugin   # plugin only, fast (what Java CI runs)
./mvnw test -pl graphqlcodegen-maven-plugin             # plugin unit tests only
./mvnw test -pl graphqlcodegen-maven-plugin -Dtest=SchemaTransformationServiceTest   # single class
./mvnw spotless:apply -pl graphqlcodegen-maven-plugin   # auto-format; ALWAYS run before committing
```

### The example tests run by default — keep them green

The vendored example harness under `examples/graphqlcodegen-example` compiles generated code and
runs a DGS runtime test (`ShowsDatafetcherTest`) against your **just-built** plugin. It builds as
part of the default reactor, so **`./mvnw install` runs it automatically** — adding more example
tests needs no special flag. Key facts:

- **Use `install`, not `verify`/`test`.** `install` builds + installs the plugin first in reactor
  order, so the examples resolve *your* plugin (not the released one); `schemaJarFilesFromDependencies`
  also needs `common` packaged as a jar (the `package` phase), which `test` doesn't reach.
- **`client-introspection` self-starts the DGS server** (via the Spring Boot plugin's `start`/`stop`
  goals) and generates from live introspection — no externally-running server required.
- **The `server` module fetches its `schemaUrl` over the network** (`raw.githubusercontent…`) by
  default. Online it just works; to build fully offline, serve it locally and override:
  ```bash
  python3 -m http.server 8000 --directory examples/graphqlcodegen-example/server/src/main/resources/schema &
  ./mvnw -B -ntp install -Dcodegen.server.schemaUrl=http://localhost:8000/main.graphqls
  ```

**Definition of done for any change:** `./mvnw -B -ntp install` is green (plugin **and** example
tests) *and* `spotless:apply` has been run (otherwise `spotless:check` fails CI). google-java-format
reformats aggressively, including test code — formatting-only diffs after edits are normal, so apply
and re-stage.

## Architecture — request flow

The Mojo collects config, hands it to an executor, which builds a DGS config and runs codegen:

1. **`Codegen.java`** — the `@Mojo`. Every user-facing option is an `@Parameter` field here
   (~50 of them). Implements `CodegenConfigProvider` (it *is* the request object).
2. **`CodegenConfigProvider.java`** — interface of getters the executor reads. `Codegen` uses
   Lombok `@Getter` to satisfy it.
3. **`CodegenExecutor.java`** — `execute(...)` orchestrates: resolves schema files, applies
   transformations/type mappings, and forwards every option to `CodeGenConfigBuilder`.
4. **`CodeGenConfigBuilder.java`** — a **checked-in** builder mirroring upstream DGS
   `CodeGenConfig`. This is the seam between our plugin and DGS. See gotcha below.
5. **`services/`** — focused helpers, each independently unit-tested:
   - `SchemaFileService` — expands/resolves schema paths and jar-embedded schemas.
   - `RemoteSchemaService` — fetches remote schemas, including GraphQL introspection.
   - `SchemaTransformationService` — normalizes schemas (e.g. root-type renaming, custom scalars).
   - `SchemaManifestService` — tracks schema hashes for `onlyGenerateChanged` incremental builds.
   - `TypeMappingService` — merges type-mapping properties (local files + classpath).
6. **`parameters/`** — structured `@Parameter` value types (`IntrospectionRequest`, `ParameterMap`).
7. **`Logger` / `MavenLogger`** — logging abstraction so services log without a hard Maven dependency.

## The most common task: exposing a new DGS codegen option

A DGS option existing in upstream `CodeGenConfig` is **not enough** — it must be wired through
all layers or it is silently unreachable from Maven. To add one:

1. Confirm the option exists in the pinned `graphql-dgs-codegen-core.version` (root `pom.xml`,
   property `graphql-dgs-codegen-core.version`). Check upstream release notes before bumping —
   often the fix is wiring, not a version bump.
2. Add an `@Parameter` field (with default) in **`Codegen.java`**.
3. Add the getter to **`CodegenConfigProvider.java`**.
4. Forward it to the builder in **`CodegenExecutor.java`**.
5. Ensure **`CodeGenConfigBuilder.java`** sets it on the DGS config.
6. Add/extend tests in `CodegenExecutorTest` and any fixtures under `src/test/resources/schema`.
7. Document the option in `README.md` (consumers rely on it as the option reference).

## Conventions & gotchas

- **`CodeGenConfigBuilder` is checked in.** When bumping `graphql-dgs-codegen-core.version`,
  verify its constructor args and setter surface still match upstream `CodeGenConfig`, then wire
  any new options through the layers above.
- **Primitive `boolean` options hide gaps.** A missing setter call usually does *not* fail
  compilation because the field defaults to `false`, so a feature can look wired but be inert.
  Add a test that asserts the option actually takes effect.
- **Mojo is `threadSafe = true`.** Do not introduce shared mutable static state in the request path.
- **Lombok** is used (`@Getter`, etc.); annotation processing is configured — don't hand-write
  generated accessors.
- Prefer **name-based test assertions** over hardcoded counts (e.g. assert a fixture name is
  present, not that there are exactly N fixtures) — brittle counts break when fixtures are added.

## CI

- **`Java CI` (`build.yaml`) triggers on `push` only — not `pull_request`.** Consequence:
  **PRs from forks get no automatic build/spotless check.** Validate fork PRs locally
  (`./mvnw -B -ntp install`, which covers plugin + examples) before merging; do not assume green
  checks mean it was tested.
- `build.yaml` runs `verify -pl graphqlcodegen-maven-plugin` and a separate `spotless:check` (also
  plugin-scoped) — fast, plugin-only; unformatted code fails the build.
- `coverage.yml` (`Java CI with JaCoCo`) runs on push/PR to `main` and publishes the badge. It
  reports **merged** coverage of the plugin's classes: unit tests (`jacoco.exec`) **plus** the
  `Codegen` Mojo executing during the example reactor build (`jacoco-it.exec`, captured by a JaCoCo
  agent on the Maven JVM via `MAVEN_OPTS`). The two are combined with `jacoco:merge@merge-all` +
  `jacoco:report@report-merged` (executions in the plugin pom). To reproduce locally:
  ```bash
  AGENT=$(find ~/.m2 -name 'org.jacoco.agent-*-runtime.jar' | sort | tail -1)
  python3 -m http.server 8000 --directory examples/graphqlcodegen-example/server/src/main/resources/schema &
  MAVEN_OPTS="-javaagent:$AGENT=destfile=$PWD/graphqlcodegen-maven-plugin/target/jacoco-it.exec,append=true,includes=io.github.deweyjose.graphqlcodegen.*" \
    ./mvnw -B -ntp clean install -Dcodegen.server.schemaUrl=http://localhost:8000/main.graphqls
  ./mvnw -pl graphqlcodegen-maven-plugin \
    org.jacoco:jacoco-maven-plugin:0.8.14:merge@merge-all \
    org.jacoco:jacoco-maven-plugin:0.8.14:report@report-merged
  # open graphqlcodegen-maven-plugin/target/site/jacoco/index.html
  ```
- **`E2E Example` (`e2e-example.yaml`) runs on push.** It runs the whole reactor (`./mvnw install`)
  against the plugin built from the current commit, proving the example generates, compiles, and
  runs end-to-end (see below).

## End-to-end example harness

`examples/graphqlcodegen-example` is a **vendored copy** (plain tracked files) of the
[`deweyjose/graphqlcodegen-example`](https://github.com/deweyjose/graphqlcodegen-example) project,
wired into the reactor as **default** Maven modules. The plugin builds first in reactor order, so a
single `./mvnw install` builds all four example modules (`common` → `server` → `client` →
`client-introspection`) against the just-built plugin. This validates real Maven-reactor behavior
the mocked unit tests cannot, and the `E2E Example` workflow runs exactly this on push.

- **Examples build by default; release stays plugin-only.** Spring Boot lives only in the example
  modules. The plugin module is a separate artifact, and `publish.yaml` deploys `-pl
  graphqlcodegen-maven-plugin`, so the examples never ship to Maven Central.
- **`client-introspection` self-starts the DGS server.** Its pom uses the `spring-boot-maven-plugin`
  `start`/`stop` goals (bound around codegen) to run the `server` app, so it generates from live
  introspection with no externally-running server. Don't remove those executions or the `server`
  dependency.
- **Dependency isolation is enforced.** The plugin must never depend on Spring Boot. The
  `maven-enforcer` `ban-spring-boot` rule (runs during the plugin module build) fails if it leaks
  in. The DGS framework core (`com.netflix.graphql.dgs:graphql-dgs`) is a legitimate transitive of
  `graphql-dgs-codegen-core` and is intentionally allowed.
- **The `server` module fetches `codegen.server.schemaUrl` over HTTP** (network by default). CI (and
  offline local builds) override it to a locally-served copy:
  `-Dcodegen.server.schemaUrl=http://localhost:8000/...`. `RemoteSchemaService` is HTTP-only, so a
  `file:` URL won't work.
- **Keeping it current:** the files under `examples/graphqlcodegen-example/` are the source of truth.
  Edit them here directly when the plugin gains a feature worth exercising, and keep the example's
  `graphql-codegen-plugin.version` property in sync with the plugin version.

## Release process

Releasing publishes to **Maven Central** and is **irreversible** — only release green `main`.

1. Bump the version in **both** `graphqlcodegen-maven-plugin/pom.xml` (the published artifact) and
   the root aggregator `pom.xml` → keep them in sync.
2. `./mvnw spotless:apply`, commit, push to `main`, and wait for `main` CI to pass.
3. Publish a **GitHub Release** with tag `graphqlcodegen-maven-plugin-<version>` targeting `main`.
   The `release: published` event triggers `publish.yaml`, which deploys to Maven Central via
   Sonatype OSS. (Auto-generated release notes match prior releases.)
