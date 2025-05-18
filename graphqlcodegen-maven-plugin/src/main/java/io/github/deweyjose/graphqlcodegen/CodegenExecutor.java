package io.github.deweyjose.graphqlcodegen;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import io.github.deweyjose.graphqlcodegen.models.CustomParameters;
import io.github.deweyjose.graphqlcodegen.models.DgsParameters;
import io.github.deweyjose.graphqlcodegen.models.ExecutionRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

public class CodegenExecutor {
  private final Log log;

  public CodegenExecutor(Log log) {
    this.log = log;
  }

  /**
   * Executes the code generation.
   *
   * @param request the execution request
   * @param artifacts the artifacts
   */
  public void execute(ExecutionRequest request, Set<Artifact> artifacts) {

    CustomParameters custom = request.customParameters();
    DgsParameters dgs = request.dgsParameters();

    verifyPackageName(dgs.packageName());

    Set<File> fullSchemaPaths =
        expandSchemaPaths(custom.schemaPaths(), custom.onlyGenerateChanged());

    verifySchemaFiles(fullSchemaPaths, custom.schemaJarFilesFromDependencies());

    SchemaFileManifest manifest =
        new SchemaFileManifest(
            new File(custom.schemaManifestOutputDir(), "schema-manifest.props"), custom.baseDir());

    Set<File> filteredSchemaFiles = fullSchemaPaths;
    if (custom.onlyGenerateChanged()) {
      filteredSchemaFiles = filterChangedSchemaFiles(fullSchemaPaths, manifest);
    }

    if (filteredSchemaFiles.isEmpty() && custom.schemaJarFilesFromDependencies().length < 1) {
      log.info("no files to generate");
      return;
    }

    Map<String, String> typeMapping =
        mergeTypeMapping(dgs.typeMapping(), custom.typeMappingPropertiesFiles(), artifacts);

    final CodeGenConfig config =
        new CodeGenConfig(
            emptySet(),
            filteredSchemaFiles,
            dgs.dependencySchemas(),
            dgs.outputDir(),
            dgs.examplesOutputDir(),
            dgs.writeToFiles(),
            dgs.packageName(),
            dgs.subPackageNameClient(),
            dgs.subPackageNameDatafetchers(),
            dgs.subPackageNameTypes(),
            dgs.subPackageNameDocs(),
            dgs.language(),
            dgs.generateBoxedTypes(),
            dgs.generateIsGetterForPrimitiveBooleanFields(),
            dgs.generateClientApi(),
            dgs.generateClientApiv2(),
            dgs.generateInterfaces(),
            dgs.generateKotlinNullableClasses(),
            dgs.generateKotlinClosureProjections(),
            typeMapping,
            dgs.includeQueries(),
            dgs.includeMutations(),
            dgs.includeSubscriptions(),
            dgs.skipEntityQueries(),
            dgs.shortProjectionNames(),
            dgs.generateDataTypes(),
            dgs.omitNullInputFields(),
            dgs.maxProjectionDepth(),
            dgs.kotlinAllFieldsOptional(),
            dgs.snakeCaseConstantNames(),
            dgs.generateInterfaceSetters(),
            dgs.generateInterfaceMethodsForInterfaceFields(),
            dgs.generateDocs(),
            dgs.generatedDocsFolder(),
            dgs.includeImports(),
            dgs.includeEnumImports().entrySet().stream()
                .collect(toMap(Entry::getKey, entry -> entry.getValue().getProperties())),
            dgs.includeClassImports().entrySet().stream()
                .collect(toMap(Entry::getKey, entry -> entry.getValue().getProperties())),
            dgs.generateCustomAnnotations(),
            dgs.javaGenerateAllConstructor(),
            dgs.implementSerializable(),
            dgs.addGeneratedAnnotation(),
            dgs.disableDatesInGeneratedAnnotation(),
            dgs.addDeprecatedAnnotation(),
            dgs.trackInputFieldSet());

    log.info(String.format("Codegen config: \n%s", config));

    final CodeGen codeGen = new CodeGen(config);
    codeGen.generate();

    if (custom.onlyGenerateChanged()) {
      try {
        manifest.syncManifest();
      } catch (Exception e) {
        log.warn("error syncing manifest", e);
      }
    }
  }

  /**
   * Expands the schema paths to include all schema files in the directories.
   *
   * @param schemaPaths the schema paths
   * @param onlyGenerateChanged whether to only generate changed schema files
   * @return the expanded schema paths
   */
  public Set<File> expandSchemaPaths(File[] schemaPaths, boolean onlyGenerateChanged) {
    if (onlyGenerateChanged) {
      Set<File> configuredSchemaPaths = stream(schemaPaths).collect(toSet());
      Set<File> expandedSchemaPaths = new HashSet<>();
      for (File path : configuredSchemaPaths) {
        if (path.isFile()) {
          expandedSchemaPaths.add(path);
        } else {
          expandedSchemaPaths.addAll(SchemaFileManifest.findGraphQLSFiles(path));
        }
      }
      log.info(String.format("expanded schema paths: %s", expandedSchemaPaths));
      return expandedSchemaPaths;
    } else {
      return stream(schemaPaths).collect(toSet());
    }
  }

  /**
   * Loads the type mapping from the properties file in the artifact.
   *
   * @param artifactFile the artifact file
   * @param typeMappingPropertiesFiles the type mapping properties files
   * @return the type mapping
   */
  public Map<String, String> loadPropertiesFile(
      File artifactFile, String[] typeMappingPropertiesFiles) {
    Map<String, String> typeMapping = new HashMap<>();
    try (JarFile jarFile = new JarFile(artifactFile)) {

      for (String file : typeMappingPropertiesFiles) {
        ZipEntry entry = jarFile.getEntry(file);
        if (entry != null) {
          try (InputStream inputStream = jarFile.getInputStream(entry)) {
            log.info(
                String.format(
                    "Loading typeMapping from %s in artifact %s",
                    file, artifactFile.getAbsolutePath()));
            java.util.Properties typeMappingProperties = new java.util.Properties();
            typeMappingProperties.load(inputStream);
            typeMappingProperties.forEach(
                (k, v) -> {
                  typeMapping.putIfAbsent(String.valueOf(k), String.valueOf(v));
                });
          }
        }
      }
    } catch (IOException e) {
      log.error(e);
    }
    return typeMapping;
  }

  /**
   * Verifies that the package name is not null.
   *
   * @param packageName the package name
   */
  public void verifyPackageName(String packageName) {
    if (isNull(packageName)) {
      throw new IllegalArgumentException("Please specify a packageName");
    }
  }

  /**
   * Verifies that there are schema files to generate. If there are no schema files and no schema
   * jar files from dependencies, an error is logged and an exception is thrown.
   *
   * @param fullSchemaPaths the full schema paths
   * @param schemaJarFilesFromDependencies the schema jar files from dependencies
   */
  public void verifySchemaFiles(
      Set<File> fullSchemaPaths, String[] schemaJarFilesFromDependencies) {
    if (fullSchemaPaths.isEmpty()
        && (schemaJarFilesFromDependencies == null || schemaJarFilesFromDependencies.length < 1)) {
      log.error(
          "No schema files found and no schemaJarFilesFromDependencies specified. "
              + "Refer to documentation for schemas and schemaJarFilesFromDependencies. ");
      throw new IllegalArgumentException("No schema files found. Please check your configuration.");
    }
  }

  /**
   * Merges user-supplied typeMapping with type mappings loaded from JAR properties files.
   * User-supplied values always take precedence over JAR values.
   *
   * @param userTypeMapping the user-supplied type mapping
   * @param typeMappingPropertiesFiles the type mapping properties files
   * @param artifacts the artifacts
   * @return the merged type mapping
   */
  public Map<String, String> mergeTypeMapping(
      Map<String, String> userTypeMapping,
      String[] typeMappingPropertiesFiles,
      Set<Artifact> artifacts) {
    Map<String, String> jarTypeMapping = new HashMap<>();
    if (typeMappingPropertiesFiles != null && typeMappingPropertiesFiles.length > 0) {
      for (Artifact dependency : artifacts) {
        File artifactFile = dependency.getFile();
        if (artifactFile != null && artifactFile.isFile()) {
          jarTypeMapping.putAll(loadPropertiesFile(artifactFile, typeMappingPropertiesFiles));
        }
      }
    }
    Map<String, String> finalTypeMapping = new HashMap<>(jarTypeMapping);
    if (userTypeMapping != null) {
      finalTypeMapping.putAll(userTypeMapping);
    }
    return finalTypeMapping;
  }

  /**
   * Returns only the changed schema files, using the manifest.
   *
   * @param allSchemaFiles the full schema paths
   * @param manifest the schema manifest
   * @return the changed schema files
   */
  public Set<File> filterChangedSchemaFiles(Set<File> allSchemaFiles, SchemaFileManifest manifest) {
    manifest.setFiles(new HashSet<>(allSchemaFiles));
    Set<File> changed = new HashSet<>(allSchemaFiles);
    changed.retainAll(manifest.getChangedFiles());
    log.info(String.format("changed schema files: %s", changed));
    return changed;
  }
}
