package io.github.deweyjose.graphqlcodegen;

import io.github.deweyjose.graphqlcodegen.parameters.IntrospectionRequest;
import io.github.deweyjose.graphqlcodegen.parameters.ParameterMap;
import io.github.deweyjose.graphqlcodegen.services.SchemaFileService;
import io.github.deweyjose.graphqlcodegen.services.SchemaManifestService;
import io.github.deweyjose.graphqlcodegen.services.TypeMappingService;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Maven Mojo for GraphQL code generation. */
@Getter
@Mojo(
    name = "generate",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class Codegen extends AbstractMojo implements CodegenConfigProvider {

  @Parameter(defaultValue = "${project}")
  private MavenProject project;

  @Parameter(
      property = "schemaPaths",
      defaultValue = "${project.basedir}/src/main/resources/schema")
  private Set<File> schemaPaths;

  @Parameter(property = "schemaUrls")
  private List<String> schemaUrls;

  @Parameter(alias = "schemaJarFilesFromDependencies", property = "schemaJarFilesFromDependencies")
  private Set<String> schemaJarFilesFromDependencies;

  @Parameter(
      property = "schemaManifestOutputDir",
      defaultValue = "${project.build.directory}/graphqlcodegen")
  private File schemaManifestOutputDir;

  @Parameter(property = "onlyGenerateChanged", defaultValue = "true")
  private boolean onlyGenerateChanged;

  @Parameter(property = "typeMappingPropertiesFiles")
  private List<String> typeMappingPropertiesFiles;

  @Parameter(property = "dgs.codegen.skip", defaultValue = "false", required = false)
  private boolean skip;

  @Parameter(property = "outputDir", defaultValue = "${project.build.directory}/generated-sources")
  private File outputDir;

  @Parameter(
      property = "examplesOutputDir",
      defaultValue = "${project.build.directory}/generated-examples")
  private File examplesOutputDir;

  @Parameter(property = "writeToFiles", defaultValue = "true")
  private boolean writeToFiles;

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

  @Parameter(property = "generateBoxedTypes", defaultValue = "false")
  private boolean generateBoxedTypes;

  @Parameter(property = "generateIsGetterForPrimitiveBooleanFields", defaultValue = "false")
  private boolean generateIsGetterForPrimitiveBooleanFields;

  @Parameter(property = "generateClientApi", defaultValue = "false")
  private boolean generateClientApi;

  @Parameter(property = "generateClientApiv2", defaultValue = "false")
  private boolean generateClientApiv2;

  @Parameter(property = "generateInterfaces", defaultValue = "false")
  private boolean generateInterfaces;

  @Parameter(property = "generateKotlinNullableClasses", defaultValue = "false")
  private boolean generateKotlinNullableClasses;

  @Parameter(property = "generateKotlinClosureProjections", defaultValue = "false")
  private boolean generateKotlinClosureProjections;

  @Parameter(property = "includeQueries")
  private Set<String> includeQueries;

  @Parameter(property = "includeMutations")
  private Set<String> includeMutations;

  @Parameter(property = "includeSubscriptions")
  private Set<String> includeSubscriptions;

  @Parameter(property = "skipEntityQueries", defaultValue = "false")
  private boolean skipEntityQueries;

  @Parameter(property = "shortProjectionNames", defaultValue = "false")
  private boolean shortProjectionNames;

  @Parameter(property = "generateDataTypes", defaultValue = "true")
  private boolean generateDataTypes;

  @Parameter(property = "omitNullInputFields", defaultValue = "false")
  private boolean omitNullInputFields;

  @Parameter(property = "maxProjectionDepth", defaultValue = "10")
  private int maxProjectionDepth;

  @Parameter(property = "kotlinAllFieldsOptional", defaultValue = "false")
  private boolean kotlinAllFieldsOptional;

  @Parameter(property = "snakeCaseConstantNames", defaultValue = "false")
  private boolean snakeCaseConstantNames;

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

  @Parameter(property = "generateCustomAnnotations", defaultValue = "false")
  private boolean generateCustomAnnotations;

  @Parameter(property = "includeImports")
  private Map<String, String> includeImports;

  @Parameter(property = "includeEnumImports")
  private Map<String, ParameterMap> includeEnumImports;

  @Parameter(property = "includeClassImports")
  private Map<String, ParameterMap> includeClassImports;

  @Parameter(property = "disableDatesInGeneratedAnnotation", defaultValue = "false")
  private boolean disableDatesInGeneratedAnnotation;

  @Parameter(property = "autoAddSource", defaultValue = "true")
  private boolean autoAddSource;

  @Parameter(property = "introspectionRequests")
  private List<IntrospectionRequest> introspectionRequests;

  @Override
  public void execute() {
    Logger.registerMavenLog(getLog());

    if (skip) {
      Logger.info("Skipping code generation as requested (skip=true)");
      return;
    }

    SchemaManifestService manifest =
        new SchemaManifestService(schemaManifestOutputDir, project.getBasedir());
    TypeMappingService typeMappingService = new TypeMappingService();
    SchemaFileService schemaFileService = new SchemaFileService(outputDir, manifest);

    Set<Artifact> artifacts = project.getArtifacts();

    var executor = new CodegenExecutor(schemaFileService, typeMappingService);
    executor.execute(this, artifacts, project.getBasedir());

    if (autoAddSource) {
      project.addCompileSourceRoot(outputDir.getAbsolutePath());
    }
  }
}
