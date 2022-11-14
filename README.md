This is port of the netflix codegen plugin for Gradle. Found [here](https://github.com/Netflix/dgs-codegen).

COPIED FROM NETFLIX DOCUMENTATION.

The DGS Code Generation plugin generates code for basic types and example data fetchers based on the your Domain Graph
Service's graphql schema file during the project's build process. The plugin requires the path to schema files and the
package name to use to generate the file. If no schema path is specified, it will look under src/resources/schema for
any files with .graphqls extension. plugin generates code for basic types and example data fetchers based on the your
Domain Graph Service's graphql schema file during the project's build process. The plugin requires the path to schema
files and the package name to use to generate the file. If no schema path is specified, it will look under
src/resources/schema for any files with .graphqls extension.

# Example Repo

https://github.com/deweyjose/graphqlcodegen-example

# Options

Options are configured in the `<configuration>` element of the dgs-codegen-maven-plugin plugin.

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

- Type: array
- Required: false
- Default: `${project.build.resources}/schema`

Example

```xml

<schemaPaths>
    <param>src/main/resources/schema/schema.graphqls1</param>
    <param>src/main/resources/schema/schema.graphqls2</param>
</schemaPaths>
```

## schemaJarFilesFromDependencies

- Type: array
- Required: false
- Default: 
- Official doc : https://netflix.github.io/dgs/generating-code-from-schema/#generating-code-from-external-schemas-in-jars 

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

## generateClient

- Type: boolean
- Required: false
- Default: false

Example

```xml

<generateClient>false</generateClient>
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
- Default: `${project.basedir}/target/generated-sources`

Example:

```xml

<outputDir>${project.build.directory}/generated-sources</outputDir>
```

## exampleOutputDir

- Type: string
- Required: false
- Default: `${project.basedir}/target/generated-examples`

Example:

```xml

<outputDir>${project.build.directory}/generated-examples</outputDir>
```     

## includeQueries

- Description: Limit generation to specified set of queries. Used in conjunction with `generateClient`.
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

- Description: Limit generation to specified set of mutations. Used in conjunction with `generateClient`.
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
- Default: false

Example

```xml

<generateInterfaceSetters>false</generateInterfaceSetters>
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

- Type 
- Requierd: false

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

```Maps the custom annotation and class names to the class packages. Only used when generateCustomAnnotations is enabled.


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
        <addGeneratedAnnotation>true</addGeneratedAnnotation>
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

The generated types are available as part of the packageName.types package under build/generated. These are
automatically added to your project's sources. The generated example data fetchers are available under
build/generated-examples. Note that these are NOT added to your project's sources and serve mainly as a basic
boilerplate code requiring further customization.
