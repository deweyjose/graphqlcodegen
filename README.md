This is port of the netflix codegen plugin for Gradle. Found [here](https://github.com/Netflix/dgs-codegen).

COPIED FROM NETFLIX DOCUMENTATION.

The DGS Code Generation plugin generates code for basic types and example data fetchers based on the your Domain Graph Service's graphql schema file during the project's build process. The plugin requires the path to schema files and the package name to use to generate the file. If no schema path is specified, it will look under src/resources/schema for any files with .graphqls extension. plugin generates code for basic types and example data fetchers based on the your Domain Graph Service's graphql schema file during the project's build process. The plugin requires the path to schema files and the package name to use to generate the file. If no schema path is specified, it will look under src/resources/schema for any files with .graphqls extension.

# Options

Options are configured in the `<configuration>` element of the dgs-codegen-maven-plugin plugin.


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

# Usage

Add the following to your pom files build/plugins section.
```xml
<plugin>
    <groupId>io.github.deweyjose</groupId>
	<artifactId>graphqlcodegen-maven-plugin</artifactId>
	<version>1.12</version>
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

The generated types are available as part of the packageName.types package under build/generated. These are automatically added to your project's sources. The generated example data fetchers are available under build/generated-examples. Note that these are NOT added to your project's sources and serve mainly as a basic boilerplate code requiring further customization.
