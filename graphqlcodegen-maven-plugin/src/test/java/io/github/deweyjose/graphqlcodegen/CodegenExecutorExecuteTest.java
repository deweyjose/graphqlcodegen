package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;

import com.netflix.graphql.dgs.codegen.Language;
import io.github.deweyjose.graphqlcodegen.models.CustomParameters;
import io.github.deweyjose.graphqlcodegen.models.DgsParameters;
import io.github.deweyjose.graphqlcodegen.models.ExecutionRequest;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;

class CodegenExecutorExecuteTest {

  @Test
  void integrationTest_generateCodeFromSchema() {
    // Arrange
    File schemaDir = new File(getClass().getClassLoader().getResource("schema").getFile());
    File[] schemaPaths = {schemaDir};
    File outputDir = new File("target/generated-test-codegen");
    File baseDir = new File("target/generated-test-codegen");

    CustomParameters custom =
        new CustomParameters(
            schemaPaths,
            new String[0], // schemaJarFilesFromDependencies
            outputDir, // schemaManifestOutputDir
            false, // onlyGenerateChanged
            baseDir,
            new String[0] // typeMappingPropertiesFiles
            );

    DgsParameters dgs =
        new DgsParameters(
            Collections.emptySet(), // schemaPaths
            Collections.emptySet(), // fullSchemaPaths
            List.of(), // dependencySchemas
            outputDir.toPath(), // outputDir
            outputDir.toPath(), // examplesOutputDir
            true, // writeToFiles
            "com.example", // packageName
            "client", // subPackageNameClient
            "datafetchers", // subPackageNameDatafetchers
            "types", // subPackageNameTypes
            "docs", // subPackageNameDocs
            Language.JAVA, // language
            false, // generateBoxedTypes
            false, // generateIsGetterForPrimitiveBooleanFields
            false, // generateClientApi
            false, // generateClientApiv2
            false, // generateInterfaces
            false, // generateKotlinNullableClasses
            false, // generateKotlinClosureProjections
            new HashMap<>(), // typeMapping
            new HashSet<>(), // includeQueries
            new HashSet<>(), // includeMutations
            new HashSet<>(), // includeSubscriptions
            false, // skipEntityQueries
            false, // shortProjectionNames
            true, // generateDataTypes
            false, // omitNullInputFields
            10, // maxProjectionDepth
            false, // kotlinAllFieldsOptional
            false, // snakeCaseConstantNames
            false, // generateInterfaceSetters
            false, // generateInterfaceMethodsForInterfaceFields
            false, // generateDocs
            outputDir.toPath(), // generatedDocsFolder (use output dir as a safe default)
            false, // generateCustomAnnotations
            false, // javaGenerateAllConstructor
            false, // implementSerializable
            false, // addGeneratedAnnotation
            false, // disableDatesInGeneratedAnnotation
            false, // addDeprecatedAnnotation
            false, // trackInputFieldSet
            new HashMap<>(), // includeImports
            new HashMap<>(), // includeEnumImports
            new HashMap<>() // includeClassImports
            );

    ExecutionRequest request = new ExecutionRequest(custom, dgs);
    CodegenExecutor executor = new CodegenExecutor(new NoOpLog());

    // Act
    executor.execute(request, Collections.emptySet());

    // Assert
    File[] generatedFiles = outputDir.listFiles();
    assertNotNull(generatedFiles, "Output directory should not be null");
    assertTrue(generatedFiles.length > 0, "Should generate at least one file");

    File typesDir = new File(outputDir, "com/example/types");
    assertTrue(typesDir.exists() && typesDir.isDirectory(), "Types directory should exist");
    File showType = new File(typesDir, "Show.java");
    File fooType = new File(typesDir, "Foo.java");
    File actorType = new File(typesDir, "Actor.java");
    File showInputType = new File(typesDir, "ShowInput.java");
    assertTrue(showType.exists(), "Show.java should be generated in the types subpackage");
    assertTrue(fooType.exists(), "Foo.java should be generated in the types subpackage");
    assertTrue(actorType.exists(), "Actor.java should be generated in the types subpackage");
    assertTrue(showInputType.exists(), "ShowInput.java should be generated in the types subpackage");

    try {
      String showTypeContent = java.nio.file.Files.readString(showType.toPath());
      assertTrue(showTypeContent.contains("class Show"), "Show.java should declare class Show");
      String fooTypeContent = java.nio.file.Files.readString(fooType.toPath());
      assertTrue(fooTypeContent.contains("class Foo"), "Foo.java should declare class Foo");
      String actorTypeContent = java.nio.file.Files.readString(actorType.toPath());
      assertTrue(actorTypeContent.contains("class Actor"), "Actor.java should declare class Actor");
      String showInputTypeContent = java.nio.file.Files.readString(showInputType.toPath());
      assertTrue(showInputTypeContent.contains("class ShowInput"), "ShowInput.java should declare class ShowInput");
    } catch (java.io.IOException e) {
      fail("Failed to read generated type files: " + e.getMessage());
    }

    File clientDir = new File(outputDir, "com/example/client");
    File datafetchersDir = new File(outputDir, "com/example/datafetchers");
    assertTrue(clientDir.exists() && clientDir.isDirectory(), "Client directory should exist");
    assertTrue(datafetchersDir.exists() && datafetchersDir.isDirectory(), "Datafetchers directory should exist");
  }

  // Simple no-op logger for integration test
  static class NoOpLog implements Log {
    public void debug(CharSequence content) {}

    public void debug(CharSequence content, Throwable error) {}

    public void debug(Throwable error) {}

    public void info(CharSequence content) {}

    public void info(CharSequence content, Throwable error) {}

    public void info(Throwable error) {}

    public void warn(CharSequence content) {}

    public void warn(CharSequence content, Throwable error) {}

    public void warn(Throwable error) {}

    public void error(CharSequence content) {}

    public void error(CharSequence content, Throwable error) {}

    public void error(Throwable error) {}

    public boolean isDebugEnabled() {
      return false;
    }

    public boolean isInfoEnabled() {
      return false;
    }

    public boolean isWarnEnabled() {
      return false;
    }

    public boolean isErrorEnabled() {
      return false;
    }
  }
}
