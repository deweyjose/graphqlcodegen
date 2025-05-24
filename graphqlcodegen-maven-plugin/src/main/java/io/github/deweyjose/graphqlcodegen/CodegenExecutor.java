package io.github.deweyjose.graphqlcodegen;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import io.github.deweyjose.codegen.generated.GeneratedCodeGenConfigBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
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
  public void execute(CodegenConfigProvider request, Set<Artifact> artifacts) {
    if (request.isSkip()) {
      log.info("Skipping code generation as requested (skip=true)");
      return;
    }

    final Set<File> fullSchemaPaths;

    if (request.isOnlyGenerateChanged()) {
      fullSchemaPaths = expandSchemaPaths(request.getSchemaPaths());
      log.info(String.format("expanded schema paths: %s", fullSchemaPaths));
    } else {
      fullSchemaPaths = stream(request.getSchemaPaths()).collect(Collectors.toSet());
    }

    verifySchemaFiles(fullSchemaPaths, request.getSchemaJarFilesFromDependencies());

    SchemaFileManifest manifest =
        new SchemaFileManifest(
            new File(request.getSchemaManifestOutputDir(), "schema-manifest.props"), new File("."));

    Set<File> filteredSchemaFiles = fullSchemaPaths;
    if (request.isOnlyGenerateChanged()) {
      filteredSchemaFiles = filterChangedSchemaFiles(fullSchemaPaths, manifest);
      log.info(String.format("changed schema files: %s", filteredSchemaFiles));
    }

    if (filteredSchemaFiles.isEmpty() && request.getSchemaJarFilesFromDependencies().length < 1) {
      log.info("no files to generate");
      return;
    }

    Map<String, String> typeMapping =
        mergeTypeMapping(
            request.getTypeMapping(), request.getTypeMappingPropertiesFiles(), artifacts);

    List<File> schemaJarFiles =
        Optional.ofNullable(request.getSchemaJarFilesFromDependencies())
            .map(arr -> Arrays.stream(arr).map(File::new).toList())
            .orElse(Collections.emptyList());

    final CodeGenConfig config =
        new GeneratedCodeGenConfigBuilder()
            .setSchemas(Collections.emptySet())
            .setSchemaFiles(filteredSchemaFiles)
            .setSchemaJarFilesFromDependencies(schemaJarFiles)
            .setOutputDir(request.getOutputDir().toPath())
            .setExamplesOutputDir(request.getExamplesOutputDir().toPath())
            .setWriteToFiles(request.isWriteToFiles())
            .setPackageName(request.getPackageName())
            .setSubPackageNameClient(request.getSubPackageNameClient())
            .setSubPackageNameDatafetchers(request.getSubPackageNameDatafetchers())
            .setSubPackageNameTypes(request.getSubPackageNameTypes())
            .setSubPackageNameDocs(request.getSubPackageNameDocs())
            .setLanguage(Language.valueOf(request.getLanguage().toUpperCase()))
            .setGenerateBoxedTypes(request.isGenerateBoxedTypes())
            .setGenerateIsGetterForPrimitiveBooleanFields(
                request.isGenerateIsGetterForPrimitiveBooleanFields())
            .setGenerateClientApi(request.isGenerateClientApi())
            .setGenerateClientApiv2(request.isGenerateClientApiv2())
            .setGenerateInterfaces(request.isGenerateInterfaces())
            .setGenerateKotlinNullableClasses(request.isGenerateKotlinNullableClasses())
            .setGenerateKotlinClosureProjections(request.isGenerateKotlinClosureProjections())
            .setTypeMapping(typeMapping)
            .setIncludeQueries(toSet(request.getIncludeQueries()))
            .setIncludeMutations(toSet(request.getIncludeMutations()))
            .setIncludeSubscriptions(toSet(request.getIncludeSubscriptions()))
            .setSkipEntityQueries(request.isSkipEntityQueries())
            .setShortProjectionNames(request.isShortProjectionNames())
            .setGenerateDataTypes(request.isGenerateDataTypes())
            .setOmitNullInputFields(request.isOmitNullInputFields())
            .setMaxProjectionDepth(request.getMaxProjectionDepth())
            .setKotlinAllFieldsOptional(request.isKotlinAllFieldsOptional())
            .setSnakeCaseConstantNames(request.isSnakeCaseConstantNames())
            .setGenerateInterfaceSetters(request.isGenerateInterfaceSetters())
            .setGenerateInterfaceMethodsForInterfaceFields(
                request.isGenerateInterfaceMethodsForInterfaceFields())
            .setGenerateDocs(request.getGenerateDocs())
            .setGeneratedDocsFolder(Paths.get(request.getGeneratedDocsFolder()))
            .setIncludeImports(
                Optional.ofNullable(request.getIncludeImports()).orElse(Collections.emptyMap()))
            .setIncludeEnumImports(toMap(request.getIncludeEnumImports()))
            .setIncludeClassImports(toMap(request.getIncludeClassImports()))
            .setGenerateCustomAnnotations(request.isGenerateCustomAnnotations())
            .setJavaGenerateAllConstructor(request.isJavaGenerateAllConstructor())
            .setImplementSerializable(request.isImplementSerializable())
            .setAddGeneratedAnnotation(request.isAddGeneratedAnnotation())
            .setDisableDatesInGeneratedAnnotation(request.isDisableDatesInGeneratedAnnotation())
            .setAddDeprecatedAnnotation(request.isAddDeprecatedAnnotation())
            .setTrackInputFieldSet(request.isTrackInputFieldSet())
            .build();

    log.info(String.format("Codegen config: \n%s", config));
    final CodeGen codeGen = new CodeGen(config);
    codeGen.generate();
    if (request.isOnlyGenerateChanged()) {
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
  public static Set<File> expandSchemaPaths(File[] schemaPaths) {
    Set<File> configuredSchemaPaths = stream(schemaPaths).collect(Collectors.toSet());
    Set<File> expandedSchemaPaths = new HashSet<>();
    for (File path : configuredSchemaPaths) {
      if (path.isFile()) {
        expandedSchemaPaths.add(path);
      } else {
        expandedSchemaPaths.addAll(SchemaFileManifest.findGraphQLSFiles(path));
      }
    }

    return expandedSchemaPaths;
  }

  /**
   * Loads the type mapping from the properties file in the artifact.
   *
   * @param artifactFile the artifact file
   * @param typeMappingPropertiesFiles the type mapping properties files
   * @return the type mapping
   */
  public static Map<String, String> loadPropertiesFile(
      File artifactFile, String[] typeMappingPropertiesFiles) {
    Map<String, String> typeMapping = new HashMap<>();
    try (JarFile jarFile = new JarFile(artifactFile)) {

      for (String file : typeMappingPropertiesFiles) {
        ZipEntry entry = jarFile.getEntry(file);
        if (entry != null) {
          try (InputStream inputStream = jarFile.getInputStream(entry)) {
            Properties typeMappingProperties = new Properties();
            typeMappingProperties.load(inputStream);
            typeMappingProperties.forEach(
                (k, v) -> {
                  typeMapping.putIfAbsent(String.valueOf(k), String.valueOf(v));
                });
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return typeMapping;
  }

  /**
   * Verifies that the package name is not null.
   *
   * @param packageName the package name
   */
  public static void verifyPackageName(String packageName) {
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
  public static void verifySchemaFiles(
      Set<File> fullSchemaPaths, String[] schemaJarFilesFromDependencies) {
    if (fullSchemaPaths.isEmpty()
        && (schemaJarFilesFromDependencies == null || schemaJarFilesFromDependencies.length < 1)) {
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
  public static Map<String, String> mergeTypeMapping(
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
  public static Set<File> filterChangedSchemaFiles(
      Set<File> allSchemaFiles, SchemaFileManifest manifest) {
    manifest.setFiles(new HashSet<>(allSchemaFiles));
    Set<File> changed = new HashSet<>(allSchemaFiles);
    changed.retainAll(manifest.getChangedFiles());
    return changed;
  }

  public static Set<String> toSet(String[] arr) {
    return Optional.ofNullable(arr)
        .map(a -> java.util.Arrays.stream(a).collect(Collectors.toSet()))
        .orElse(Collections.emptySet());
  }

  public static Map<String, Map<String, String>> toMap(
      Map<String, io.github.deweyjose.graphqlcodegen.ParameterMap> m) {
    if (m == null) return Collections.emptyMap();
    return m.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() == null ? Collections.emptyMap() : e.getValue().getProperties()));
  }
}
