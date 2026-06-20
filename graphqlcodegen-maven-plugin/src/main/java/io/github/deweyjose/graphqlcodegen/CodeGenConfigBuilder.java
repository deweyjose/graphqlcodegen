package io.github.deweyjose.graphqlcodegen;

import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Builder for constructing {@link CodeGenConfig}. */
public class CodeGenConfigBuilder {
  private Set<String> schemas;

  private Set<File> schemaFiles;

  private List<File> schemaJarFilesFromDependencies;

  private Path outputDir;

  private Path examplesOutputDir;

  private boolean writeToFiles;

  private String packageName;

  private String subPackageNameClient;

  private String subPackageNameDatafetchers;

  private String subPackageNameTypes;

  private String subPackageNameDocs;

  private Language language;

  private boolean generateBoxedTypes;

  private boolean generateIsGetterForPrimitiveBooleanFields;

  private boolean generateClientApi;

  private boolean generateClientApiv2;

  private boolean generateInterfaces;

  private boolean generateKotlinNullableClasses;

  private boolean generateKotlinClosureProjections;

  private Map<String, String> typeMapping;

  private Set<String> includeQueries;

  private Set<String> includeMutations;

  private Set<String> includeSubscriptions;

  private boolean skipEntityQueries;

  private boolean shortProjectionNames;

  private boolean generateDataTypes;

  private boolean kotlinAllFieldsOptional;

  private boolean snakeCaseConstantNames;

  private boolean generateInterfaceSetters;

  private boolean generateInterfaceMethodsForInterfaceFields;

  private boolean generateDocs;

  private Path generatedDocsFolder;

  private Map<String, String> includeImports;

  private Map<String, Map<String, String>> includeEnumImports;

  private Map<String, Map<String, String>> includeClassImports;

  private boolean generateCustomAnnotations;

  private boolean javaGenerateAllConstructor;

  private boolean implementSerializable;

  private boolean addGeneratedAnnotation;

  private boolean disableDatesInGeneratedAnnotation;

  private boolean addDeprecatedAnnotation;

  private boolean trackInputFieldSet;

  private boolean generateJSpecifyAnnotations;

  public CodeGenConfigBuilder setSchemas(Set<String> schemas) {
    this.schemas = schemas;
    return this;
  }

  public CodeGenConfigBuilder setSchemaFiles(Set<File> schemaFiles) {
    this.schemaFiles = schemaFiles;
    return this;
  }

  public CodeGenConfigBuilder setSchemaJarFilesFromDependencies(
      List<File> schemaJarFilesFromDependencies) {
    this.schemaJarFilesFromDependencies = schemaJarFilesFromDependencies;
    return this;
  }

  public CodeGenConfigBuilder setOutputDir(Path outputDir) {
    this.outputDir = outputDir;
    return this;
  }

  public CodeGenConfigBuilder setExamplesOutputDir(Path examplesOutputDir) {
    this.examplesOutputDir = examplesOutputDir;
    return this;
  }

  public CodeGenConfigBuilder setWriteToFiles(boolean writeToFiles) {
    this.writeToFiles = writeToFiles;
    return this;
  }

  public CodeGenConfigBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public CodeGenConfigBuilder setSubPackageNameClient(String subPackageNameClient) {
    this.subPackageNameClient = subPackageNameClient;
    return this;
  }

  public CodeGenConfigBuilder setSubPackageNameDatafetchers(String subPackageNameDatafetchers) {
    this.subPackageNameDatafetchers = subPackageNameDatafetchers;
    return this;
  }

  public CodeGenConfigBuilder setSubPackageNameTypes(String subPackageNameTypes) {
    this.subPackageNameTypes = subPackageNameTypes;
    return this;
  }

  public CodeGenConfigBuilder setSubPackageNameDocs(String subPackageNameDocs) {
    this.subPackageNameDocs = subPackageNameDocs;
    return this;
  }

  public CodeGenConfigBuilder setLanguage(Language language) {
    this.language = language;
    return this;
  }

  public CodeGenConfigBuilder setGenerateBoxedTypes(boolean generateBoxedTypes) {
    this.generateBoxedTypes = generateBoxedTypes;
    return this;
  }

  public CodeGenConfigBuilder setGenerateIsGetterForPrimitiveBooleanFields(
      boolean generateIsGetterForPrimitiveBooleanFields) {
    this.generateIsGetterForPrimitiveBooleanFields = generateIsGetterForPrimitiveBooleanFields;
    return this;
  }

  public CodeGenConfigBuilder setGenerateClientApi(boolean generateClientApi) {
    this.generateClientApi = generateClientApi;
    return this;
  }

  public CodeGenConfigBuilder setGenerateClientApiv2(boolean generateClientApiv2) {
    this.generateClientApiv2 = generateClientApiv2;
    return this;
  }

  public CodeGenConfigBuilder setGenerateInterfaces(boolean generateInterfaces) {
    this.generateInterfaces = generateInterfaces;
    return this;
  }

  public CodeGenConfigBuilder setGenerateKotlinNullableClasses(
      boolean generateKotlinNullableClasses) {
    this.generateKotlinNullableClasses = generateKotlinNullableClasses;
    return this;
  }

  public CodeGenConfigBuilder setGenerateKotlinClosureProjections(
      boolean generateKotlinClosureProjections) {
    this.generateKotlinClosureProjections = generateKotlinClosureProjections;
    return this;
  }

  public CodeGenConfigBuilder setTypeMapping(Map<String, String> typeMapping) {
    this.typeMapping = typeMapping;
    return this;
  }

  public CodeGenConfigBuilder setIncludeQueries(Set<String> includeQueries) {
    this.includeQueries = includeQueries;
    return this;
  }

  public CodeGenConfigBuilder setIncludeMutations(Set<String> includeMutations) {
    this.includeMutations = includeMutations;
    return this;
  }

  public CodeGenConfigBuilder setIncludeSubscriptions(Set<String> includeSubscriptions) {
    this.includeSubscriptions = includeSubscriptions;
    return this;
  }

  public CodeGenConfigBuilder setSkipEntityQueries(boolean skipEntityQueries) {
    this.skipEntityQueries = skipEntityQueries;
    return this;
  }

  public CodeGenConfigBuilder setShortProjectionNames(boolean shortProjectionNames) {
    this.shortProjectionNames = shortProjectionNames;
    return this;
  }

  public CodeGenConfigBuilder setGenerateDataTypes(boolean generateDataTypes) {
    this.generateDataTypes = generateDataTypes;
    return this;
  }

  public CodeGenConfigBuilder setKotlinAllFieldsOptional(boolean kotlinAllFieldsOptional) {
    this.kotlinAllFieldsOptional = kotlinAllFieldsOptional;
    return this;
  }

  public CodeGenConfigBuilder setSnakeCaseConstantNames(boolean snakeCaseConstantNames) {
    this.snakeCaseConstantNames = snakeCaseConstantNames;
    return this;
  }

  public CodeGenConfigBuilder setGenerateInterfaceSetters(boolean generateInterfaceSetters) {
    this.generateInterfaceSetters = generateInterfaceSetters;
    return this;
  }

  public CodeGenConfigBuilder setGenerateInterfaceMethodsForInterfaceFields(
      boolean generateInterfaceMethodsForInterfaceFields) {
    this.generateInterfaceMethodsForInterfaceFields = generateInterfaceMethodsForInterfaceFields;
    return this;
  }

  public CodeGenConfigBuilder setGenerateDocs(boolean generateDocs) {
    this.generateDocs = generateDocs;
    return this;
  }

  public CodeGenConfigBuilder setGeneratedDocsFolder(Path generatedDocsFolder) {
    this.generatedDocsFolder = generatedDocsFolder;
    return this;
  }

  public CodeGenConfigBuilder setIncludeImports(Map<String, String> includeImports) {
    this.includeImports = includeImports;
    return this;
  }

  public CodeGenConfigBuilder setIncludeEnumImports(
      Map<String, Map<String, String>> includeEnumImports) {
    this.includeEnumImports = includeEnumImports;
    return this;
  }

  public CodeGenConfigBuilder setIncludeClassImports(
      Map<String, Map<String, String>> includeClassImports) {
    this.includeClassImports = includeClassImports;
    return this;
  }

  public CodeGenConfigBuilder setGenerateCustomAnnotations(boolean generateCustomAnnotations) {
    this.generateCustomAnnotations = generateCustomAnnotations;
    return this;
  }

  public CodeGenConfigBuilder setJavaGenerateAllConstructor(boolean javaGenerateAllConstructor) {
    this.javaGenerateAllConstructor = javaGenerateAllConstructor;
    return this;
  }

  public CodeGenConfigBuilder setImplementSerializable(boolean implementSerializable) {
    this.implementSerializable = implementSerializable;
    return this;
  }

  public CodeGenConfigBuilder setAddGeneratedAnnotation(boolean addGeneratedAnnotation) {
    this.addGeneratedAnnotation = addGeneratedAnnotation;
    return this;
  }

  public CodeGenConfigBuilder setDisableDatesInGeneratedAnnotation(
      boolean disableDatesInGeneratedAnnotation) {
    this.disableDatesInGeneratedAnnotation = disableDatesInGeneratedAnnotation;
    return this;
  }

  public CodeGenConfigBuilder setAddDeprecatedAnnotation(boolean addDeprecatedAnnotation) {
    this.addDeprecatedAnnotation = addDeprecatedAnnotation;
    return this;
  }

  public CodeGenConfigBuilder setTrackInputFieldSet(boolean trackInputFieldSet) {
    this.trackInputFieldSet = trackInputFieldSet;
    return this;
  }

  public CodeGenConfigBuilder setGenerateJSpecifyAnnotations(boolean generateJSpecifyAnnotations) {
    this.generateJSpecifyAnnotations = generateJSpecifyAnnotations;
    return this;
  }

  public CodeGenConfig build() {
    return new CodeGenConfig(
        schemas,
        schemaFiles,
        schemaJarFilesFromDependencies,
        outputDir,
        examplesOutputDir,
        writeToFiles,
        packageName,
        subPackageNameClient,
        subPackageNameDatafetchers,
        subPackageNameTypes,
        subPackageNameDocs,
        language,
        generateBoxedTypes,
        generateIsGetterForPrimitiveBooleanFields,
        generateClientApi,
        generateClientApiv2,
        generateInterfaces,
        generateKotlinNullableClasses,
        generateKotlinClosureProjections,
        typeMapping,
        includeQueries,
        includeMutations,
        includeSubscriptions,
        skipEntityQueries,
        shortProjectionNames,
        generateDataTypes,
        kotlinAllFieldsOptional,
        snakeCaseConstantNames,
        generateInterfaceSetters,
        generateInterfaceMethodsForInterfaceFields,
        generateDocs,
        generatedDocsFolder,
        includeImports,
        includeEnumImports,
        includeClassImports,
        generateCustomAnnotations,
        javaGenerateAllConstructor,
        implementSerializable,
        addGeneratedAnnotation,
        disableDatesInGeneratedAnnotation,
        addDeprecatedAnnotation,
        trackInputFieldSet,
        generateJSpecifyAnnotations);
  }
}
