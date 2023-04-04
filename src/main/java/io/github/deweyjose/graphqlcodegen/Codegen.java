package io.github.deweyjose.graphqlcodegen;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Codegen extends AbstractMojo {

  @Parameter(defaultValue = "${project}")
  private MavenProject project;

  @Parameter(property = "schemaPaths")
  private File[] schemaPaths;

  @Parameter(alias = "schemaJarFilesFromDependencies", property = "schemaJarFilesFromDependencies")
  private String[] schemaJarFilesFromDependencies;
  private final List<File> schemaJarFilesFromDependenciesFiles = new ArrayList<>();

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

  @Parameter(property = "generateClientApi", defaultValue = "false")
  private boolean generateClientApi;

  @Parameter(property = "generateClientApiV2", defaultValue = "false")
  private boolean generateClientApiV2;

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

  @Parameter(property = "generateInterfaceMethodsForInterfaceFields", defaultValue = "false")
  private boolean generateInterfaceMethodsForInterfaceFields;

  @Parameter(property = "javaGenerateAllConstructor", defaultValue = "false")
  private boolean javaGenerateAllConstructor;

  @Parameter(property = "implementSerializable", defaultValue = "false")
  private boolean implementSerializable;

  @Parameter(property = "addGeneratedAnnotation", defaultValue = "false")
  private boolean addGeneratedAnnotation;

  @Parameter(property = "addDeprecatedAnnotation", defaultValue = "false")
  private boolean addDeprecatedAnnotation;

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

  private void verifySettings() {
    if (isNull(packageName)) {
      throw new RuntimeException("Please specify a packageName");
    }

    int size = stream(schemaPaths).collect(toSet()).size();
    if (schemaPaths.length != size) {
      stream(schemaPaths).forEach(s -> {
        getLog().error("schemaPath: " + s.getPath());
      });
      throw new RuntimeException("Duplicate entries in schemaPaths");
    }


    for (String jarDep : schemaJarFilesFromDependencies) {
      String jarDepClean = jarDep.trim();
      if (jarDepClean.isEmpty())
        continue;

      Optional<Artifact> artifactOpt = findFromDependencies(jarDepClean);
      if (artifactOpt.isPresent()) {
        Artifact artifact = artifactOpt.get();
        File file = artifact.getFile();
        this.schemaJarFilesFromDependenciesFiles.add(file);
      } else {
        getLog().warn(String.format("Unable to find Artifact`%s`", jarDepClean));
      }

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

      // @formatter:off
      final CodeGenConfig config = new CodeGenConfig(
        emptySet(),
        stream(schemaPaths).collect(toSet()),
        schemaJarFilesFromDependenciesFiles,
        outputDir.toPath(),
        exampleOutputDir.toPath(),
        writeToFiles,
        packageName,
        subPackageNameClient,
        subPackageNameDatafetchers,
        subPackageNameTypes,
        Language.valueOf(language.toUpperCase()),
        generateBoxedTypes,
        generateClientApi,
        generateClientApiV2,
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
        generateInterfaceMethodsForInterfaceFields,
        includeImports,
        includeEnumImports
          .entrySet()
          .stream()
          .collect(toMap(
            Entry::getKey,
            entry -> entry.getValue().getProperties()
          )),
        includeClassImports
          .entrySet()
          .stream()
          .collect(toMap(
            Entry::getKey,
            entry -> entry.getValue().getProperties()
          )),
        generateCustomAnnotations,
        javaGenerateAllConstructor,
        implementSerializable,
        addGeneratedAnnotation,
        addDeprecatedAnnotation
      );
      // @formatter:on

      getLog().info(format("Codegen config: %n%s", config));

      final CodeGen codeGen = new CodeGen(config);
      codeGen.generate();
    }
  }

  private Optional<Artifact> findFromDependencies(String artifactRef) {
    String cleanRef = artifactRef.trim();
    Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
    for (Artifact artifact : dependencyArtifacts) {
      String ref = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();

      if (ref.equals(cleanRef)) {
        return Optional.of(artifact);
      }
    }
    return Optional.empty();
  }
}
