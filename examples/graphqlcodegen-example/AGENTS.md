# AGENTS.md

Guidance for AI agents making changes in this repository. Read this before editing — it
encodes the architecture, the conventions, and the gotchas that are easy to miss.

## What this project is

A **demo/reference project** showing how to consume the
[`io.github.deweyjose:graphqlcodegen-maven-plugin`](https://github.com/deweyjose/graphqlcodegen)
(which wraps Netflix DGS codegen) in a real multi-module Maven build. It is *not* the plugin
itself — it exists to exercise and document the plugin's configuration surface end to end:
schema files, jar-embedded schemas, remote schema URLs, GraphQL introspection, type mappings,
and client-API generation.

- Multi-module Maven build. Parent is Spring Boot (`spring-boot-starter-parent`). Java **17**
  (`.tool-versions` pins `temurin-17.0.7+7`).
- Modules: **`common`**, **`server`**, **`client`**, **`client-introspection`** (declared in
  root `pom.xml`).
- The plugin version is centralized in the root `pom.xml` property
  `graphql-codegen-plugin.version`. Currently **3.7.3** (latest — check
  `gh release list -R deweyjose/graphqlcodegen` before bumping).

## Build & run

Use the system `mvn` — this repo has no Maven wrapper.

```bash
mvn -B install                 # build all modules; runs codegen + tests
mvn -B -pl server test         # tests for a single module
```

Codegen is bound to `generate-sources` and runs automatically during the build. Generated
sources land in `target/generated-sources` and are **not** checked in.

To see it in action (the `client-introspection` build needs the server running):

```bash
cd server && mvn spring-boot:run          # starts GraphQL server on http://localhost:8080/graphql
cd client && mvn spring-boot:run          # queries the server, prints shows/theaters
```

## Module architecture — what each one demonstrates

1. **`common`** — shared schema + hand-written domain type. Its `.graphqls` files live under
   `src/main/resources/META-INF/schema/` so they ship *inside the jar*; downstream modules pull
   them via `<schemaJarFilesFromDependencies>`. Also holds `Show.java` (a hand-written type the
   generated code maps onto) and `graphql/common-type-mappings.properties` (a classpath
   type-mapping file consumed via `<typeMappingPropertiesFiles>`).
2. **`server`** — DGS Spring Boot GraphQL server. Demonstrates the **widest plugin config**:
   local `<schemaPaths>`, a remote `<schemaUrls>`, jar-embedded schemas, and both local
   (`<localTypeMappingPropertiesFiles>`) and classpath (`<typeMappingPropertiesFiles>`) type
   mappings. Datafetchers under `datafetchers/` resolve the schema; services under `services/`
   back them. Generated types use package `com.acme`.
3. **`client`** — DGS client. Demonstrates **client-API generation** (`<generateClientApi>`,
   `<includeQueries>`) plus inline `<typeMapping>`. `Main.java` builds typed queries with the
   generated `*GraphQLQuery` / `*ProjectionRoot` classes (package `com.acme`).
4. **`client-introspection`** — same as `client`, but the schema comes from **live GraphQL
   introspection** (`<introspectionRequests>` against `http://localhost:8080/graphql`).
   Generated types use package `com.acme.introspection`. **This module's build requires the
   server to be running** — that's the intended demonstration, not a bug.

## How the plugin is wired

The plugin is declared once in the root `pom.xml` `<pluginManagement>` (binding the `generate`
goal + global options like `addGeneratedAnnotation`), then each module adds its own `<plugin>`
block under `<build><plugins>` with module-specific `<configuration>`. To change generation
behavior, edit the relevant module's `<configuration>` — the four modules deliberately use
different option combinations, so don't assume a change in one applies to the others.

## The most common task: showcasing a plugin option

This repo's purpose is to be a living example. When the plugin gains a feature worth
demonstrating:

1. Bump `graphql-codegen-plugin.version` in the root `pom.xml` to a version that has it.
2. Add the `<configuration>` option to whichever module best illustrates it.
3. Add schema/fixtures under that module's `src/main/resources/schema/` (or
   `common`'s `META-INF/schema/` for jar-shared schemas) if the option needs them.
4. Reference the new generated code from `Main.java` / datafetchers so the build actually
   compiles against it — that proves the option works, which is the whole point.
5. `mvn -B install` must be green (build the full reactor, not just one module).

## Conventions & gotchas

- **Schema location matters.** `common`'s schemas must stay under `META-INF/schema/` to be
  discoverable both by the server at runtime
  (`dgs.graphql.schema-locations=classpath*:**/*.graphqls` in `application.yaml`) and by
  downstream `<schemaJarFilesFromDependencies>` codegen.
- **Generated package names are intentional**: `com.acme` (client/server),
  `com.acme.introspection` (introspection). `Main.java` imports from these — renaming a
  `<packageName>` breaks the imports.
- **`client-introspection` will fail to build with the server down.** Start the server first.
- **Generated sources are not committed** and live in `target/`; never edit generated files —
  change the schema or the plugin config instead.
- **Hand-written vs generated types**: `common/Show.java` is hand-written and wired in via type
  mappings so the generated code reuses it. Don't let codegen regenerate a competing `Show`.
- The `client` schema includes a `doNotCodeGen` query intentionally excluded via
  `<includeQueries>` — it demonstrates query filtering; leave it as a negative example.

## When asked to "update to the latest plugin"

The plugin lives at `deweyjose/graphqlcodegen`. Get the real latest from
`gh release list -R deweyjose/graphqlcodegen` (the Maven Central search API lags). Update only
the `graphql-codegen-plugin.version` property in the root `pom.xml`, then run `mvn -B install`
to confirm the new version still generates and compiles across all modules.
