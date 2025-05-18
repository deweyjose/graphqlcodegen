package io.github.deweyjose.graphqlcodegen;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import com.netflix.graphql.dgs.codegen.Language;
import io.github.deweyjose.graphqlcodegen.models.CustomParameters;
import io.github.deweyjose.graphqlcodegen.models.DgsParameters;
import io.github.deweyjose.graphqlcodegen.models.ExecutionRequest;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
    name = "generate",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class Codegen extends AbstractMojo {

  @Parameter(defaultValue = "${project}")
  private MavenProject project;

  @Parameter(
      property = "schemaPaths",
      defaultValue = "${project.basedir}/src/main/resources/schema")
  private File[] schemaPaths;

  @Parameter(alias = "schemaJarFilesFromDependencies", property = "schemaJarFilesFromDependencies")
  private String[] schemaJarFilesFromDependencies;

  @Parameter(property = "packageName", defaultValue = "")
  private String packageName;

  @Parameter(property = "subPackageNameClient", defaultValue = "client")
  private String subPackageNameClient;

  @Parameter(property = "subPackageNameDatafetchers", defaultValue = "datafetchers")
  private String subPackageNameDatafetchers;

  @Parameter(property = "subPackageNameTypes", defaultValue = "types")
  private String subPackageNameTypes;

  @Parameter(property = "subPackageNameDocs", defaultValue = "docs")
  private String subPackageNameDocs;

  @Parameter(property = "language", defaultValue = "java")
  private String language;

  @Parameter(property = "typeMapping")
  private Map<String, String> typeMapping;

  /**
   * Provide the typeMapping as properties file(s) that is accessible as a compile-time-classpath
   * resource Values in the properties file will be added to `typeMapping` Map when it is not
   * already present
   */
  @Parameter(property = "typeMappingPropertiesFiles")
  private String[] typeMappingPropertiesFiles;

  @Parameter(property = "generateBoxedTypes", defaultValue = "false")
  private boolean generateBoxedTypes;

  @Parameter(property = "generateClientApi", defaultValue = "false")
  private boolean generateClientApi;

  @Parameter(property = "generateClientApiv2", defaultValue = "false")
  private boolean generateClientApiv2;

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

  @Parameter(
      property = "examplesOutputDir",
      defaultValue = "${project.build.directory}/generated-examples")
  private File examplesOutputDir;

  @Parameter(
      property = "schemaManifestOutputDir",
      defaultValue = "${project.build.directory}/graphqlcodegen")
  private File schemaManifestOutputDir;

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

  @Parameter(property = "generateInterfaceMethodsForInterfaceFields", defaultValue = "false")
  private boolean generateInterfaceMethodsForInterfaceFields;

  @Parameter(property = "generateDocs", defaultValue = "false")
  private Boolean generateDocs;

  @Parameter(property = "generatedDocsFolder", defaultValue = "./generated-docs")
  private String generatedDocsFolder;

  @Parameter(property = "javaGenerateAllConstructor", defaultValue = "false")
  private boolean javaGenerateAllConstructor;

  @Parameter(property = "implementSerializable", defaultValue = "false")
  private boolean implementSerializable;

  @Parameter(property = "addGeneratedAnnotation", defaultValue = "false")
  private boolean addGeneratedAnnotation;

  @Parameter(property = "addDeprecatedAnnotation", defaultValue = "false")
  private boolean addDeprecatedAnnotation;

  @Parameter(property = "trackInputFieldSet", defaultValue = "false")
  private boolean trackInputFieldSet;

  @Parameter(property = "dgs.codegen.skip", defaultValue = "false", required = false)
  private boolean skip;

  @Parameter(property = "generateCustomAnnotations", defaultValue = "false")
  private boolean generateCustomAnnotations;

  @Parameter(property = "includeImports")
  private Map<String, String> includeImports;

  @Parameter(property = "includeEnumImports")
  private Map<String, Properties> includeEnumImports;

  @Parameter(property = "includeClassImports")
  private Map<String, Properties> includeClassImports;

  @Parameter(property = "onlyGenerateChanged", defaultValue = "true")
  private boolean onlyGenerateChanged;

  @Parameter(property = "disableDatesInGeneratedAnnotation", defaultValue = "false")
  private boolean disableDatesInGeneratedAnnotation;

  @Parameter(property = "generateIsGetterForPrimitiveBooleanFields", defaultValue = "false")
  private boolean generateIsGetterForPrimitiveBooleanFields;

  @Override
  public void execute() {
    if (skip) {
      return;
    }

    // Map Mojo parameters to CustomParameters
    CustomParameters custom =
        new CustomParameters(
            schemaPaths,
            schemaJarFilesFromDependencies,
            schemaManifestOutputDir,
            onlyGenerateChanged,
            project.getBasedir(),
            typeMappingPropertiesFiles);

    // Map Mojo parameters to DgsParameters
    DgsParameters dgs =
        new DgsParameters(
            emptySet(), // schemaPaths (not used in executor)
            emptySet(), // fullSchemaPaths (not used in executor)
            DependencySchemaExtractor.extract(project, schemaJarFilesFromDependencies),
            outputDir.toPath(),
            examplesOutputDir.toPath(),
            writeToFiles,
            packageName,
            subPackageNameClient,
            subPackageNameDatafetchers,
            subPackageNameTypes,
            subPackageNameDocs,
            Language.valueOf(language.toUpperCase()),
            generateBoxedTypes,
            generateIsGetterForPrimitiveBooleanFields,
            generateClientApi,
            generateClientApiv2,
            generateInterfaces,
            generateKotlinNullableClasses,
            generateKotlinClosureProjections,
            typeMapping == null ? new HashMap<>() : typeMapping,
            includeQueries == null ? new HashSet<>() : stream(includeQueries).collect(toSet()),
            includeMutations == null ? new HashSet<>() : stream(includeMutations).collect(toSet()),
            includeSubscriptions == null
                ? new HashSet<>()
                : stream(includeSubscriptions).collect(toSet()),
            skipEntityQueries,
            shortProjectionNames,
            generateDataTypes,
            omitNullInputFields,
            maxProjectionDepth,
            kotlinAllFieldsOptional,
            snakeCaseConstantNames,
            generateInterfaceSetters,
            generateInterfaceMethodsForInterfaceFields,
            generateDocs,
            Paths.get(generatedDocsFolder),
            generateCustomAnnotations,
            javaGenerateAllConstructor,
            implementSerializable,
            addGeneratedAnnotation,
            disableDatesInGeneratedAnnotation,
            addDeprecatedAnnotation,
            trackInputFieldSet,
            includeImports == null ? new HashMap<>() : includeImports,
            includeEnumImports == null ? new HashMap<>() : includeEnumImports,
            includeClassImports == null ? new HashMap<>() : includeClassImports);

    ExecutionRequest request = new ExecutionRequest(custom, dgs);
    CodegenExecutor executor = new CodegenExecutor(getLog());
    Set<Artifact> artifacts = new HashSet<>(project.getArtifacts());
    executor.execute(request, artifacts);
  }
}
