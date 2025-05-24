package io.github.deweyjose.paramcodegen;

import com.squareup.javapoet.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
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
public class BuilderCodegen extends AbstractMojo {

  public static final String CODEGENCONFIG_URL =
      "https://raw.githubusercontent.com/Netflix/dgs-codegen/master/graphql-dgs-codegen-core/src/main/kotlin/com/netflix/graphql/dgs/codegen/CodeGen.kt";

  @Parameter(
      property = "buildercodegen.outputDirectory",
      defaultValue = "${project.build.directory}/generated-sources/buildercodegen")
  private String outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Builder Code Generator Plugin");
    getLog().info("Output Directory: " + outputDirectory);

    try {
      String codeGenConfig = downloadCodeGenConfig(CODEGENCONFIG_URL);
      KtParameter[] params = parseCodeGenConfigParameters(codeGenConfig);
      generateBuilderClass(params, outputDirectory);
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to generate builder from CodeGenConfig.kt", e);
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
   * Generate the builder class for CodeGenConfig.
   *
   * @param params The parameters to generate the builder class for.
   */
  public static void generateBuilderClass(KtParameter[] params, String outputDir)
      throws IOException {
    // Generate the builder class for CodeGenConfig
    TypeSpec.Builder builderClass =
        TypeSpec.classBuilder("GeneratedCodeGenConfigBuilder")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Auto-generated by BuilderCodegen. DO NOT EDIT.\n");

    // Add fields and setters for each parameter
    for (KtParameter param : params) {
      String name = param.getName();
      String kotlinType = param.getTypeReference().getText();
      TypeName javaType = mapKotlinTypeToJavaType(kotlinType);
      FieldSpec field = FieldSpec.builder(javaType, name, Modifier.PRIVATE).build();
      builderClass.addField(field);
      // Setter
      MethodSpec setter =
          MethodSpec.methodBuilder("set" + capitalize(name))
              .addModifiers(Modifier.PUBLIC)
              .returns(
                  ClassName.get(
                      "io.github.deweyjose.codegen.generated", "GeneratedCodeGenConfigBuilder"))
              .addParameter(javaType, name)
              .addStatement("this.$N = $N", name, name)
              .addStatement("return this")
              .build();
      builderClass.addMethod(setter);
    }

    // Build method
    MethodSpec.Builder buildMethod =
        MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(ClassName.get("com.netflix.graphql.dgs.codegen", "CodeGenConfig"));
    StringBuilder ctorArgs = new StringBuilder();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) ctorArgs.append(", ");
      ctorArgs.append(params[i].getName());
    }
    buildMethod.addStatement("return new CodeGenConfig(" + ctorArgs + ")");
    builderClass.addMethod(buildMethod.build());

    JavaFile.builder("io.github.deweyjose.codegen.generated", builderClass.build())
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

  // Utility: capitalize
  private static String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
