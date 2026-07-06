# graphqlcodegen-maven-plugin

![code coverage](badges/jacoco.svg) [full coverage report](https://deweyjose.github.io/graphqlcodegen/index.html)

A Maven plugin for [Netflix DGS codegen](https://github.com/Netflix/dgs-codegen) — a port of the
official Gradle plugin. It generates Java (or Kotlin) types, example data fetchers, and typed
client APIs from your GraphQL schema during the build.

- **Group/artifact:** `io.github.deweyjose:graphqlcodegen-maven-plugin` ([Maven Central](https://central.sonatype.com/artifact/io.github.deweyjose/graphqlcodegen-maven-plugin))
- **Goal:** `generate` (prefix `graphqlcodegen`), bound to the `generate-sources` phase by default
- **Requires:** Java 17+

## Quick start

Add the plugin to your pom's `<build><plugins>` section:

```xml
<plugin>
  <groupId>io.github.deweyjose</groupId>
  <artifactId>graphqlcodegen-maven-plugin</artifactId>
  <version>3.8.0</version>
  <executions>
    <execution>
      <goals>
        <goal>generate</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <schemaPaths>
      <param>src/main/resources/schema/schema.graphqls</param>
    </schemaPaths>
    <packageName>com.acme.myproject.generated</packageName>
  </configuration>
</plugin>
```

If no `schemaPaths` is configured, the plugin looks in `src/main/resources/schema` for files with
a `.graphqls`, `.graphql`, or `.gqls` extension.

### Generated output

Generated types land under `${project.build.directory}/generated-sources` in the
`<packageName>.types` package (client APIs under `.client`, and so on). The output directory is
**automatically added to your project's compile source roots** — no build-helper plugin needed
(disable with [`autoAddSource`](#autoaddsource)). Example data fetchers are written to
`${project.build.directory}/generated-examples`; these are *not* added to your sources and serve
as boilerplate to copy and customize.

### Schema sources

The plugin can pull schemas from four places, in any combination:

- [`schemaPaths`](#schemapaths) — local files or directories (the default)
- [`schemaJarFilesFromDependencies`](#schemajarfilesfromdependencies) — schemas embedded in dependency jars
- [`schemaUrls`](#schemaurls) — remote schema files fetched over HTTP at build time
- [`introspectionRequests`](#introspectionrequests) — live GraphQL introspection at build time

## Example project

A complete, multi-module example (server, jar-embedded schemas, type mappings, client API
generation, live introspection) is vendored in this repository under
[`examples/graphqlcodegen-example`](examples/graphqlcodegen-example) and builds as part of the
reactor on every push.

# Options

Options are configured in the `<configuration>` element of the plugin.

## Schema inputs

### schemaPaths

A list of schema file or directory paths, relative to the project or absolute. For directory
paths, only files with the extensions `.graphql`, `.graphqls`, or `.gqls` are considered.

- Type: array of paths
- Required: false
- Default: `${project.basedir}/src/main/resources/schema`

```xml
<schemaPaths>
  <param>src/main/resources/schema/schema.graphqls</param>
  <param>src/main/resources/someDirWithSchemas</param>
</schemaPaths>
```

### schemaJarFilesFromDependencies

Generate from schemas packaged inside dependency jars, referenced by `groupId:artifactId:version`
coordinates. The `.graphql(s)` files must live under the `META-INF` folder of the jar. See the
[official DGS docs](https://netflix.github.io/dgs/generating-code-from-schema/#generating-code-from-external-schemas-in-jars).

- Type: array
- Required: false
- Default: `[]`

```xml
<schemaJarFilesFromDependencies>
  <param>com.example:shared-schemas:1.0.0</param>
</schemaJarFilesFromDependencies>
```

### schemaUrls

Remote schema files to download (HTTP GET) at build time and include as codegen inputs. Downloads
are stored deterministically under `<outputDir>/remote-schemas/` so incremental builds behave
predictably.

- Type: array
- Required: false
- Default: `[]`

```xml
<schemaUrls>
  <param>https://raw.githubusercontent.com/acme/schemas/main/schema.graphqls</param>
</schemaUrls>
```

### introspectionRequests

> **Experimental:** this feature may change in future releases.

Generate code from a schema fetched via GraphQL introspection (HTTP POST) at build time. Each
`introspectionRequest` supports:

- `url` (string, required): the GraphQL endpoint URL
- `query` (string, optional): a custom introspection query; defaults to the standard full
  introspection query
- `operationName` (string, optional): defaults to `IntrospectionQuery`
- `headers` (map, optional): HTTP headers to include in the request

- Type: array of `introspectionRequest` objects
- Required: false
- Default: `[]`

```xml
<introspectionRequests>
  <introspectionRequest>
    <url>https://your-graphql-endpoint/graphql</url>
    <headers>
      <Authorization>Bearer ${env.YOUR_TOKEN}</Authorization>
      <Content-Type>application/json</Content-Type>
    </headers>
  </introspectionRequest>
</introspectionRequests>
```

## Core options

### packageName

The base package for generated code. Sub-packages are appended per kind of generated class (see
the `subPackageName*` options).

- Type: string
- Required: true

```xml
<packageName>com.acme.generated</packageName>
```

### language

`java` or `kotlin`.

- Type: string
- Required: false
- Default: `java`

```xml
<language>kotlin</language>
```

### skip

Skip code generation entirely. Also settable from the command line via
`-Ddgs.codegen.skip=true`.

- Type: boolean
- Required: false
- Default: `false`

```xml
<skip>true</skip>
```

### onlyGenerateChanged

Only regenerate when schema files have changed since the last build (tracked via a manifest of
schema hashes — see [`schemaManifestOutputDir`](#schemamanifestoutputdir)). Change detection
applies to `schemaPaths` only; jar, URL, and introspection schemas are always regenerated.

- Type: boolean
- Required: false
- Default: `true`

```xml
<onlyGenerateChanged>true</onlyGenerateChanged>
```

### typeMapping

Map GraphQL types to existing Java/Kotlin classes instead of generating them.

- Type: map
- Required: false

```xml
<typeMapping>
  <Date>java.time.LocalDateTime</Date>
</typeMapping>
```

### typeMappingPropertiesFiles

One or more type-mapping properties files loaded from the **compile classpath of dependency
jars**. Entries are merged into `typeMapping`; explicit `<typeMapping>` entries win on conflict.

- Type: array
- Required: false

```xml
<typeMappingPropertiesFiles>
  <typeMappingPropertiesFile>graphql/common-type-mappings.properties</typeMappingPropertiesFile>
</typeMappingPropertiesFiles>
```

### localTypeMappingPropertiesFiles

One or more type-mapping properties files from the **local project**, relative to the project
root. Entries are merged into `typeMapping`; explicit `<typeMapping>` entries win on conflict.

- Type: array
- Required: false

```xml
<localTypeMappingPropertiesFiles>
  <localTypeMappingPropertiesFile>src/main/resources/type-mapping.properties</localTypeMappingPropertiesFile>
</localTypeMappingPropertiesFiles>
```

## Output locations

### outputDir

Where generated sources are written.

- Type: string
- Required: false
- Default: `${project.build.directory}/generated-sources`

```xml
<outputDir>${project.build.directory}/generated-sources</outputDir>
```

### autoAddSource

Automatically add `outputDir` to the Maven compile source roots. Eliminates the need for the
build-helper-maven-plugin in most setups.

- Type: boolean
- Required: false
- Default: `true`

```xml
<autoAddSource>true</autoAddSource>
```

### examplesOutputDir

Where generated example data fetchers are written. (Not added to compile sources.)

- Type: string
- Required: false
- Default: `${project.build.directory}/generated-examples`

```xml
<examplesOutputDir>${project.build.directory}/generated-examples</examplesOutputDir>
```

### schemaManifestOutputDir

Where the schema-hash manifest for [`onlyGenerateChanged`](#onlygeneratechanged) is stored.

- Type: string
- Required: false
- Default: `${project.build.directory}/graphqlcodegen`

```xml
<schemaManifestOutputDir>${project.build.directory}/graphqlcodegen</schemaManifestOutputDir>
```

### writeToFiles

Write generated sources to disk. Disabling this effectively turns codegen into a dry run.

- Type: boolean
- Required: false
- Default: `true`

```xml
<writeToFiles>true</writeToFiles>
```

## Sub-package names

### subPackageNameClient

- Type: string
- Required: false
- Default: `client`

```xml
<subPackageNameClient>client</subPackageNameClient>
```

### subPackageNameDatafetchers

- Type: string
- Required: false
- Default: `datafetchers`

```xml
<subPackageNameDatafetchers>datafetchers</subPackageNameDatafetchers>
```

### subPackageNameTypes

- Type: string
- Required: false
- Default: `types`

```xml
<subPackageNameTypes>types</subPackageNameTypes>
```

### subPackageNameDocs

- Type: string
- Required: false
- Default: `docs`

```xml
<subPackageNameDocs>docs</subPackageNameDocs>
```

## Data types & interfaces

### generateDataTypes

Generate data types (POJOs) for schema types. Useful to disable when only generating a client
API against types that already exist.

- Type: boolean
- Required: false
- Default: `true`

```xml
<generateDataTypes>true</generateDataTypes>
```

### generateInterfaces

Generate interfaces for data classes.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateInterfaces>true</generateInterfaces>
```

### generateInterfaceSetters

Generate setters on generated interfaces.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateInterfaceSetters>true</generateInterfaceSetters>
```

### generateInterfaceMethodsForInterfaceFields

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateInterfaceMethodsForInterfaceFields>true</generateInterfaceMethodsForInterfaceFields>
```

### generateBoxedTypes

Use boxed types (e.g. `Integer` instead of `int`) for non-nullable primitive fields.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateBoxedTypes>true</generateBoxedTypes>
```

### generateIsGetterForPrimitiveBooleanFields

Generate `is`-prefixed getters for primitive boolean fields.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateIsGetterForPrimitiveBooleanFields>true</generateIsGetterForPrimitiveBooleanFields>
```

### javaGenerateAllConstructor

Generate an all-args constructor on generated Java data types.

- Type: boolean
- Required: false
- Default: `false`

```xml
<javaGenerateAllConstructor>true</javaGenerateAllConstructor>
```

### implementSerializable

Generated data types implement `java.io.Serializable`.

- Type: boolean
- Required: false
- Default: `false`

```xml
<implementSerializable>true</implementSerializable>
```

### snakeCaseConstantNames

- Type: boolean
- Required: false
- Default: `false`

```xml
<snakeCaseConstantNames>true</snakeCaseConstantNames>
```

### trackInputFieldSet

Generate `has<FieldName>()` methods that track which fields were explicitly set on input types —
useful for distinguishing "explicitly set to null" from "never set".

- Type: boolean
- Required: false
- Default: `false`

```xml
<trackInputFieldSet>true</trackInputFieldSet>
```

## Client API generation

### generateClientApi

Generate typed query/projection builder classes for use in GraphQL clients.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateClientApi>true</generateClientApi>
```

### includeQueries

Limit client API generation to the listed query fields. Used with `generateClientApi`.

- Type: array
- Required: false
- Default: `[]` (all queries)

```xml
<includeQueries>
  <param>shows</param>
  <param>theaters</param>
</includeQueries>
```

### includeMutations

Limit client API generation to the listed mutation fields. Used with `generateClientApi`.

- Type: array
- Required: false
- Default: `[]` (all mutations)

```xml
<includeMutations>
  <param>addShow</param>
</includeMutations>
```

### includeSubscriptions

Limit client API generation to the listed subscription fields. Used with `generateClientApi`.

- Type: array
- Required: false
- Default: `[]` (all subscriptions)

```xml
<includeSubscriptions>
  <param>showAdded</param>
</includeSubscriptions>
```

### skipEntityQueries

Skip generating federated `_entities` queries.

- Type: boolean
- Required: false
- Default: `false`

```xml
<skipEntityQueries>true</skipEntityQueries>
```

### shortProjectionNames

- Type: boolean
- Required: false
- Default: `false`

```xml
<shortProjectionNames>true</shortProjectionNames>
```

## Kotlin

### generateKotlinNullableClasses

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateKotlinNullableClasses>true</generateKotlinNullableClasses>
```

### generateKotlinClosureProjections

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateKotlinClosureProjections>true</generateKotlinClosureProjections>
```

### kotlinAllFieldsOptional

- Type: boolean
- Required: false
- Default: `false`

```xml
<kotlinAllFieldsOptional>true</kotlinAllFieldsOptional>
```

## Annotations

### addGeneratedAnnotation

Add a `@Generated` annotation to generated classes.

- Type: boolean
- Required: false
- Default: `false`

```xml
<addGeneratedAnnotation>true</addGeneratedAnnotation>
```

### disableDatesInGeneratedAnnotation

Omit the timestamp from the `@Generated` annotation (keeps generated code reproducible).

- Type: boolean
- Required: false
- Default: `false`

```xml
<disableDatesInGeneratedAnnotation>true</disableDatesInGeneratedAnnotation>
```

### generatedAnnotationType

Fully-qualified class name of the `@Generated` annotation to apply to generated types. When
unset, graphql-dgs-codegen-core uses its default (`<packageName>.Generated`). Added in
graphql-dgs-codegen-core 8.5.0.

- Type: string
- Required: false
- Default: (unset)

```xml
<generatedAnnotationType>javax.annotation.processing.Generated</generatedAnnotationType>
```

### addDeprecatedAnnotation

Add `@Deprecated` to generated members for schema fields marked `@deprecated`.

- Type: boolean
- Required: false
- Default: `false`

```xml
<addDeprecatedAnnotation>true</addDeprecatedAnnotation>
```

### generateJSpecifyAnnotations

Generate [JSpecify](https://jspecify.dev/) null-safety annotations in generated Java code:
classes are annotated with `@NullMarked` and nullable members with `@Nullable`.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateJSpecifyAnnotations>true</generateJSpecifyAnnotations>
```

### jacksonVersions

Select which Jackson major version(s) the generated code targets. `2` uses the
`com.fasterxml.jackson` package; `3` uses the `tools.jackson` package. When empty, the codegen
default (Jackson 2) is used. Currently only affects the Kotlin generator path.

- Type: set<string> (allowed values: `2`, `3`)
- Required: false
- Default: empty (Jackson 2)

```xml
<jacksonVersions>
  <jacksonVersion>3</jacksonVersion>
</jacksonVersions>
```

### generateCustomAnnotations

Generate custom annotations from `@annotate` directives in the schema. Used with
`includeImports`, `includeEnumImports`, and `includeClassImports`.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateCustomAnnotations>true</generateCustomAnnotations>
```

### includeImports

Maps custom annotation names to their packages. Only used when `generateCustomAnnotations` is
enabled.

- Type: map<string, string>
- Required: false

```xml
<includeImports>
  <validator>com.test.validator</validator>
</includeImports>
```

### includeEnumImports

Maps annotation enum arguments to their packages. Only used when `generateCustomAnnotations` is
enabled.

- Type: map<string, map<string, string>>
- Required: false

```xml
<includeEnumImports>
  <someAnnotation>
    <properties>
      <sortBy>com.test.enums</sortBy>
    </properties>
  </someAnnotation>
</includeEnumImports>
```

### includeClassImports

Maps annotation class-name arguments to their packages. Only used when
`generateCustomAnnotations` is enabled.

- Type: map<string, map<string, string>>
- Required: false

```xml
<includeClassImports>
  <someAnnotation>
    <properties>
      <BasicValidation>com.test.validators</BasicValidation>
    </properties>
  </someAnnotation>
</includeClassImports>
```

## Docs generation

### generateDocs

Generate schema documentation.

- Type: boolean
- Required: false
- Default: `false`

```xml
<generateDocs>true</generateDocs>
```

### generatedDocsFolder

Folder for generated documentation.

- Type: string
- Required: false
- Default: `./generated-docs`

```xml
<generatedDocsFolder>${project.build.directory}/generated-docs</generatedDocsFolder>
```

## Deprecated / no-op options

These parameters are still accepted so existing POMs keep parsing, but they no longer have any
effect because the underlying option was removed from `graphql-dgs-codegen-core`:

| Option | Status |
|--------|--------|
| `generateClientApiv2` | No-op since graphql-dgs-codegen-core 8.4.0 (v2 client API consolidated). Use [`generateClientApi`](#generateclientapi). |
| `omitNullInputFields` | No-op with graphql-dgs-codegen-core >= 8.2.1 (option removed upstream). The plugin logs a warning if set. |
| `maxProjectionDepth` | Accepted but not forwarded to codegen (option removed upstream). |

# Architecture

This project is a multi-module Maven reactor. The root `pom.xml` is a `pom`-packaging
aggregator; the published artifact lives in the `graphqlcodegen-maven-plugin` module. The example
project is vendored in and wired as Maven modules that build **by default**, so a single
`./mvnw install` runs the plugin's unit tests and the example tests together. Release stays
plugin-only (scoped with `-pl`), and Spring Boot lives only in the example modules.

```
.                                    # aggregator (pom)
├── graphqlcodegen-maven-plugin/     # the published plugin
└── examples/graphqlcodegen-example/ # end-to-end harness (built by default)
    ├── common/  server/  client/  client-introspection/
```

## graphqlcodegen-maven-plugin

The Maven plugin that users apply to their projects. It is responsible for:

- Accepting configuration via plugin parameters (the options documented above).
- Resolving schema files from the local project, dependency jars, remote URLs, and introspection.
- Invoking the DGS codegen library with the correct configuration.
- Managing incremental generation via schema-hash manifest tracking.
- Holding a checked-in `CodeGenConfigBuilder` that mirrors the upstream `CodeGenConfig`
  constructor shape.

## examples/graphqlcodegen-example

A vendored, multi-module DGS project that exercises the plugin end to end: jar-embedded schemas,
remote/introspection schemas, type mappings, and client-API generation. It builds **by default**
(plugin first in reactor order, so the examples use the just-built plugin) and is validated on
every push by the **E2E Example** GitHub Actions workflow. The `client-introspection` module
starts its own DGS server for live introspection, so no externally-running server is needed.
Spring Boot and the DGS framework live entirely in these modules and never enter the plugin's
own build (a `maven-enforcer` rule bans Spring Boot from the plugin).

# Contributing

### GitHub issues

Feel free to create a GitHub issue for requests to integrate with newer
[releases](https://github.com/Netflix/dgs-codegen/releases) of the core DGS codegen library.

### PRs

PRs are welcome. Typically, new plugin options are needed when the
[CodeGenConfig](https://github.com/Netflix/dgs-codegen/blob/master/graphql-dgs-codegen-core/src/main/kotlin/com/netflix/graphql/dgs/codegen/CodeGen.kt)
constructor in the core library changes. When constructor parameters change upstream:

- Update `CodeGenConfigBuilder` to match the latest constructor shape and ordering.
- Wire new options through `Codegen.java`, `CodegenConfigProvider.java`, and
  `CodegenExecutor.java` (all under
  `graphqlcodegen-maven-plugin/src/main/java/io/github/deweyjose/graphqlcodegen`).
- Add/update tests and document the option in this README.

See [AGENTS.md](AGENTS.md) and
[graphqlcodegen-maven-plugin/AGENTS.md](graphqlcodegen-maven-plugin/AGENTS.md) for a detailed
walkthrough of the codebase, conventions, and the option-wiring checklist.

Release checklist:

1. Bump the version in both [`graphqlcodegen-maven-plugin/pom.xml`](graphqlcodegen-maven-plugin/pom.xml)
   and the root aggregator [`pom.xml`](pom.xml), and the example's
   `graphql-codegen-plugin.version` property (keep all three in sync).
2. Run `./mvnw spotless:apply install` locally — this builds and tests the plugin **and** the
   example modules.
3. Publish a GitHub Release with tag `graphqlcodegen-maven-plugin-<version>`; the release event
   triggers the publish workflow, which deploys to Maven Central.

## Testing with the example project

The example project lives in `examples/graphqlcodegen-example` and builds by default as part of
the reactor, against the plugin you just built. A single command runs the plugin unit tests and
the example tests (including the `client-introspection` module, which starts its own DGS server):

```bash
./mvnw -B -ntp install
```

`install` (not `verify`) ensures the plugin is installed first so the examples resolve it. The
`server` module fetches its schema over HTTP from `main` by default; to build fully offline,
serve the in-repo schema and override the URL:

```bash
python3 -m http.server 8000 \
  --directory examples/graphqlcodegen-example/server/src/main/resources/schema &
./mvnw -B -ntp install -Dcodegen.server.schemaUrl=http://localhost:8000/main.graphqls
```

The **E2E Example** CI workflow runs exactly this on every push (see
[`.github/workflows/e2e-example.yaml`](.github/workflows/e2e-example.yaml)).
