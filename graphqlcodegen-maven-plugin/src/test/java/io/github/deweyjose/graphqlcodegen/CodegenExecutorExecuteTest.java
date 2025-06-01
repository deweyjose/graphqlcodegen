package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashSet;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;

class CodegenExecutorExecuteTest {

  @Test
  void integrationTest_generateCodeFromSchema() throws java.io.IOException {
    // Arrange
    File schemaDir = new File(getClass().getClassLoader().getResource("schema").getFile());
    File[] schemaPaths = {schemaDir};
    File outputDir = new File("target/generated-test-codegen");
    TestCodegenProvider config = new TestCodegenProvider();
    config.setSchemaPaths(schemaPaths);
    config.setOutputDir(outputDir);
    config.setSchemaManifestOutputDir(outputDir);
    Log log = new TestLog();
    SchemaFileService schemaFileService =
        new SchemaFileService(outputDir, new SchemaManifestService(outputDir, outputDir));
    TypeMappingService typeMappingService = new TypeMappingService();
    CodegenExecutor executor = new CodegenExecutor(log, schemaFileService, typeMappingService);
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

  @Test
  void integrationTest_generateCodeFromSchemaWithRemoteSchema() throws java.io.IOException {
    // Arrange
    File outputDir = new File("target/generated-test-codegen-remote");
    TestCodegenProvider config = new TestCodegenProvider();
    config.setOutputDir(outputDir);
    config.setSchemaManifestOutputDir(outputDir);
    config.setSchemaUrls(new String[] {TestUtils.TEST_SCHEMA_URL});
    config.setOnlyGenerateChanged(true);
    Log log = new TestLog();
    SchemaFileService schemaFileService =
        new SchemaFileService(outputDir, new SchemaManifestService(outputDir, outputDir));
    TypeMappingService typeMappingService = new TypeMappingService();
    CodegenExecutor executor = new CodegenExecutor(log, schemaFileService, typeMappingService);
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
    String[] expectedTypeFiles = {"Foo.java"};
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
}
