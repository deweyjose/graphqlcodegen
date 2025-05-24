package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;

class CodegenExecutorExecuteTest {

  @Test
  void integrationTest_generateCodeFromSchema() throws java.io.IOException {
    // Arrange
    File schemaDir = new File(getClass().getClassLoader().getResource("schema").getFile());
    File[] schemaPaths = {schemaDir};
    File outputDir = new File("target/generated-test-codegen");

    CodegenConfigProvider config =
        new CodegenConfigProvider() {
          public File[] getSchemaPaths() {
            return schemaPaths;
          }

          public String[] getSchemaJarFilesFromDependencies() {
            return new String[0];
          }

          public File getSchemaManifestOutputDir() {
            return outputDir;
          }

          public boolean isOnlyGenerateChanged() {
            return false;
          }

          public String[] getTypeMappingPropertiesFiles() {
            return new String[0];
          }

          public boolean isSkip() {
            return false;
          }

          public File getOutputDir() {
            return outputDir;
          }

          public File getExamplesOutputDir() {
            return outputDir;
          }

          public boolean isWriteToFiles() {
            return true;
          }

          public String getPackageName() {
            return "com.example";
          }

          public String getSubPackageNameClient() {
            return "client";
          }

          public String getSubPackageNameDatafetchers() {
            return "datafetchers";
          }

          public String getSubPackageNameTypes() {
            return "types";
          }

          public String getSubPackageNameDocs() {
            return "docs";
          }

          public String getLanguage() {
            return "java";
          }

          public Map<String, String> getTypeMapping() {
            return new HashMap<>();
          }

          public boolean isGenerateBoxedTypes() {
            return false;
          }

          public boolean isGenerateIsGetterForPrimitiveBooleanFields() {
            return false;
          }

          public boolean isGenerateClientApi() {
            return false;
          }

          public boolean isGenerateClientApiv2() {
            return false;
          }

          public boolean isGenerateInterfaces() {
            return false;
          }

          public boolean isGenerateKotlinNullableClasses() {
            return false;
          }

          public boolean isGenerateKotlinClosureProjections() {
            return false;
          }

          public String[] getIncludeQueries() {
            return new String[0];
          }

          public String[] getIncludeMutations() {
            return new String[0];
          }

          public String[] getIncludeSubscriptions() {
            return new String[0];
          }

          public boolean isSkipEntityQueries() {
            return false;
          }

          public boolean isShortProjectionNames() {
            return false;
          }

          public boolean isGenerateDataTypes() {
            return true;
          }

          public boolean isOmitNullInputFields() {
            return false;
          }

          public int getMaxProjectionDepth() {
            return 10;
          }

          public boolean isKotlinAllFieldsOptional() {
            return false;
          }

          public boolean isSnakeCaseConstantNames() {
            return false;
          }

          public boolean isGenerateInterfaceSetters() {
            return false;
          }

          public boolean isGenerateInterfaceMethodsForInterfaceFields() {
            return false;
          }

          public Boolean getGenerateDocs() {
            return false;
          }

          public String getGeneratedDocsFolder() {
            return "generated-docs";
          }

          public boolean isJavaGenerateAllConstructor() {
            return false;
          }

          public boolean isImplementSerializable() {
            return false;
          }

          public boolean isAddGeneratedAnnotation() {
            return false;
          }

          public boolean isAddDeprecatedAnnotation() {
            return false;
          }

          public boolean isTrackInputFieldSet() {
            return false;
          }

          public boolean isGenerateCustomAnnotations() {
            return false;
          }

          public Map<String, String> getIncludeImports() {
            return new HashMap<>();
          }

          public Map<String, ParameterMap> getIncludeEnumImports() {
            return new HashMap<>();
          }

          public Map<String, ParameterMap> getIncludeClassImports() {
            return new HashMap<>();
          }

          public boolean isDisableDatesInGeneratedAnnotation() {
            return false;
          }
        };
    Log log = org.mockito.Mockito.mock(Log.class);
    CodegenExecutor executor = new CodegenExecutor(log);
    executor.execute(config, new HashSet<>(), new File("."));

    // Assert that code generation produced output files
    assertTrue(outputDir.exists() && outputDir.isDirectory(), "Output directory should exist");
    File[] generatedFiles = outputDir.listFiles();
    assertNotNull(generatedFiles, "Output directory should not be empty");
    assertTrue(
        generatedFiles.length > 0, "There should be generated files in the output directory");

    // Check for specific generated type files and their content
    File typesDir = new File(outputDir, "com/example/types");
    assertTrue(typesDir.exists() && typesDir.isDirectory(), "Types directory should exist");
    String[] expectedTypeFiles = {"Show.java", "ShowInput.java", "Foo.java", "Actor.java"};
    for (String fileName : expectedTypeFiles) {
      File f = new File(typesDir, fileName);
      assertTrue(f.exists(), fileName + " should be generated");
      String content = java.nio.file.Files.readString(f.toPath());
      String className = fileName.replace(".java", "");
      assertTrue(
          content.contains("public class " + className),
          fileName + " should contain class definition");
    }

    // Check for datafetcher files
    File datafetchersDir = new File(outputDir, "com/example/datafetchers");
    assertTrue(
        datafetchersDir.exists() && datafetchersDir.isDirectory(),
        "Datafetchers directory should exist");
    String[] expectedDatafetcherFiles = {"BarsDatafetcher.java", "ShowsDatafetcher.java"};
    for (String fileName : expectedDatafetcherFiles) {
      File f = new File(datafetchersDir, fileName);
      assertTrue(f.exists(), fileName + " should be generated");
      String content = java.nio.file.Files.readString(f.toPath());
      String className = fileName.replace(".java", "");
      assertTrue(
          content.contains("public class " + className),
          fileName + " should contain class definition");
    }
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
