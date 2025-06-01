package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestCodegenProvider implements CodegenConfigProvider {
  private File[] schemaPaths = new File[0];
  private String[] schemaJarFilesFromDependencies = new String[0];
  private File schemaManifestOutputDir = new File("target/test-schema-manifest");
  private boolean onlyGenerateChanged = false;
  private String[] typeMappingPropertiesFiles = new String[0];
  private boolean skip = false;
  private File outputDir = new File("target/generated-test-codegen");
  private File examplesOutputDir = outputDir;
  private boolean writeToFiles = true;
  private String packageName = "com.example";
  private String subPackageNameClient = "client";
  private String subPackageNameDatafetchers = "datafetchers";
  private String subPackageNameTypes = "types";
  private String subPackageNameDocs = "docs";
  private String language = "java";
  private Map<String, String> typeMapping = new HashMap<>();
  private boolean generateBoxedTypes = false;
  private boolean generateIsGetterForPrimitiveBooleanFields = false;
  private boolean generateClientApi = false;
  private boolean generateClientApiv2 = false;
  private boolean generateInterfaces = false;
  private boolean generateKotlinNullableClasses = false;
  private boolean generateKotlinClosureProjections = false;
  private String[] includeQueries = new String[0];
  private String[] includeMutations = new String[0];
  private String[] includeSubscriptions = new String[0];
  private boolean skipEntityQueries = false;
  private boolean shortProjectionNames = false;
  private boolean generateDataTypes = true;
  private boolean omitNullInputFields = false;
  private int maxProjectionDepth = 10;
  private boolean kotlinAllFieldsOptional = false;
  private boolean snakeCaseConstantNames = false;
  private boolean generateInterfaceSetters = false;
  private boolean generateInterfaceMethodsForInterfaceFields = false;
  private Boolean generateDocs = false;
  private String generatedDocsFolder = "generated-docs";
  private boolean javaGenerateAllConstructor = false;
  private boolean implementSerializable = false;
  private boolean addGeneratedAnnotation = false;
  private boolean addDeprecatedAnnotation = false;
  private boolean trackInputFieldSet = false;
  private boolean generateCustomAnnotations = false;
  private Map<String, String> includeImports = new HashMap<>();
  private Map<String, ParameterMap> includeEnumImports = new HashMap<>();
  private Map<String, ParameterMap> includeClassImports = new HashMap<>();
  private boolean disableDatesInGeneratedAnnotation = false;
  private String[] schemaUrls = new String[0];

  // Setters for test customization
  public void setSchemaPaths(File[] schemaPaths) {
    this.schemaPaths = schemaPaths;
  }

  public void setOutputDir(File outputDir) {
    this.outputDir = outputDir;
    this.examplesOutputDir = outputDir;
  }

  public void setSchemaManifestOutputDir(File dir) {
    this.schemaManifestOutputDir = dir;
  }

  public void setSchemaUrls(String[] urls) {
    this.schemaUrls = urls;
  }

  public void setOnlyGenerateChanged(boolean b) {
    this.onlyGenerateChanged = b;
  }

  // Add more setters as needed

  @Override
  public File[] getSchemaPaths() {
    return schemaPaths;
  }

  @Override
  public String[] getSchemaJarFilesFromDependencies() {
    return schemaJarFilesFromDependencies;
  }

  @Override
  public File getSchemaManifestOutputDir() {
    return schemaManifestOutputDir;
  }

  @Override
  public boolean isOnlyGenerateChanged() {
    return onlyGenerateChanged;
  }

  @Override
  public String[] getTypeMappingPropertiesFiles() {
    return typeMappingPropertiesFiles;
  }

  @Override
  public boolean isSkip() {
    return skip;
  }

  @Override
  public File getOutputDir() {
    return outputDir;
  }

  @Override
  public File getExamplesOutputDir() {
    return examplesOutputDir;
  }

  @Override
  public boolean isWriteToFiles() {
    return writeToFiles;
  }

  @Override
  public String getPackageName() {
    return packageName;
  }

  @Override
  public String getSubPackageNameClient() {
    return subPackageNameClient;
  }

  @Override
  public String getSubPackageNameDatafetchers() {
    return subPackageNameDatafetchers;
  }

  @Override
  public String getSubPackageNameTypes() {
    return subPackageNameTypes;
  }

  @Override
  public String getSubPackageNameDocs() {
    return subPackageNameDocs;
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public Map<String, String> getTypeMapping() {
    return typeMapping;
  }

  @Override
  public boolean isGenerateBoxedTypes() {
    return generateBoxedTypes;
  }

  @Override
  public boolean isGenerateIsGetterForPrimitiveBooleanFields() {
    return generateIsGetterForPrimitiveBooleanFields;
  }

  @Override
  public boolean isGenerateClientApi() {
    return generateClientApi;
  }

  @Override
  public boolean isGenerateClientApiv2() {
    return generateClientApiv2;
  }

  @Override
  public boolean isGenerateInterfaces() {
    return generateInterfaces;
  }

  @Override
  public boolean isGenerateKotlinNullableClasses() {
    return generateKotlinNullableClasses;
  }

  @Override
  public boolean isGenerateKotlinClosureProjections() {
    return generateKotlinClosureProjections;
  }

  @Override
  public String[] getIncludeQueries() {
    return includeQueries;
  }

  @Override
  public String[] getIncludeMutations() {
    return includeMutations;
  }

  @Override
  public String[] getIncludeSubscriptions() {
    return includeSubscriptions;
  }

  @Override
  public boolean isSkipEntityQueries() {
    return skipEntityQueries;
  }

  @Override
  public boolean isShortProjectionNames() {
    return shortProjectionNames;
  }

  @Override
  public boolean isGenerateDataTypes() {
    return generateDataTypes;
  }

  @Override
  public boolean isOmitNullInputFields() {
    return omitNullInputFields;
  }

  @Override
  public int getMaxProjectionDepth() {
    return maxProjectionDepth;
  }

  @Override
  public boolean isKotlinAllFieldsOptional() {
    return kotlinAllFieldsOptional;
  }

  @Override
  public boolean isSnakeCaseConstantNames() {
    return snakeCaseConstantNames;
  }

  @Override
  public boolean isGenerateInterfaceSetters() {
    return generateInterfaceSetters;
  }

  @Override
  public boolean isGenerateInterfaceMethodsForInterfaceFields() {
    return generateInterfaceMethodsForInterfaceFields;
  }

  @Override
  public Boolean getGenerateDocs() {
    return generateDocs;
  }

  @Override
  public String getGeneratedDocsFolder() {
    return generatedDocsFolder;
  }

  @Override
  public boolean isJavaGenerateAllConstructor() {
    return javaGenerateAllConstructor;
  }

  @Override
  public boolean isImplementSerializable() {
    return implementSerializable;
  }

  @Override
  public boolean isAddGeneratedAnnotation() {
    return addGeneratedAnnotation;
  }

  @Override
  public boolean isAddDeprecatedAnnotation() {
    return addDeprecatedAnnotation;
  }

  @Override
  public boolean isTrackInputFieldSet() {
    return trackInputFieldSet;
  }

  @Override
  public boolean isGenerateCustomAnnotations() {
    return generateCustomAnnotations;
  }

  @Override
  public Map<String, String> getIncludeImports() {
    return includeImports;
  }

  @Override
  public Map<String, ParameterMap> getIncludeEnumImports() {
    return includeEnumImports;
  }

  @Override
  public Map<String, ParameterMap> getIncludeClassImports() {
    return includeClassImports;
  }

  @Override
  public boolean isDisableDatesInGeneratedAnnotation() {
    return disableDatesInGeneratedAnnotation;
  }

  @Override
  public String[] getSchemaUrls() {
    return schemaUrls;
  }
}
