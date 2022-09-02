package io.github.deweyjose.graphqlcodegen;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Codegen extends AbstractMojo {

	@Parameter(property = "schemaPaths", defaultValue = "${project.build.resources}/schema")
	private File[] schemaPaths;

	@Parameter(property = "packageName", defaultValue = "")
	private String packageName;

	@Parameter(property = "subPackageNameClient", defaultValue = "client")
	private String subPackageNameClient;

	@Parameter(property = "subPackageNameDatafetchers", defaultValue = "datafetchers")
	private String subPackageNameDatafetchers;

	@Parameter(property = "subPackageNameTypes", defaultValue = "types")
	private String subPackageNameTypes;

	@Parameter(property = "language", defaultValue = "java")
	private String language;

	@Parameter(property = "typeMapping")
	private Map<String, String> typeMapping;

	@Parameter(property = "generateBoxedTypes", defaultValue = "false")
	private boolean generateBoxedTypes;

	@Parameter(property = "generateClient", defaultValue = "false")
	private boolean generateClient;

	@Parameter(property = "generateDataTypes", defaultValue = "true")
	private boolean generateDataTypes;

	@Parameter(property = "generateInterfaces", defaultValue = "false")
	private boolean generateInterfaces;

	@Parameter(property = "generateKotlinNullableClasses", defaultValue = "false")
	private boolean generateKotlinNullableClasses;

	@Parameter(property = "generateKotlinClosureProjections", defaultValue = "false")
	private boolean generateKotlinClosureProjections;

	@Parameter(property = "outputDir", defaultValue = "${project.build.directory}/generated-sources")
	private File outputDir;

	@Parameter(property = "exampleOutputDir", defaultValue = "${project.build.directory}/generated-examples")
	private File exampleOutputDir;

	@Parameter(property = "includeQueries")
	private String[] includeQueries;

	@Parameter(property = "includeMutations")
	private String[] includeMutations;

	@Parameter(property = "skipEntityQueries", defaultValue = "false")
	private boolean skipEntityQueries;

	@Parameter(property = "shortProjectionNames", defaultValue = "false")
	private boolean shortProjectionNames;

	@Parameter(property = "maxProjectionDepth", defaultValue = "10")
	private int maxProjectionDepth;

	@Parameter(property = "omitNullInputFields", defaultValue = "false")
	private boolean omitNullInputFields;

	@Parameter(property = "kotlinAllFieldsOptional", defaultValue = "false")
	private boolean kotlinAllFieldsOptional;

	@Parameter(property = "snakeCaseConstantNames", defaultValue = "false")
	private boolean snakeCaseConstantNames;

	@Parameter(property = "writeToFiles", defaultValue = "true")
	private boolean writeToFiles;

	@Parameter(property = "includeSubscriptions")
	private String[] includeSubscriptions;

	@Parameter(property = "generateInterfaceSetters", defaultValue = "false")
	private boolean generateInterfaceSetters;

	@Parameter(property = "javaGenerateAllConstructor", defaultValue = "false")
	private boolean javaGenerateAllConstructor;

	@Parameter(property = "implementSerializable", defaultValue = "false")
	private boolean implementSerializable;
	
	@Parameter(property = "addGeneratedAnnotation", defaultValue = "false")
	private boolean addGeneratedAnnotation;

	@Parameter(property = "dgs.codegen.skip", defaultValue = "false", required = false)
	private boolean skip;

	@Parameter(property = "generateCustomAnnotations", defaultValue = "false")
	private boolean generateCustomAnnotations;

	@Parameter(property = "includeImports")
	private Map<String, String> includeImports;

	@Parameter(property = "includeEnumImports")
	private Map<String, Map<String, String>> includeEnumImports;

	private void verifySettings() {
		if (isNull(packageName)) {
			throw new RuntimeException("Please specify a packageName");
		}

		if (schemaPaths.length != stream(schemaPaths).collect(toSet()).size()) {
			throw new RuntimeException("Duplicate entries in schemaPaths");
		}

		for (final File schemaPath : schemaPaths) {
			if (!schemaPath.exists()) {
				try {
					throw new RuntimeException(
							format("Schema File: %s does not exist!", schemaPath.getCanonicalPath()));
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!skip) {
			verifySettings();
				    
			final CodeGenConfig config = new CodeGenConfig(
					emptySet(),
					stream(schemaPaths).collect(toSet()),
					outputDir.toPath(),
					exampleOutputDir.toPath(),
					writeToFiles,
					packageName,
					subPackageNameClient,
					subPackageNameDatafetchers,
					subPackageNameTypes,
					Language.valueOf(language.toUpperCase()),
					generateBoxedTypes,
					generateClient,
					generateInterfaces,
				    generateKotlinNullableClasses,
				    generateKotlinClosureProjections,
					typeMapping,
					stream(includeQueries).collect(toSet()),
					stream(includeMutations).collect(toSet()),
					stream(includeSubscriptions).collect(toSet()),
					skipEntityQueries,
					shortProjectionNames,
					generateDataTypes,
					omitNullInputFields,
					maxProjectionDepth,
					kotlinAllFieldsOptional,
					snakeCaseConstantNames,
					generateInterfaceSetters,
					includeImports,
					includeEnumImports,
					generateCustomAnnotations,
				    javaGenerateAllConstructor,
				    implementSerializable,
				    addGeneratedAnnotation
				);

			getLog().info(format("Codegen config: %n%s", config));

			final CodeGen codeGen = new CodeGen(config);
			codeGen.generate();
		}
	}
}
