# AGENTS.md

Guidance for AI agents making changes in this repository. Read this before editing — it
encodes the architecture, the conventions CI enforces, and the gotchas that are easy to miss.

## What this project is

A **Maven plugin** (`io.github.deweyjose:graphqlcodegen-maven-plugin`) that wraps Netflix
DGS codegen (`graphql-dgs-codegen-core`) so projects can generate GraphQL Java/Kotlin types
and clients from `.graphqls` schemas during the build.

- Single Maven module. Build descriptor: root `pom.xml`. Java **17**.
- The plugin exposes one goal: **`generate`** (`@Mojo(name = "generate")`), bound by default
  to the `generate-sources` phase.
- Consumers configure it entirely through Maven `<configuration>` parameters.

## Build, test, format

Always use the Maven wrapper (`./mvnw`), never a system `mvn` — it pins the build version.

```bash
./mvnw -B -ntp verify        # full build + all tests + spotless:check (what CI runs)
./mvnw test                  # tests only
./mvnw test -Dtest=SchemaTransformationServiceTest          # single test class
./mvnw test -Dtest=SchemaTransformationServiceTest#methodName   # single test method
./mvnw spotless:apply        # auto-format; ALWAYS run before committing
```

**Definition of done for any change:** `./mvnw -B -ntp verify` is green *and* `spotless:apply`
has been run (otherwise `spotless:check` fails CI). google-java-format reformats aggressively,
including test code — formatting-only diffs after edits are normal, so apply and re-stage.

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
  (`./mvnw -B -ntp verify`) before merging; do not assume green checks mean it was tested.
- `build.yaml` runs `verify` and a separate `spotless:check` — unformatted code fails the build.
- `coverage.yml` (`Java CI with JaCoCo`) runs on push and PR and publishes a coverage badge on `main`.
- **`E2E Example` (`e2e-example.yaml`) runs on push.** It builds the `graphqlcodegen-example`
  project against the plugin built from the current commit, proving generated sources compile and
  run end-to-end (see below).

## End-to-end example harness

`examples/graphqlcodegen-example` is a **vendored copy** (plain tracked files) of the
[`deweyjose/graphqlcodegen-example`](https://github.com/deweyjose/graphqlcodegen-example) project.
The `E2E Example` workflow installs the plugin from source, then builds all four example modules
against it (`common` → `server` → `client` → `client-introspection`), starting the DGS server so
`client-introspection` can generate from live introspection. This validates real Maven-reactor
behavior the mocked unit tests cannot.

- **The example is never part of the plugin's Maven build.** Do **not** add it to `<modules>`; the
  plugin's root `pom.xml` is not an aggregator. The example is built only by the workflow, in a
  separate `mvn` invocation, with its own Spring Boot parent.
- **Dependency isolation is enforced.** The plugin must never depend on Spring Boot. The
  `maven-enforcer` `ban-spring-boot` rule (runs during every build, incl. `verify`) fails if it
  leaks in. The DGS framework core (`com.netflix.graphql.dgs:graphql-dgs`) is a legitimate
  transitive of `graphql-dgs-codegen-core` and is intentionally allowed.
- **CI overrides two things on the example** (no edits to the vendored files needed): the plugin
  version (`-Dgraphql-codegen-plugin.version=<built version>`) and the server's remote schema URL
  (`-Dcodegen.server.schemaUrl=http://localhost:8000/...`, served locally to avoid a network dep).
- **Keeping it current:** the files under `examples/graphqlcodegen-example/` are the source of
  truth for the e2e build. Edit them here directly when the plugin gains a feature worth exercising.

## Release process

Releasing publishes to **Maven Central** and is **irreversible** — only release green `main`.

1. Bump the version in `pom.xml` → `<version>` (the only place it lives).
2. `./mvnw spotless:apply`, commit, push to `main`, and wait for `main` CI to pass.
3. Publish a **GitHub Release** with tag `graphqlcodegen-maven-plugin-<version>` targeting `main`.
   The `release: published` event triggers `publish.yaml`, which deploys to Maven Central via
   Sonatype OSS. (Auto-generated release notes match prior releases.)
