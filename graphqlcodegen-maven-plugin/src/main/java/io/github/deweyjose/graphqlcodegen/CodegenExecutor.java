package io.github.deweyjose.graphqlcodegen;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import io.github.deweyjose.codegen.generated.GeneratedCodeGenConfigBuilder;
import io.github.deweyjose.graphqlcodegen.parameters.ParameterMap;
import io.github.deweyjose.graphqlcodegen.services.SchemaFileService;
import io.github.deweyjose.graphqlcodegen.services.TypeMappingService;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.maven.artifact.Artifact;

/**
 * Executes code generation and provides utility methods for schema expansion, manifest, and type
 * mapping.
 */
public class CodegenExecutor {
  private final SchemaFileService schemaFileService;
  private final TypeMappingService typeMappingService;

  /**
   * Constructor for CodegenExecutor.
   *
   * @param schemaFileService the schema file service
   * @param typeMappingService the type mapping service
   */
  public CodegenExecutor(
      SchemaFileService schemaFileService, TypeMappingService typeMappingService) {
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
    // get the schema paths that might have changed or all of them.
    if (request.isOnlyGenerateChanged()) {
      schemaFileService.loadExpandedSchemaPaths(request.getSchemaPaths());
      Logger.info("expanded schema paths: {}", schemaFileService.getSchemaPaths());
    } else {
      schemaFileService.setSchemaPaths(request.getSchemaPaths());
    }

    // load the schema jar files from dependencies
    schemaFileService.loadSchemaJarFilesFromDependencies(
        artifacts, request.getSchemaJarFilesFromDependencies());

    schemaFileService.loadSchemaUrls(request.getSchemaUrls());
    schemaFileService.loadIntrospectedSchemas(request.getIntrospectionRequests());
    schemaFileService.checkHasSchemaFiles();

    if (request.isOnlyGenerateChanged()) {
      schemaFileService.filterChangedSchemaFiles();
      Logger.info("changed schema files: {}", schemaFileService.getSchemaPaths());
    }

    if (schemaFileService.noWorkToDo()) {
      Logger.info("no files to generate");
      return;
    }

    Map<String, String> typeMapping =
        typeMappingService.mergeTypeMapping(
            request.getTypeMapping(),
            request.getTypeMappingPropertiesFiles(),
            request.getLocalTypeMappingPropertiesFiles(),
            artifacts,
            projectBaseDir);

    final CodeGenConfig config =
        new GeneratedCodeGenConfigBuilder()
            .setSchemas(Collections.emptySet())
            .setSchemaFiles(schemaFileService.getSchemaPaths())
            .setSchemaJarFilesFromDependencies(
                schemaFileService.getSchemaJarFilesFromDependencies())
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
            .setIncludeQueries(request.getIncludeQueries())
            .setIncludeMutations(request.getIncludeMutations())
            .setIncludeSubscriptions(request.getIncludeSubscriptions())
            .setSkipEntityQueries(request.isSkipEntityQueries())
            .setShortProjectionNames(request.isShortProjectionNames())
            .setGenerateDataTypes(request.isGenerateDataTypes())
            .setOmitNullInputFields(request.isOmitNullInputFields())
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

    Logger.info("Codegen config: \n{}", config);
    final CodeGen codeGen = new CodeGen(config);
    codeGen.generate();

    if (request.isOnlyGenerateChanged()) {
      schemaFileService.syncManifest();
    }
  }

  /**
   * Converts a map of ParameterMap to a map of string-to-string maps.
   *
   * @param m the map to convert
   * @return a map of string to string maps
   */
  public static Map<String, Map<String, String>> toMap(Map<String, ParameterMap> m) {
    if (m == null) return Collections.emptyMap();
    return m.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() == null ? Collections.emptyMap() : e.getValue().getProperties()));
  }
}
