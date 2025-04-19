package io.github.deweyjose.graphqlcodegen;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
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

  @Parameter(property = "schemas", defaultValue = "${project.basedir}/src/main/resources/schema")
  private File[] schemas;

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

  /**
   * Helper function that computes schema paths to generate.
   *
   * @return
   */
  private Set<File> expandSchemaPaths() {
    if (onlyGenerateChanged) {
      Set<File> configuredSchemaPaths = stream(schemas).collect(toSet());
      Set<File> expandedSchemaPaths = new HashSet<>();

      // expand any directories into graphql file paths
      for (File path : configuredSchemaPaths) {
        if (path.isFile()) {
          expandedSchemaPaths.add(path);
        } else {
          expandedSchemaPaths.addAll(SchemaFileManifest.findGraphQLSFiles(path));
        }
      }

      getLog().info(String.format("expanded schema paths: %s", expandedSchemaPaths));
      return expandedSchemaPaths;
    } else {
      return stream(schemas).collect(toSet());
    }
  }

  @Override
  public void execute() {
    if (!skip) {

      verifyPackageName();

      Set<File> fullSchemaPaths = expandSchemaPaths();

      verifySchemaFiles(fullSchemaPaths);

      SchemaFileManifest manifest =
          new SchemaFileManifest(
              new File(schemaManifestOutputDir, "schema-manifest.props"), project.getBasedir());

      if (onlyGenerateChanged) {
        manifest.setFiles(new HashSet<>(fullSchemaPaths));
        fullSchemaPaths.retainAll(manifest.getChangedFiles());
        getLog().info(String.format("changed schema files: %s", fullSchemaPaths));
      }

      if (fullSchemaPaths.isEmpty() && schemaJarFilesFromDependencies.length < 1) {
        getLog().info("no files to generate");
        return;
      }

      if (typeMappingPropertiesFiles != null && typeMappingPropertiesFiles.length > 0) {
        Set<Artifact> dependencies = project.getArtifacts();
        java.util.Properties typeMappingProperties = new java.util.Properties();
        for (Artifact dependency : dependencies) {
          File artifactFile = dependency.getFile();
          if (artifactFile != null && artifactFile.isFile()) {
            loadPropertiesFile(typeMappingProperties, artifactFile, typeMappingPropertiesFiles);
          }
        }
        // Set key-value from this properties object to typeMapping Map
        // only when it is not already present in the Map
        if (typeMapping == null) {
          typeMapping = new HashMap<>();
        }
        typeMappingProperties.forEach(
            (k, v) -> {
              typeMapping.putIfAbsent(String.valueOf(k), String.valueOf(v));
            });
      }

      final CodeGenConfig config =
          new CodeGenConfig(
              emptySet(),
              fullSchemaPaths,
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
              generateDocs,
              Paths.get(generatedDocsFolder),
              includeImports,
              includeEnumImports.entrySet().stream()
                  .collect(toMap(Entry::getKey, entry -> entry.getValue().getProperties())),
              includeClassImports.entrySet().stream()
                  .collect(toMap(Entry::getKey, entry -> entry.getValue().getProperties())),
              generateCustomAnnotations,
              javaGenerateAllConstructor,
              implementSerializable,
              addGeneratedAnnotation,
              disableDatesInGeneratedAnnotation,
              addDeprecatedAnnotation);

      getLog().info(format("Codegen config: \n%s", config));

      final CodeGen codeGen = new CodeGen(config);
      codeGen.generate();

      if (onlyGenerateChanged) {
        try {
          manifest.syncManifest();
        } catch (Exception e) {
          getLog().warn("error syncing manifest", e);
        }
      }
    }
  }

  /**
   * @param typeMappingProperties: Java Properties where typeMapping will be loaded into
   * @param artifactFile: Artifact file
   * @param typeMappingPropertiesFiles: Input: Classpath location of typeMapping properties file
   * @return
   */
  private void loadPropertiesFile(
      java.util.Properties typeMappingProperties,
      File artifactFile,
      String[] typeMappingPropertiesFiles) {
    try (JarFile jarFile = new JarFile(artifactFile)) {
      for (String file : typeMappingPropertiesFiles) {
        ZipEntry entry = jarFile.getEntry(file);
        if (entry != null) {
          try (InputStream inputStream = jarFile.getInputStream(entry)) {
            getLog()
                .info(
                    String.format(
                        "Loading typeMapping from %s in artifact %s",
                        file, artifactFile.getAbsolutePath()));
            // load the data into the typeMappingProperties
            typeMappingProperties.load(inputStream);
          }
        }
      }
    } catch (IOException e) {
      getLog().error(e);
    }
  }

  public void verifyPackageName() {
    if (isNull(packageName)) {
      throw new IllegalArgumentException("Please specify a packageName");
    }
  }

  public void verifySchemaFiles(Set<File> fullSchemaPaths) {
    if (fullSchemaPaths.isEmpty() && schemaJarFilesFromDependencies.length < 1) {
      getLog()
          .error(
              "No schema files found and no schemaJarFilesFromDependencies specified. "
                  + "Refer to documentation for schemas and schemaJarFilesFromDependencies. ");
      throw new IllegalArgumentException("No schema files found. Please check your configuration.");
    }
  }
}
