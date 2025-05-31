package io.github.deweyjose.graphqlcodegen;

import static java.util.Arrays.stream;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import io.github.deweyjose.codegen.generated.GeneratedCodeGenConfigBuilder;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

/**
 * Executes code generation and provides utility methods for schema expansion, manifest, and type
 * mapping.
 */
public class CodegenExecutor {
  private final Log log;
  private final SchemaFileService schemaFileService;
  private final TypeMappingService typeMappingService;

  /**
   * Constructs a CodegenExecutor with the given Maven logger.
   *
   * @param log the Maven logger
   */
  public CodegenExecutor(
      Log log, SchemaFileService schemaFileService, TypeMappingService typeMappingService) {
    this.log = log;
    this.schemaFileService = schemaFileService;
    this.typeMappingService = typeMappingService;
  }

  /**
   * Executes the code generation.
   *
   * @param request the execution request
   * @param artifacts the artifacts
   * @param projectBaseDir the project base directory
   */
  @SneakyThrows
  public void execute(CodegenConfigProvider request, Set<Artifact> artifacts, File projectBaseDir) {
    if (request.isSkip()) {
      log.info("Skipping code generation as requested (skip=true)");
      return;
    }

    final Set<File> fullSchemaPaths;

    if (request.isOnlyGenerateChanged()) {
      fullSchemaPaths = schemaFileService.expandSchemaPaths(request.getSchemaPaths());
      log.info(String.format("expanded schema paths: %s", fullSchemaPaths));
    } else {
      fullSchemaPaths = stream(request.getSchemaPaths()).collect(Collectors.toSet());
    }

    for (String url : request.getSchemaUrls()) {
      fullSchemaPaths.add(
          schemaFileService.saveUrlToFile(url, request.getSchemaManifestOutputDir()));
    }
    schemaFileService.verifySchemaFiles(
        fullSchemaPaths, request.getSchemaJarFilesFromDependencies());

    SchemaManifestService manifest =
        new SchemaManifestService(
            new File(request.getSchemaManifestOutputDir(), "schema-manifest.props"),
            projectBaseDir);

    Set<File> filteredSchemaFiles = fullSchemaPaths;
    if (request.isOnlyGenerateChanged()) {
      filteredSchemaFiles = schemaFileService.filterChangedSchemaFiles(fullSchemaPaths, manifest);
      log.info(String.format("changed schema files: %s", filteredSchemaFiles));
    }

    if (filteredSchemaFiles.isEmpty() && request.getSchemaJarFilesFromDependencies().length < 1) {
      log.info("no files to generate");
      return;
    }

    Map<String, String> typeMapping =
        typeMappingService.mergeTypeMapping(
            request.getTypeMapping(), request.getTypeMappingPropertiesFiles(), artifacts);

    List<File> schemaJarFiles =
        DependencySchemaExtractor.extract(artifacts, request.getSchemaJarFilesFromDependencies());

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
   * Converts an array of strings to a set.
   *
   * @param arr the array
   * @return a set of strings
   */
  public static Set<String> toSet(String[] arr) {
    return Optional.ofNullable(arr)
        .map(a -> java.util.Arrays.stream(a).collect(Collectors.toSet()))
        .orElse(Collections.emptySet());
  }

  /**
   * Converts a map of ParameterMap to a map of string-to-string maps.
   *
   * @param m the map to convert
   * @return a map of string to string maps
   */
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
