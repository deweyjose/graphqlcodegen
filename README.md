# graphqlcodegen-maven-plugin

This maven plugin is a port of the netflix codegen plugin for Gradle.
Found [here](https://github.com/Netflix/dgs-codegen).

# Contributing

### GitHub Issue

Feel free to simply create a GitHub issue for requests to integrate with
newer [releases](https://github.com/Netflix/dgs-codegen/releases) of the core DGS Codegen library.

### PRs

PRS are welcome as well. The level of difficulty across DGS Codgen updates varies. Typically we add
new plugin options and update
the [CodeGenConfig](https://github.com/Netflix/dgs-codegen/blob/master/graphql-dgs-codegen-core/src/main/kotlin/com/netflix/graphql/dgs/codegen/CodeGen.kt#L443)
constructor - when new
options are added to the Codegen core library.

Please make sure you run step 2 below to ensure your PR builds correctly. You may need to analyze
the CodeGenConfig ctor parameters and add support for new options.
Make sure to document any new options to the `Options` section below.

Process:

1. bump the version in [pom.xml](pom.xml)
2. run `mvn install` locally to ensure the project still builds
3. run `mvn spotless:apply` if there are any code style/formating violations
4. Adjust [CodeGen](src/main/java/io/github/deweyjose/graphqlcodegen/Codegen.java) to support new
   options if needed.

# Overview

The DGS Code Generation plugin generates code for basic types and example data fetchers based on
your Domain Graph
Service's graphql schema file during the project's build process. The plugin requires the
designated `packageName` for file generation.
If no `schemaPath` is specified, it will look in the src/main/resources/schema folder for
any files with .graphqls extension.

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

Directory paths: Only files with `.graphql` or `.graphqls` will be considered.

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

```xml

<addDeprecatedAnnotation>false</addDeprecatedAnnotation>
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

You'll also need to add the generates-sources folder to the classpath:

```xml

<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>build-helper-maven-plugin</artifactId>
  <executions>
    <execution>
      <phase>generate-sources</phase>
      <goals>
        <goal>add-source</goal>
      </goals>
      <configuration>
        <sources>
          <source>${project.build.directory}/generated-sources</source>
        </sources>
      </configuration>
    </execution>
  </executions>
</plugin>
```

# Generated Output

COPIED FROM NETFLIX DOCUMENTATION.

The generated types are available as part of the packageName.types package under build/generated.
These are
automatically added to your project's sources. The generated example data fetchers are available
under
build/generated-examples. Note that these are NOT added to your project's sources and serve mainly
as a basic
boilerplate code requiring further customization.
