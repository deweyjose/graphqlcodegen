# AGENTS.md — graphqlcodegen-maven-plugin (the published plugin)

Module-level guidance for the plugin itself. Read the repo-root `AGENTS.md` first for
reactor layout, build commands, CI, and release; this file covers the code in this module.

## Layout

```
src/main/java/io/github/deweyjose/graphqlcodegen/
├── Codegen.java                 # the @Mojo — every user-facing option is an @Parameter here
├── CodegenConfigProvider.java   # getter interface the executor reads (Codegen implements it)
├── CodegenExecutor.java         # orchestrates schema resolution + forwards options to the builder
├── CodeGenConfigBuilder.java    # checked-in builder mirroring upstream DGS CodeGenConfig
├── Logger.java / MavenLogger.java  # logging abstraction (services don't depend on Maven APIs)
├── parameters/                  # structured @Parameter value types
│   ├── IntrospectionRequest.java   # url/query/operationName/headers
│   └── ParameterMap.java           # nested map support for includeEnumImports/includeClassImports
└── services/                    # focused helpers, each independently unit-tested
    ├── SchemaFileService.java          # expands/resolves schema paths + jar-embedded schemas
    ├── RemoteSchemaService.java        # HTTP GET schema files; HTTP POST introspection → SDL
    ├── SchemaTransformationService.java # schema normalization (root-type renaming, scalars)
    ├── SchemaManifestService.java      # schema hashes for onlyGenerateChanged incremental builds
    ├── TypeMappingService.java         # merges typeMapping (inline > classpath > local files)
    └── Constants.java                  # default introspection query/operation name
```

## Request flow

`Codegen.execute()` → builds services → `CodegenExecutor.execute(this, artifacts, basedir)`:

1. Expand `schemaPaths` (directories → `.graphql`/`.graphqls`/`.gqls` files).
2. Resolve jar-embedded schemas (`META-INF/**` in dependency jars), remote `schemaUrls`
   (saved under `<outputDir>/remote-schemas/<base64(url)>.graphqls` for deterministic
   incremental behavior), and `introspectionRequests` (introspection JSON → SDL via
   graphql-java).
3. If `onlyGenerateChanged`, filter to files whose hash changed vs. the manifest
   (applies to `schemaPaths` only); short-circuit if nothing to do.
4. Merge type mappings (`typeMapping` wins over properties files).
5. Build `CodeGenConfig` via `CodeGenConfigBuilder`, run DGS `CodeGen.generate()`,
   then sync the manifest.
6. Back in the Mojo: if `autoAddSource`, add `outputDir` as a compile source root.

## Checklist: exposing a new DGS codegen option

An option existing in upstream `CodeGenConfig` is **not enough** — it must be wired through
all layers or it is silently unreachable from Maven:

1. Confirm the option exists in the pinned `graphql-dgs-codegen-core.version` (property in
   this module's `pom.xml`). Check upstream release notes before bumping — often the fix is
   wiring, not a version bump.
2. Add an `@Parameter` field (with default) in `Codegen.java`.
3. Add the getter to `CodegenConfigProvider.java` (and to the test double
   `src/test/java/.../TestCodegenProvider.java`).
4. Forward it to the builder in `CodegenExecutor.java`.
5. Ensure `CodeGenConfigBuilder.java` sets it on the DGS config — its `build()` call must
   match the upstream constructor's **exact argument order**.
6. Add/extend tests in `CodegenExecutorTest` and fixtures under `src/test/resources/schema`.
7. Document the option in the root `README.md` — it is the consumer-facing option reference,
   and its stated **types and defaults must match the `@Parameter` declarations** (they have
   drifted before).

## Conventions & gotchas

- **`CodeGenConfigBuilder` is checked in, not generated.** When bumping
  `graphql-dgs-codegen-core.version`, diff its fields and `build()` argument list against the
  upstream `CodeGenConfig` constructor.
- **Primitive `boolean` options hide gaps.** A missing setter call does not fail compilation —
  the field just stays `false` — so a feature can look wired but be inert. Add a test that
  asserts the option actually takes effect.
- **Known intentional no-ops** (kept for POM compatibility; do not "fix" them by wiring them
  up without checking upstream first): `generateClientApiv2` (removed upstream in 8.4.0),
  `omitNullInputFields` (removed upstream in 8.2.1; executor logs a warning),
  `maxProjectionDepth` (parameter + getter exist but it is not forwarded — removed upstream).
- **Mojo is `threadSafe = true`.** No shared mutable static state in the request path.
- **Lombok** (`@Getter`, `@SneakyThrows`, …) is used; don't hand-write accessors it generates.
- **Services must not depend on Maven APIs** — they take the `Logger` abstraction, which is
  what keeps them unit-testable without a Maven harness. Keep it that way.
- **Spring Boot is banned** from this module's classpath (maven-enforcer `ban-spring-boot`).
  The DGS framework core (`com.netflix.graphql.dgs:graphql-dgs`) is a legitimate transitive
  of `graphql-dgs-codegen-core` and is intentionally allowed.
- Prefer **name-based test assertions** over hardcoded counts (assert a fixture name is
  present, not that there are exactly N fixtures).

## Build & test (from the repo root)

```bash
./mvnw -B -ntp verify -pl graphqlcodegen-maven-plugin    # this module only (what Java CI runs)
./mvnw test -pl graphqlcodegen-maven-plugin -Dtest=CodegenExecutorTest   # single class
./mvnw spotless:apply -pl graphqlcodegen-maven-plugin    # ALWAYS before committing
./mvnw -B -ntp install                                   # full reactor incl. example e2e — the definition of done
```

Test resources: `src/test/resources/schema` (unit fixtures), `src/test/resources/introspection`
(canned introspection query/response for `RemoteSchemaServiceTest`).
