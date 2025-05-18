package io.github.deweyjose.graphqlcodegen.models;

import com.netflix.graphql.dgs.codegen.Language;
import io.github.deweyjose.graphqlcodegen.Properties;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record DgsParameters(
    Set<File> schemaPaths,
    Set<File> fullSchemaPaths,
    List<File> dependencySchemas,
    Path outputDir,
    Path examplesOutputDir,
    boolean writeToFiles,
    String packageName,
    String subPackageNameClient,
    String subPackageNameDatafetchers,
    String subPackageNameTypes,
    String subPackageNameDocs,
    Language language,
    boolean generateBoxedTypes,
    boolean generateIsGetterForPrimitiveBooleanFields,
    boolean generateClientApi,
    boolean generateClientApiv2,
    boolean generateInterfaces,
    boolean generateKotlinNullableClasses,
    boolean generateKotlinClosureProjections,
    Map<String, String> typeMapping,
    Set<String> includeQueries,
    Set<String> includeMutations,
    Set<String> includeSubscriptions,
    boolean skipEntityQueries,
    boolean shortProjectionNames,
    boolean generateDataTypes,
    boolean omitNullInputFields,
    int maxProjectionDepth,
    boolean kotlinAllFieldsOptional,
    boolean snakeCaseConstantNames,
    boolean generateInterfaceSetters,
    boolean generateInterfaceMethodsForInterfaceFields,
    Boolean generateDocs,
    Path generatedDocsFolder,
    boolean generateCustomAnnotations,
    boolean javaGenerateAllConstructor,
    boolean implementSerializable,
    boolean addGeneratedAnnotation,
    boolean disableDatesInGeneratedAnnotation,
    boolean addDeprecatedAnnotation,
    boolean trackInputFieldSet,
    Map<String, String> includeImports,
    Map<String, Properties> includeEnumImports,
    Map<String, Properties> includeClassImports) {}
