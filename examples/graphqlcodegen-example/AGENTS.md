# AGENTS.md — examples/graphqlcodegen-example (vendored e2e harness)

Guidance for AI agents editing the example harness. Read the repo-root `AGENTS.md` first for
reactor layout, build commands, and CI; this file covers what each example module demonstrates
and how to extend it.

## What this is

A **vendored copy** of the [`deweyjose/graphqlcodegen-example`](https://github.com/deweyjose/graphqlcodegen-example)
demo project, wired into this repo's Maven reactor as **default modules**. It is *not* the plugin
itself — it exists to exercise and document the plugin's configuration surface end to end:
schema files, jar-embedded schemas, remote schema URLs, GraphQL introspection, type mappings,
and client-API generation. The files here are the **source of truth** — edit them directly; there
is no submodule or sync step.

- Parent pom is Spring Boot (`spring-boot-starter-parent`). Java **17**. Spring Boot must never
  leak into the plugin module (enforced there by maven-enforcer).
- Modules, in reactor order: **`common`** → **`server`** → **`client`** → **`client-introspection`**.
- The plugin version is centralized in this directory's root `pom.xml` property
  **`graphql-codegen-plugin.version`** and must stay **in sync with the in-repo plugin version**
  (root aggregator + `graphqlcodegen-maven-plugin/pom.xml`). No release lookup needed — the
  version to match lives in this repo.

## Build & run

Build from the **repo root** with the Maven wrapper — the examples resolve the *just-built*
plugin only when the whole reactor runs with `install`:

```bash
./mvnw -B -ntp install                              # plugin + all example modules (the e2e test)
./mvnw -B -ntp install -pl examples/graphqlcodegen-example/server -am   # one example module + its deps
```

The `server` module's codegen fetches `codegen.server.schemaUrl` over HTTP (defaults to the
schema on `main`). To build offline, serve the in-repo schema and override the property — see
the root `AGENTS.md`.

Codegen is bound to `generate-sources` and runs automatically. Generated sources land in each
module's `target/generated-sources` and are **not** checked in.

To run the demo interactively (from this directory):

```bash
cd server && mvn spring-boot:run     # GraphQL server on http://localhost:8080/graphql
cd client && mvn spring-boot:run     # queries the server, prints shows/theaters
```

## Module architecture — what each one demonstrates

1. **`common`** — shared schema + hand-written domain type. Its `.graphqls` files live under
   `src/main/resources/META-INF/schema/` so they ship *inside the jar*; downstream modules pull
   them via `<schemaJarFilesFromDependencies>`. Also holds `Show.java` (a hand-written type the
   generated code maps onto) and `graphql/common-type-mappings.properties` (a classpath
   type-mapping file consumed via `<typeMappingPropertiesFiles>`).
2. **`server`** — DGS Spring Boot GraphQL server. Demonstrates the **widest plugin config**:
   local `<schemaPaths>`, a remote `<schemaUrls>` (the `codegen.server.schemaUrl` property),
   jar-embedded schemas, and both local (`<localTypeMappingPropertiesFiles>`) and classpath
   (`<typeMappingPropertiesFiles>`) type mappings. Datafetchers under `datafetchers/` resolve the
   schema; services under `services/` back them. `ShowsDatafetcherTest` is the DGS runtime test
   that makes this a real e2e check. Generated types use package `com.acme`.
3. **`client`** — DGS client. Demonstrates **client-API generation** (`<generateClientApi>`,
   `<includeQueries>`), inline `<typeMapping>`, and `<autoAddSource>false</autoAddSource>` with
   the build-helper plugin instead. `Main.java` builds typed queries with the generated
   `*GraphQLQuery` / `*ProjectionRoot` classes (package `com.acme`).
4. **`client-introspection`** — same as `client`, but the schema comes from **live GraphQL
   introspection** (`<introspectionRequests>` against `http://localhost:8080/graphql`).
   **Self-contained:** its pom binds the `spring-boot-maven-plugin` `start`/`stop` goals around
   codegen, so it launches the `server` app itself during the build — no externally-running
   server needed. Don't remove those executions or the `server` dependency. Generated types use
   package `com.acme.introspection`.

## How the plugin is wired

The plugin is declared once in this directory's root `pom.xml` `<pluginManagement>` (binding the
`generate` goal + global options like `addGeneratedAnnotation`), then each module adds its own
`<plugin>` block under `<build><plugins>` with module-specific `<configuration>`. To change
generation behavior, edit the relevant module's `<configuration>` — the four modules deliberately
use different option combinations, so don't assume a change in one applies to the others.

## The most common task: showcasing a plugin option

This harness's purpose is to be a living example. When the plugin gains a feature worth
demonstrating:

1. Add the `<configuration>` option to whichever module best illustrates it.
2. Add schema/fixtures under that module's `src/main/resources/schema/` (or `common`'s
   `META-INF/schema/` for jar-shared schemas) if the option needs them.
3. Reference the new generated code from `Main.java` / datafetchers / tests so the build actually
   compiles against it — that proves the option works, which is the whole point.
4. `./mvnw -B -ntp install` from the repo root must be green (full reactor, not just one module).
5. If the release bumping the plugin version hasn't happened yet, the examples still work: the
   reactor installs the in-repo plugin at the current version first, and
   `graphql-codegen-plugin.version` points at that version.

## Conventions & gotchas

- **Schema location matters.** `common`'s schemas must stay under `META-INF/schema/` to be
  discoverable both by the server at runtime
  (`dgs.graphql.schema-locations=classpath*:**/*.graphqls` in `application.yaml`) and by
  downstream `<schemaJarFilesFromDependencies>` codegen.
- **Generated package names are intentional**: `com.acme` (client/server),
  `com.acme.introspection` (introspection). `Main.java` imports from these — renaming a
  `<packageName>` breaks the imports.
- **Generated sources are not committed** and live in `target/`; never edit generated files —
  change the schema or the plugin config instead.
- **Hand-written vs generated types**: `common/Show.java` is hand-written and wired in via type
  mappings so the generated code reuses it. Don't let codegen regenerate a competing `Show`.
- The `client` schema includes a `doNotCodeGen` query intentionally excluded via
  `<includeQueries>` — it demonstrates query filtering; leave it as a negative example.
- **`codegen.server.schemaUrl` must stay overridable.** CI points it at a locally-served copy of
  the in-repo schema; if you rename the property or hardcode the URL, the E2E and coverage
  workflows break. `RemoteSchemaService` is HTTP-only — a `file:` URL won't work.
