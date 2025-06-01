![code coverage](badges/jacoco.svg)

[View full coverage report](https://deweyjose.github.io/graphqlcodegen/index.html)

# graphqlcodegen-maven-plugin

This maven plugin is a port of the netflix codegen plugin for Gradle.
Found [here](https://github.com/Netflix/dgs-codegen).

# Architecture

This project is organized into two main Maven modules:

## 1. graphqlcodegen-maven-plugin
This is the main Maven plugin that users apply to their projects. It provides goals for generating Java (or Kotlin) code from GraphQL schemas, mirroring the functionality of the Netflix DGS Gradle codegen plugin. It is responsible for:
- Accepting configuration via plugin parameters.
- Resolving schema files from the local project and dependencies.
- Invoking the DGS codegen library with the correct configuration.
- Managing incremental code generation and manifest tracking.

## 2. graphqlcodegen-bootstrap-plugin
This is a helper plugin used only during the build of this project. Its purpose is to:
- Download the latest `CodeGenConfig.kt` from the Netflix DGS codegen repository.
- Parse the constructor parameters and their types.
- Generate a Java builder class that exactly matches the upstream config surface.

This architecture ensures that the Maven plugin's configuration is always in sync with the upstream DGS codegen library, and makes it much easier to adapt to changes in the core library.

# Contributing

### GitHub Issue

Feel free to simply create a GitHub issue for requests to integrate with
newer [releases](https://github.com/Netflix/dgs-codegen/releases) of the core DGS Codegen library.

### PRs

PRs are welcome as well. The level of difficulty across DGS Codegen updates varies. Typically, new plugin options are added when the [CodeGenConfig](https://github.com/Netflix/dgs-codegen/blob/master/graphql-dgs-codegen-core/src/main/kotlin/com/netflix/graphql/dgs/codegen/CodeGen.kt) constructor in the core library changes.

**Good news:** The process for handling constructor changes is now much simpler:

- The `graphqlcodegen-bootstrap-plugin` will automatically fetch the latest `CodeGenConfig.kt` and generate a new builder class reflecting any new or changed parameters.
- If the upstream constructor changes, you will get a compile failure in the main plugin module. The error will clearly indicate which parameters are missing or need to be updated.
- Simply update the main plugin's configuration and wiring to match the new builder and parameters. The compile error will guide you to what needs to be fixed.
- Make sure to document any new options in the `Options` section below.

Process:

1. Bump the version in [pom.xml](pom.xml)
2. Run `mvn spotless:apply clean install` locally to ensure the project still builds and is formatted
3. Adjust [CodeGen](src/main/java/io/github/deweyjose/graphqlcodegen/Codegen.java) and related classes to support new options if needed
4. **Test with the example project:**
   - Clone [graphqlcodegen-example](https://github.com/deweyjose/graphqlcodegen-example)
   - Bump the plugin version in its `pom.xml` to match your changes
   - Run `mvn spotless:apply clean install` in the example project to ensure everything works as expected

> **Note:** In the future, the example repo will be folded into this repository as a set of test modules to make testing and validation even easier.

# Overview

The DGS Code Generation plugin generates code for basic types and example data fetchers based on
your Domain Graph
Service's graphql schema file during the project's build process. The plugin requires the
designated `packageName` for file generation.
If no `schemaPath` is specified, it will look in the src/main/resources/schema folder for
any files with .graphqls, .graphql or .gqls extension.

# Example Repo

https://github.com/deweyjose/graphqlcodegen-example

# Options

Options are configured in the `<configuration>` element of the dgs-codegen-maven-plugin plugin.

## onlyGenerateChanged

This options enables the plugin to limit code generation to only schema files that have changed.
Today this only works with schemaPaths.

This only works for `<schemaPaths>`. A subsequent release for schema compilation via dependencies
will be release soon.

- Type: boolean
- Required: false
- Default: true

```xml

<onlyGenerateChanged>true</onlyGenerateChanged>
```

## subPackageNameDocs

- Type: string
- Required: false
- Default: docs

Example

```xml

<subPackageNameDocs>docs</subPackageNameDocs>
```

## generateDocs

- Type: string
- Required: false
- Default: docs

Example

```xml

<generateDocs>true</generateDocs>
```

## generatedDocsFolder

- Type: string
- Required: false
- Default: ./generated-docs

Example

```xml

<generatedDocsFolder>true</generatedDocsFolder>
```

## addGeneratedAnnotation

- Type: boolean
- Required: false
- Default: false

Example

```xml

<addGeneratedAnnotation>true</addGeneratedAnnotation>
```

## skip

- Type: boolean
- Required: false
- Default: false

Example

```xml

<dgs.codegen.skip>true</dgs.codegen.skip>
```

Or

```shell
# mvn ... -Ddgs.codegen.skip=true
```

## schemaPaths

A list of schema file or directory paths.

Directory paths: Only files with file extensions `.graphql`, `.graphqls` and `.gqls` will be considered.

Default value is `${project.basedir}/src/main/resources/schema`.

- Type: array
- Required: false
- Default: `${project.basedir}/src/main/resources/schema`

Example

```xml

<schemaPaths>
  <param>src/main/resources/schema/schema.graphqls1</param>
  <param>src/main/resources/schema/schema.graphqls2</param>
  <param>src/main/resources/someDirWithSchema</param>
</schemaPaths>
```

## schemaJarFilesFromDependencies

- Type: array
- Required: false
- Default: []
- Official
  doc : https://netflix.github.io/dgs/generating-code-from-schema/#generating-code-from-external-schemas-in-jars.
- Please note that `.graphql(s)` files must exist under the `META-INF` folder in the external jar
  file.

Example

```xml

<schemaJarFilesFromDependencies>
  <param>com.netflix.graphql.dgs:some-dependency:1.0.0</param>
  <param>com.netflix.graphql.dgs:some-dependency:X.X.X</param>
</schemaJarFilesFromDependencies>
```

## packageName

- Type: string
- Required: true

Example

```xml

<packageName>com.acme.se.generated</packageName>
```

## typeMapping

- Type: map
- Required: false

Example

```xml

<typeMapping>
  <Date>java.time.LocalDateTime</Date>
</typeMapping>
```

## typeMappingPropertiesFiles

Provide typeMapping as properties file(s) that is accessible as a compile-time-classpath resource
Key-Value pairs in the properties file will be added to `typeMapping` Map when it is not already
present in it

When a same GraphQL type is present in both `typeMapping` and also in `typeMappingPropertiesFiles`,
value in `typeMapping` will be used (and the value from `typeMappingPropertiesFiles` *will* be
ignored)

- Type: Array
- Required: false

Example

```xml

<typeMappingPropertiesFiles>
  <typeMappingPropertiesFile>commontypes-typeMapping.properties</typeMappingPropertiesFile>
  <typeMappingPropertiesFile>someother-commontypes-typeMapping.properties
  </typeMappingPropertiesFile>
</typeMappingPropertiesFiles>
```

## subPackageNameClient

- Type: string
- Required: false
- Default: client

Example

```xml

<subPackageNameClient>client</subPackageNameClient>
```

## subPackageNameDatafetchers

- Type: string
- Required: false
- Default: client

Example

```xml

<subPackageNameDatafetchers>datafetchers</subPackageNameDatafetchers>
```

## subPackageNameTypes

- Type: string
- Required: false
- Default: client

Example

```xml

<subPackageNameTypes>types</subPackageNameTypes>
```

## generateBoxedTypes

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateBoxedTypes>false</generateBoxedTypes>
```

## generateClientApi

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateClientApi>false</generateClientApi>
```

## generateClientApiV2

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateClientApiV2>false</generateClientApiV2>
```

## generateInterfaces

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateInterfaces>false</generateInterfaces>
```

## generateKotlinNullableClasses

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateKotlinNullableClasses>false</generateKotlinNullableClasses>
```

## generateKotlinClosureProjections

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateKotlinClosureProjections>false</generateKotlinClosureProjections>
```

## outputDir

- Type: string
- Required: false
- Default: `${project.build.directory}/generated-sources`

Example:

```xml

<outputDir>${project.build.directory}/generated-sources</outputDir>
```

## autoAddSource

Controls whether the plugin automatically adds the generated sources directory to the Maven compile classpath. This eliminates the need for the build-helper-maven-plugin in most setups. 

- Type: boolean
- Required: false
- Default: true

Example:

```xml
<autoAddSource>true</autoAddSource>
```

## exampleOutputDir

- Type: string
- Required: false
- Default: `${project.build.directory}/generated-examples`

Example:

```xml

<exampleOutputDir>${project.build.directory}/generated-examples</exampleOutputDir>
```

## schemaManifestOutputDir

- Type: string
- Required: false
- Default: `${project.build.directory}/graphqlcodegen`

Example:

```xml

<schemaManifestOutputDir>${project.build.directory}/graphqlcodegen</schemaManifestOutputDir>
```

## includeQueries

- Description: Limit generation to specified set of queries. Used in conjunction
  with `generateClient`.
- Type: array
- Required: false
- Default: []

Example

```xml

<includeQueries>
  <param>QueryFieldName1</param>
  <param>QueryFieldName2</param>
</includeQueries>
```

## includeMutations

- Description: Limit generation to specified set of mutations. Used in conjunction
  with `generateClient`.
- Type: array
- Required: false
- Default: []

Example

```xml

<includeMutations>
  <param>MutationFieldName1</param>
  <param>MutationFieldName1</param>
</includeMutations>
```

## skipEntityQueries

- Type: boolean
- Required: false
- Default: false

Example

```xml

<skipEntityQueries>false</skipEntityQueries>
```

## shortProjectionNames

- Type: boolean
- Required: false
- Default: false

Example

```xml

<shortProjectionNames>false</shortProjectionNames>
```

## generateDataTypes

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateDataTypes>false</generateDataTypes>
```

## maxProjectionDepth

- Type: int
- Required: false
- Default: 10

Example

```xml

<maxProjectionDepth>10</maxProjectionDepth>
```

## language

- Type: String
- Required: false
- Default: java

Example

```xml

<language>kotlin</language>
```

## omitNullInputFields

- Type: boolean
- Required: false
- Default: false

Example

```xml

<omitNullInputFields>false</omitNullInputFields>
```

## kotlinAllFieldsOptional

- Type: boolean
- Required: false
- Default: false

Example

```xml

<kotlinAllFieldsOptional>false</kotlinAllFieldsOptional>
```

## snakeCaseConstantNames

- Type: boolean
- Required: false
- Default: false

Example

```xml

<snakeCaseConstantNames>false</snakeCaseConstantNames>
```

## writeToFiles

- Type: boolean
- Required: false
- Default: true

Example

```xml

<writeToFiles>false</writeToFiles>
```

## includeSubscriptions

- Type: array
- Required: false
- Default: []

Example

```xml

<includeSubscriptions>
  <param>Subscriptions1</param>
  <param>Subscriptions2</param>
</includeSubscriptions>
```

## generateInterfaceSetters

- Type: boolean
- Required: false
- Default: true

Example

```xml

<generateInterfaceSetters>false</generateInterfaceSetters>
```

## generateInterfaceMethodsForInterfaceFields

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateInterfaceMethodsForInterfaceFields>false</generateInterfaceMethodsForInterfaceFields>
```

## javaGenerateAllConstructor

- Type: boolean
- Required: false
- Default: false

Example

```xml

<javaGenerateAllConstructor>false</javaGenerateAllConstructor>
```

## implementSerializable

- Type: boolean
- Required: false
- Default: false

Example

```xml

<implementSerializable>false</implementSerializable>
```

## generateCustomAnnotations

- Type: boolean
- Required: false
- Default: false

```xml

<generateCustomAnnotations>false</generateCustomAnnotations>
```

## addDeprecatedAnnotation

- Type: boolean
- Required: false
- Default: false

Example

```xml

<addDeprecatedAnnotation>true</addDeprecatedAnnotation>
```

## trackInputFieldSet

Generate has[FieldName] methods keeping track of what fields are explicitly set on input types. This is useful for distinguishing between fields that were explicitly set to null versus fields that were never set.

- Type: boolean
- Required: false
- Default: false

Example

```xml
<trackInputFieldSet>true</trackInputFieldSet>
```

## includeImports

- Type: map<string, string>
- Required: false

```xml

<includeImports>
  <validator>com.test.validator</validator>
</includeImports>
```

## includeEnumImports

- Type: map<string,<string,string>>
- Required: false

```xml

<includeEnumImports>
  <foo>
    <properties>
      <bar>bla</bar>
    </properties>
  </foo>
  <bar>
    <properties>
      <zoo>bar.bar</zoo>
      <zing>bla.bla</zing>
    </properties>
  </bar>
</includeEnumImports>
```

## includeClassImports

Maps the custom annotation and class names to the class packages. Only used when
generateCustomAnnotations is enabled.

- Type
- Required: false

```xml

<includeClassImports>
  <foo>
    <properties>
      <bar>bla</bar>
    </properties>
  </foo>
  <bar>
    <properties>
      <zoo>bar.bar</zoo>

      <zing>bla.bla</zing>
    </properties>
  </bar>
</includeClassImports>
```

## generateIsGetterForPrimitiveBooleanFields

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateIsGetterForPrimitiveBooleanFields>false</generateIsGetterForPrimitiveBooleanFields>
```

## disableDatesInGeneratedAnnotation

- Type: boolean
- Required: false
- Default: false

Example

```xml

<disableDatesInGeneratedAnnotation>true</disableDatesInGeneratedAnnotation>
```

## introspectionRequests

> **Experimental:** This is a new feature and may change in future releases.

Allows you to generate code from a GraphQL schema fetched via introspection at build time. You can specify one or more endpoints, queries, operation names, and custom HTTP headers for dynamic schema fetching.

- Type: array of objects (introspectionRequest)
- Required: false
- Default: []

Each `introspectionRequest` supports:
- `url` (string, required): The GraphQL endpoint URL
- `query` (string, optional): Path to a custom introspection query file (defaults to standard introspection query)
- `operationName` (string, optional): The operation name for the introspection query
- `headers` (map<string, string>, optional): HTTP headers to include in the request

Example:

```xml
<introspectionRequests>
  <introspectionRequest>
    <url>https://your-graphql-endpoint/graphql</url>
    <query>{ __schema { queryType { name } } }</query>
    <operationName>IntrospectionQuery</operationName>
    <headers>
      <Authorization>Bearer ${env.YOUR_TOKEN}</Authorization>
      <X-Custom-Header>value</X-Custom-Header>
    </headers>
  </introspectionRequest>
</introspectionRequests>
```

# AI Stories & Project History

Below is a curated set of AI-generated documentation and project history, summarizing key architectural changes, automation, and major improvements. These documents provide context for contributors and maintainers.

| Title | Link | Quick Summary |
|-------|------|--------------|
| Automating GraphQL Codegen Maven Plugin Maintenance | [human-thoughts.md](docs/ai-stories/human-thoughts.md) | Reflections on automating plugin maintenance, AI-assisted development, and test coverage improvements. |
| Multi-Module Refactor & Parameter Plugin Integration | [chat-summary-pr-multimodule-paramplugin.md](docs/ai-stories/chat-summary-pr-multimodule-paramplugin.md) | Details the transition to a multi-module Maven setup and parameter plugin automation. |
| Major Accomplishments: May 2024 PRs (216, 217, 221, 225, 226, 227, 228) | [chat-summary-major-accomplishments.md](docs/ai-stories/chat-summary-major-accomplishments.md) | Summarizes architectural evolution, automation, and future direction post-major PRs. |
| PR: Add Code Coverage Reporting, Badge, and GitHub Pages Publishing | [chat-summary-pr-coverage-report-pages.md](docs/ai-stories/chat-summary-pr-coverage-report-pages.md) | Documents the addition of code coverage, CI integration, and publishing to GitHub Pages. |
| PR Summary: May 2024 | [chat-summary-pr-summary.md](docs/ai-stories/chat-summary-pr-summary.md) | High-level summary of recent PRs, dependency updates, and type mapping improvements. |
| PR-235: Remote Schema Support & Javadoc Compliance | [pr-235-remote-schema-javadoc.md](docs/ai-stories/pr-235-remote-schema-javadoc.md) | Adds remote schema URL support, deterministic downloads, and documents the iterative Javadoc compliance process for Maven Central release. |

# Usage

Add the following to your pom files build/plugins section.

```xml

<plugin>
  <groupId>io.github.deweyjose</groupId>
  <artifactId>graphqlcodegen-maven-plugin</artifactId>
  <version>1.24</version>
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
    <packageName>com.acme.[your_project].generated</packageName>    
  </configuration>
</plugin>
```

# Generated Output

The generated types are available as part of the packageName.types package under build/generated. These are automatically added to your project's sources (no build-helper plugin required). The generated example data fetchers are available under build/generated-examples. Note that these are NOT added to your project's sources and serve mainly as a basic boilerplate code requiring further customization.
