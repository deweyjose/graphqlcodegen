package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.util.Map;
import lombok.Getter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

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
  private File[] schemaPaths;

  @Parameter(alias = "schemaJarFilesFromDependencies", property = "schemaJarFilesFromDependencies")
  private String[] schemaJarFilesFromDependencies;

  @Parameter(
      property = "schemaManifestOutputDir",
      defaultValue = "${project.build.directory}/graphqlcodegen")
  private File schemaManifestOutputDir;

  @Parameter(property = "onlyGenerateChanged", defaultValue = "true")
  private boolean onlyGenerateChanged;

  @Parameter(property = "typeMappingPropertiesFiles")
  private String[] typeMappingPropertiesFiles;

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
  private String[] includeQueries;

  @Parameter(property = "includeMutations")
  private String[] includeMutations;

  @Parameter(property = "includeSubscriptions")
  private String[] includeSubscriptions;

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

  @Override
  public void execute() {
    new CodegenExecutor(getLog()).execute(this, project.getArtifacts(), project.getBasedir());
  }

  // Dummy function for testing coverage reporting
  public void dummyFunction() {
    // does nothing
  }
}
