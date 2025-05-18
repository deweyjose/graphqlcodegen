package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenExecutorTest {

  private Log log;
  private CodegenExecutor executor;

  @BeforeEach
  void setUp() {
    log = mock(Log.class);
    executor = new CodegenExecutor(log);
  }

  @Test
  void testVerifyPackageNameThrowsOnNull() {
    assertThrows(IllegalArgumentException.class, () -> executor.verifyPackageName(null));
  }

  @Test
  void testVerifyPackageNameDoesNotThrow() {
    assertDoesNotThrow(() -> executor.verifyPackageName("com.example"));
  }

  @Test
  void testExpandSchemaPaths_withFilesAndDirs(@TempDir Path tempDir) throws IOException {
    // Create a temporary schema file
    File schemaFile = tempDir.resolve("test.graphqls").toFile();
    schemaFile.createNewFile();

    // Create a temporary directory with schema files
    File schemaDir = tempDir.resolve("schemas").toFile();
    schemaDir.mkdir();
    File schemaFile2 = new File(schemaDir, "test2.graphqls");
    schemaFile2.createNewFile();
    File schemaFile3 = new File(schemaDir, "test3.graphqls");
    schemaFile3.createNewFile();

    // Test with both files and directories
    File[] schemaPaths = {schemaFile, schemaDir};
    Set<File> result = executor.expandSchemaPaths(schemaPaths, true);

    // Should find all 3 schema files
    assertEquals(3, result.size());
    assertTrue(result.contains(schemaFile));
    assertTrue(result.contains(schemaFile2));
    assertTrue(result.contains(schemaFile3));

    // Test without recursive search
    result = executor.expandSchemaPaths(schemaPaths, false);
    assertEquals(2, result.size());
    assertTrue(result.contains(schemaFile));
    assertTrue(result.contains(schemaDir));
  }

  @Test
  void testLoadPropertiesFile_validJarAndProperties(@TempDir Path tempDir) throws Exception {
    // Copy the test JAR to the temp directory
    File jarFile = tempDir.resolve("test-artifact.jar").toFile();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("test-artifact.jar")) {
      assertNotNull(in, "test-artifact.jar should be on the test classpath");
      java.nio.file.Files.copy(in, jarFile.toPath());
    }
    // Call loadPropertiesFile
    CodegenExecutor exec = new CodegenExecutor(log);
    String[] props = {"test-type-mapping.properties"};
    Map<String, String> result = exec.loadPropertiesFile(jarFile, props);
    assertEquals("bar", result.get("foo"));
    assertEquals("world", result.get("hello"));
  }

  @Test
  void testVerifySchemaFilesThrowsOnEmpty() {
    assertThrows(
        IllegalArgumentException.class,
        () -> executor.verifySchemaFiles(Collections.emptySet(), new String[0]));
  }

  @Test
  void testVerifySchemaFilesDoesNotThrow() {
    Set<File> files = new HashSet<>();
    files.add(new File("dummy.graphqls"));
    assertDoesNotThrow(() -> executor.verifySchemaFiles(files, new String[0]));
  }

  @Test
  void testExecute_withMinimalValidRequest() {
    // TODO: create minimal valid ExecutionRequest, mock artifacts, call execute, verify log/codegen
  }

  @Test
  void testMergeTypeMapping_userPrecedence(@TempDir Path tempDir) throws Exception {
    // Copy the test JAR to the temp directory
    File jarFile = tempDir.resolve("test-artifact.jar").toFile();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("test-artifact.jar")) {
      assertNotNull(in, "test-artifact.jar should be on the test classpath");
      java.nio.file.Files.copy(in, jarFile.toPath());
    }
    // User map overlaps with JAR and adds a new key
    Map<String, String> userMap = new java.util.HashMap<>();
    userMap.put("foo", "userBar"); // overrides JAR
    userMap.put("userOnly", "value");
    String[] props = {"test-type-mapping.properties"};
    Set<org.apache.maven.artifact.Artifact> artifacts = new java.util.HashSet<>();
    org.apache.maven.artifact.Artifact mockArtifact =
        mock(org.apache.maven.artifact.Artifact.class);
    when(mockArtifact.getFile()).thenReturn(jarFile);
    artifacts.add(mockArtifact);
    CodegenExecutor exec = new CodegenExecutor(log);
    Map<String, String> result = exec.mergeTypeMapping(userMap, props, artifacts);
    // User value should win
    assertEquals("userBar", result.get("foo"));
    // JAR value should be present if not overridden
    assertEquals("world", result.get("hello"));
    // User-only value should be present
    assertEquals("value", result.get("userOnly"));
  }
}
