package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.util.Map;

public interface CodegenConfigProvider {
  File[] getSchemaPaths();

  String[] getSchemaJarFilesFromDependencies();

  File getSchemaManifestOutputDir();

  boolean isOnlyGenerateChanged();

  String[] getTypeMappingPropertiesFiles();

  boolean isSkip();

  File getOutputDir();

  File getExamplesOutputDir();

  boolean isWriteToFiles();

  String getPackageName();

  String getSubPackageNameClient();

  String getSubPackageNameDatafetchers();

  String getSubPackageNameTypes();

  String getSubPackageNameDocs();

  String getLanguage();

  Map<String, String> getTypeMapping();

  boolean isGenerateBoxedTypes();

  boolean isGenerateIsGetterForPrimitiveBooleanFields();

  boolean isGenerateClientApi();

  boolean isGenerateClientApiv2();

  boolean isGenerateInterfaces();

  boolean isGenerateKotlinNullableClasses();

  boolean isGenerateKotlinClosureProjections();

  String[] getIncludeQueries();

  String[] getIncludeMutations();

  String[] getIncludeSubscriptions();

  boolean isSkipEntityQueries();

  boolean isShortProjectionNames();

  boolean isGenerateDataTypes();

  boolean isOmitNullInputFields();

  int getMaxProjectionDepth();

  boolean isKotlinAllFieldsOptional();

  boolean isSnakeCaseConstantNames();

  boolean isGenerateInterfaceSetters();

  boolean isGenerateInterfaceMethodsForInterfaceFields();

  Boolean getGenerateDocs();

  String getGeneratedDocsFolder();

  boolean isJavaGenerateAllConstructor();

  boolean isImplementSerializable();

  boolean isAddGeneratedAnnotation();

  boolean isAddDeprecatedAnnotation();

  boolean isTrackInputFieldSet();

  boolean isGenerateCustomAnnotations();

  Map<String, String> getIncludeImports();

  Map<String, ParameterMap> getIncludeEnumImports();

  Map<String, ParameterMap> getIncludeClassImports();

  boolean isDisableDatesInGeneratedAnnotation();
}
