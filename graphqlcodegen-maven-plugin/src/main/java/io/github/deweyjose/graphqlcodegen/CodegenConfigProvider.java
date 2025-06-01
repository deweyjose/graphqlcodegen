package io.github.deweyjose.graphqlcodegen;

import io.github.deweyjose.graphqlcodegen.parameters.IntrospectionRequest;
import io.github.deweyjose.graphqlcodegen.parameters.ParameterMap;
import java.io.File;
import java.util.List;
import java.util.Map;

/** Interface for providing configuration to the GraphQL codegen plugin. */
public interface CodegenConfigProvider {
  /**
   * @return schema file paths
   */
  File[] getSchemaPaths();

  /**
   * @return schema jar files from dependencies
   */
  String[] getSchemaJarFilesFromDependencies();

  /**
   * @return output directory for schema manifest
   */
  File getSchemaManifestOutputDir();

  /**
   * @return whether to only generate changed files
   */
  boolean isOnlyGenerateChanged();

  /**
   * @return type mapping properties files
   */
  String[] getTypeMappingPropertiesFiles();

  /**
   * @return whether to skip code generation
   */
  boolean isSkip();

  /**
   * @return output directory
   */
  File getOutputDir();

  /**
   * @return examples output directory
   */
  File getExamplesOutputDir();

  /**
   * @return whether to write to files
   */
  boolean isWriteToFiles();

  /**
   * @return package name
   */
  String getPackageName();

  /**
   * @return subpackage name for client
   */
  String getSubPackageNameClient();

  /**
   * @return subpackage name for datafetchers
   */
  String getSubPackageNameDatafetchers();

  /**
   * @return subpackage name for types
   */
  String getSubPackageNameTypes();

  /**
   * @return subpackage name for docs
   */
  String getSubPackageNameDocs();

  /**
   * @return language
   */
  String getLanguage();

  /**
   * @return type mapping
   */
  Map<String, String> getTypeMapping();

  /**
   * @return whether to generate boxed types
   */
  boolean isGenerateBoxedTypes();

  /**
   * @return whether to generate is-getter for primitive boolean fields
   */
  boolean isGenerateIsGetterForPrimitiveBooleanFields();

  /**
   * @return whether to generate client API
   */
  boolean isGenerateClientApi();

  /**
   * @return whether to generate client API v2
   */
  boolean isGenerateClientApiv2();

  /**
   * @return whether to generate interfaces
   */
  boolean isGenerateInterfaces();

  /**
   * @return whether to generate Kotlin nullable classes
   */
  boolean isGenerateKotlinNullableClasses();

  /**
   * @return whether to generate Kotlin closure projections
   */
  boolean isGenerateKotlinClosureProjections();

  /**
   * @return included queries
   */
  String[] getIncludeQueries();

  /**
   * @return included mutations
   */
  String[] getIncludeMutations();

  /**
   * @return included subscriptions
   */
  String[] getIncludeSubscriptions();

  /**
   * @return whether to skip entity queries
   */
  boolean isSkipEntityQueries();

  /**
   * @return whether to use short projection names
   */
  boolean isShortProjectionNames();

  /**
   * @return whether to generate data types
   */
  boolean isGenerateDataTypes();

  /**
   * @return whether to omit null input fields
   */
  boolean isOmitNullInputFields();

  /**
   * @return max projection depth
   */
  int getMaxProjectionDepth();

  /**
   * @return whether all Kotlin fields are optional
   */
  boolean isKotlinAllFieldsOptional();

  /**
   * @return whether to use snake case constant names
   */
  boolean isSnakeCaseConstantNames();

  /**
   * @return whether to generate interface setters
   */
  boolean isGenerateInterfaceSetters();

  /**
   * @return whether to generate interface methods for interface fields
   */
  boolean isGenerateInterfaceMethodsForInterfaceFields();

  /**
   * @return whether to generate docs
   */
  Boolean getGenerateDocs();

  /**
   * @return generated docs folder
   */
  String getGeneratedDocsFolder();

  /**
   * @return whether to generate all Java constructors
   */
  boolean isJavaGenerateAllConstructor();

  /**
   * @return whether to implement Serializable
   */
  boolean isImplementSerializable();

  /**
   * @return whether to add @Generated annotation
   */
  boolean isAddGeneratedAnnotation();

  /**
   * @return whether to add @Deprecated annotation
   */
  boolean isAddDeprecatedAnnotation();

  /**
   * @return whether to track input field set
   */
  boolean isTrackInputFieldSet();

  /**
   * @return whether to generate custom annotations
   */
  boolean isGenerateCustomAnnotations();

  /**
   * @return included imports
   */
  Map<String, String> getIncludeImports();

  /**
   * @return included enum imports
   */
  Map<String, ParameterMap> getIncludeEnumImports();

  /**
   * @return included class imports
   */
  Map<String, ParameterMap> getIncludeClassImports();

  /**
   * @return whether to disable dates in @Generated annotation
   */
  boolean isDisableDatesInGeneratedAnnotation();

  /**
   * @return remote schema URLs
   */
  String[] getSchemaUrls();

  /**
   * @return whether to auto add source
   */
  boolean isAutoAddSource();

  List<IntrospectionRequest> getIntrospectionRequests();
}
