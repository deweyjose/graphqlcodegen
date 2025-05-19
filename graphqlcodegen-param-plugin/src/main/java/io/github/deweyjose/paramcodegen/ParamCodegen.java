package io.github.deweyjose.paramcodegen;

import com.squareup.javapoet.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.com.intellij.mock.MockProject;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtPrimaryConstructor;

// This class is a Maven Mojo that fetches CodeGenConfig.kt from GitHub, parses its constructor, and
// will generate Java code for parameter mapping. See tests in
// src/test/java/io/github/deweyjose/paramcodegen/ParamCodegenTest.java.
@Mojo(name = "generate")
public class ParamCodegen extends AbstractMojo {

  public static final String CODEGENCONFIG_URL =
      "https://raw.githubusercontent.com/Netflix/dgs-codegen/master/graphql-dgs-codegen-core/src/main/kotlin/com/netflix/graphql/dgs/codegen/CodeGen.kt";

  @Parameter(
      property = "paramcodegen.outputDirectory",
      defaultValue = "${project.build.directory}/generated-sources/paramcodegen")
  private String outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Parameter Code Generator Plugin");
    getLog().info("Output Directory: " + outputDirectory);

    try {
      String codeGenConfig = downloadCodeGenConfig(CODEGENCONFIG_URL);
      KtParameter[] params = parseCodeGenConfigParameters(codeGenConfig);
      generateAutoCodegen(params, outputDirectory);
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to generate parameters from CodeGenConfig.kt", e);
    }
  }

  /**
   * Download the CodeGenConfig.kt file from the given URL.
   *
   * @param url The URL to download the CodeGenConfig.kt file from.
   * @return The CodeGenConfig.kt file as a string.
   * @throws IOException If there is an error downloading the file.
   * @throws InterruptedException If the download is interrupted.
   * @return The CodeGenConfig.kt file as a string.
   */
  public static String downloadCodeGenConfig(String url) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    String body = response.body();
    return body;
  }

  /**
   * Parse the CodeGenConfig.kt file and return the parameters.
   *
   * @param code The CodeGenConfig.kt file as a string.
   * @return The parameters as an array of KtParameter.
   */
  public static KtParameter[] parseCodeGenConfigParameters(String code) {
    CompilerConfiguration configuration = new CompilerConfiguration();
    KotlinCoreEnvironment environment =
        KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(), configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES);
    KtFile ktFile =
        org.jetbrains.kotlin.psi.KtPsiFactoryKt.KtPsiFactory(
                (MockProject) environment.getProject(), false)
            .createFile("CodeGenConfig.kt", code);
    KtClass codeGenConfigClass = null;
    for (PsiElement child : ktFile.getChildren()) {
      if (child instanceof KtClass) {
        KtClass ktClass = (KtClass) child;
        if ("CodeGenConfig".equals(ktClass.getName())) {
          codeGenConfigClass = ktClass;
          break;
        }
      }
    }
    if (codeGenConfigClass == null) {
      throw new IllegalArgumentException("CodeGenConfig class not found in source");
    }
    KtPrimaryConstructor ctor = codeGenConfigClass.getPrimaryConstructor();
    if (ctor == null) {
      throw new IllegalArgumentException("CodeGenConfig class has no primary constructor");
    }

    List<KtParameter> params = ctor.getValueParameters();
    return params.toArray(new KtParameter[0]);
  }

  /**
   * Generate the auto-generated code for the parameters.
   *
   * @param params The parameters to generate the code for.
   */
  public static void generateAutoCodegen(KtParameter[] params, String outputDir)
      throws IOException {
    // 1. Generate AutoCustomParameters class
    TypeSpec.Builder customClass =
        TypeSpec.classBuilder("AutoCustomParameters")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Auto-generated by ParamCodegen. DO NOT EDIT.\n");
    List<FieldSpec> customFields =
        List.of(
            FieldSpec.builder(
                    ClassName.get("org.apache.maven.project", "MavenProject"),
                    "project",
                    Modifier.PUBLIC)
                .build(),
            FieldSpec.builder(ArrayTypeName.of(File.class), "schemaPaths", Modifier.PUBLIC).build(),
            FieldSpec.builder(ClassName.get(File.class), "schemaManifestOutputDir", Modifier.PUBLIC)
                .build(),
            FieldSpec.builder(TypeName.BOOLEAN, "onlyGenerateChanged", Modifier.PUBLIC).build(),
            FieldSpec.builder(
                    ArrayTypeName.of(String.class), "typeMappingPropertiesFiles", Modifier.PUBLIC)
                .build());

    for (FieldSpec f : customFields) {
      customClass.addField(f);
    }

    MethodSpec.Builder customCtor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    for (FieldSpec f : customFields) {
      customCtor.addParameter(f.type, f.name);
      customCtor.addStatement("this.$N = $N", f.name, f.name);
    }
    customClass.addMethod(customCtor.build());
    JavaFile.builder("io.github.deweyjose.codegen.generated", customClass.build())
        .build()
        .writeTo(new File(outputDir));

    // 2. Generate AutoDgsParameters class
    Set<String> customFieldNames =
        Set.of(
            "project",
            "schemaPaths",
            "schemaManifestOutputDir",
            "onlyGenerateChanged",
            "typeMappingPropertiesFiles");
    List<FieldSpec> dgsFields = new java.util.ArrayList<>();
    List<String> dgsFieldNames = new java.util.ArrayList<>();

    for (KtParameter param : params) {
      String name = param.getName();
      if (customFieldNames.contains(name)) {
        throw new IllegalArgumentException(
            "Parameter " + name + " is already defined in AutoCustomParameters");
      }
      dgsFieldNames.add(name);
      String kotlinType = param.getTypeReference().getText();
      TypeName javaType = mapKotlinTypeToJavaType(kotlinType);
      dgsFields.add(FieldSpec.builder(javaType, name, Modifier.PUBLIC).build());
    }

    TypeSpec.Builder dgsClass =
        TypeSpec.classBuilder("AutoDgsParameters")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc("Auto-generated by ParamCodegen. DO NOT EDIT.\n");

    for (FieldSpec f : dgsFields) {
      dgsClass.addField(f);
    }

    MethodSpec.Builder dgsCtor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    for (FieldSpec f : dgsFields) {
      dgsCtor.addParameter(f.type, f.name);
      dgsCtor.addStatement("this.$N = $N", f.name, f.name);
    }
    dgsClass.addMethod(dgsCtor.build());
    JavaFile.builder("io.github.deweyjose.codegen.generated", dgsClass.build())
        .build()
        .writeTo(new File(outputDir));

    // 3. Generate AutoExecutionRequest class
    ClassName autoCustomParameters =
        ClassName.get("io.github.deweyjose.codegen.generated", "AutoCustomParameters");
    ClassName autoDgsParameters =
        ClassName.get("io.github.deweyjose.codegen.generated", "AutoDgsParameters");
    FieldSpec customField =
        FieldSpec.builder(autoCustomParameters, "customParameters", Modifier.PUBLIC, Modifier.FINAL)
            .build();
    FieldSpec dgsField =
        FieldSpec.builder(autoDgsParameters, "dgsParameters", Modifier.PUBLIC, Modifier.FINAL)
            .build();
    TypeSpec.Builder execClass =
        TypeSpec.classBuilder("AutoExecutionRequest")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc("Auto-generated by ParamCodegen. DO NOT EDIT.\n")
            .addField(customField)
            .addField(dgsField);
    MethodSpec.Builder execCtor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(autoCustomParameters, "customParameters")
            .addParameter(autoDgsParameters, "dgsParameters")
            .addStatement("this.customParameters = customParameters")
            .addStatement("this.dgsParameters = dgsParameters");
    execClass.addMethod(execCtor.build());
    JavaFile.builder("io.github.deweyjose.codegen.generated", execClass.build())
        .build()
        .writeTo(new File(outputDir));

    // 4. Generate AutoCodegen class
    ClassName abstractMojo = ClassName.get("org.apache.maven.plugin", "AbstractMojo");
    ClassName parameterAnn = ClassName.get("org.apache.maven.plugins.annotations", "Parameter");
    ClassName autoExecutionRequest =
        ClassName.get("io.github.deweyjose.codegen.generated", "AutoExecutionRequest");
    ClassName codegenExecutor =
        ClassName.get("io.github.deweyjose.graphqlcodegen", "CodegenExecutor");
    ClassName artifact = ClassName.get("org.apache.maven.artifact", "Artifact");
    ClassName mavenProject = ClassName.get("org.apache.maven.project", "MavenProject");
    ClassName setClass = ClassName.get("java.util", "Set");
    ClassName hashSetClass = ClassName.get("java.util", "HashSet");
    ClassName languageEnum = ClassName.get("com.netflix.graphql.dgs.codegen", "Language");

    TypeSpec.Builder autoCodegen =
        TypeSpec.classBuilder("AutoCodegen")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Auto-generated by ParamCodegen. DO NOT EDIT.\n")
            .superclass(abstractMojo);

    // Add Maven @Parameter fields for all custom parameters
    for (FieldSpec f : customFields) {
      AnnotationSpec.Builder paramAnn = AnnotationSpec.builder(parameterAnn);
      if (f.name.equals("project")) {
        paramAnn.addMember("defaultValue", "$S", "${project}");
      } else if (f.name.equals("schemaPaths")) {
        paramAnn.addMember("property", "$S", "schemaPaths");
        paramAnn.addMember("defaultValue", "$S", "${project.basedir}/src/main/resources/schema");
      } else if (f.name.equals("schemaManifestOutputDir")) {
        paramAnn.addMember("property", "$S", "schemaManifestOutputDir");
        paramAnn.addMember("defaultValue", "$S", "${project.build.directory}/graphqlcodegen");
      } else if (f.name.equals("onlyGenerateChanged")) {
        paramAnn.addMember("property", "$S", "onlyGenerateChanged");
        paramAnn.addMember("defaultValue", "$S", "true");
      } else if (f.name.equals("typeMappingPropertiesFiles")) {
        paramAnn.addMember("property", "$S", "typeMappingPropertiesFiles");
      }
      autoCodegen.addField(f.toBuilder().addAnnotation(paramAnn.build()).build());
    }
    // Add skip parameter
    autoCodegen.addField(
        FieldSpec.builder(TypeName.BOOLEAN, "skip", Modifier.PRIVATE)
            .addAnnotation(
                AnnotationSpec.builder(parameterAnn)
                    .addMember("property", "$S", "dgs.codegen.skip")
                    .addMember("defaultValue", "$S", "false")
                    .addMember("required", "$L", false)
                    .build())
            .build());

    // Add Maven @Parameter fields for all DGS parameters (skipping duplicates)
    for (FieldSpec f : dgsFields) {
      AnnotationSpec.Builder paramAnn =
          AnnotationSpec.builder(parameterAnn).addMember("property", "$S", f.name);
      autoCodegen.addField(f.toBuilder().addAnnotation(paramAnn.build()).build());
    }

    // Implement execute()
    MethodSpec.Builder exec =
        MethodSpec.methodBuilder("execute")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .beginControlFlow("if (skip)")
            .addStatement("return")
            .endControlFlow();

    // Construct AutoCustomParameters
    exec.addStatement(
        "$T custom = new $T(project, schemaPaths, schemaManifestOutputDir, onlyGenerateChanged, typeMappingPropertiesFiles)",
        autoCustomParameters,
        autoCustomParameters);

    exec.addStatement(
        "$T dgsLang = $T.valueOf(language.toString().toUpperCase())", languageEnum, languageEnum);

    // Construct AutoDgsParameters (with conversions as needed)
    StringBuilder dgsArgs = new StringBuilder();
    for (int i = 0; i < dgsFields.size(); i++) {
      String name = dgsFields.get(i).name;
      // Conversion logic for known types
      if (name.equals("outputDir")
          || name.equals("examplesOutputDir")
          || name.equals("generatedDocsFolder")) {
        dgsArgs.append(name).append(".toPath()");
      } else if (name.equals("language")) {
        dgsArgs.append("dgsLang");
      } else if (name.equals("typeMapping")) {
        dgsArgs.append("typeMapping == null ? new java.util.HashMap<>() : typeMapping");
      } else if (name.equals("includeQueries")
          || name.equals("includeMutations")
          || name.equals("includeSubscriptions")) {
        dgsArgs
            .append(name)
            .append(" == null ? new java.util.HashSet<>() : java.util.Arrays.stream(")
            .append(name)
            .append(").collect(java.util.stream.Collectors.toSet())");
      } else if (name.equals("includeImports")
          || name.equals("includeEnumImports")
          || name.equals("includeClassImports")) {
        dgsArgs.append(name).append(" == null ? new java.util.HashMap<>() : ").append(name);
      } else {
        dgsArgs.append(name);
      }
      if (i < dgsFields.size() - 1) dgsArgs.append(", ");
    }
    exec.addStatement("$T dgs = new $T(" + dgsArgs + ")", autoDgsParameters, autoDgsParameters);

    // Construct AutoExecutionRequest
    exec.addStatement(
        "$T request = new $T(custom, dgs)", autoExecutionRequest, autoExecutionRequest);

    // Create CodegenExecutor, get artifacts, call execute
    exec.addStatement("$T executor = new $T(getLog())", codegenExecutor, codegenExecutor);
    exec.addStatement("$T artifacts = new $T(project.getArtifacts())", setClass, hashSetClass);
    exec.addStatement("executor.execute(request, artifacts)");

    autoCodegen.addMethod(exec.build());

    JavaFile.builder("io.github.deweyjose.codegen.generated", autoCodegen.build())
        .build()
        .writeTo(new File(outputDir));
  }

  /**
   * Map the Kotlin type to a Java type.
   *
   * @param kotlinType The Kotlin type to map.
   * @return The Java type.
   */
  public static TypeName mapKotlinTypeToJavaType(String kotlinType) {
    switch (kotlinType) {
      case "String":
        return ClassName.get(String.class);
      case "Int":
        return TypeName.INT;
      case "Boolean":
        return TypeName.BOOLEAN;
      case "Double":
        return TypeName.DOUBLE;
      case "Float":
        return TypeName.FLOAT;
      case "Long":
        return TypeName.LONG;
      case "Set<String>":
      case "List<String>":
        return ArrayTypeName.of(String.class);
      case "Set<java.io.File>":
      case "List<java.io.File>":
        return ArrayTypeName.of(File.class);
      case "java.io.File":
        return ClassName.get("java.io", "File");
      case "java.nio.file.Path":
        return ClassName.get("java.nio.file", "Path");
      case "Map<String, String>":
        return ParameterizedTypeName.get(
            ClassName.get("java.util", "Map"),
            ClassName.get(String.class),
            ClassName.get(String.class));
      default:
        return ClassName.get(Object.class);
    }
  }
}
