package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenExecutorTest {

  private Log log;
  private CodegenExecutor executor;
  private SchemaFileService schemaFileService;
  private TypeMappingService typeMappingService;

  @TempDir static Path classTempDir;
  static File testJarFile;
  static final String propsFileName = "test-type-mapping.properties";
  static final String propsContent = "foo=bar\nhello=world\n";

  @BeforeAll
  static void createTestJar() throws Exception {
    // Create the properties file
    Path propsPath = classTempDir.resolve(propsFileName);
    java.nio.file.Files.write(
        propsPath, propsContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    // Create the JAR file containing the properties file
    testJarFile = classTempDir.resolve("test-artifact.jar").toFile();
    try (java.util.jar.JarOutputStream jarOut =
        new java.util.jar.JarOutputStream(new java.io.FileOutputStream(testJarFile))) {
      java.util.jar.JarEntry entry = new java.util.jar.JarEntry(propsFileName);
      jarOut.putNextEntry(entry);
      byte[] bytes = java.nio.file.Files.readAllBytes(propsPath);
      jarOut.write(bytes);
      jarOut.closeEntry();
    }
  }

  @BeforeEach
  void setUp() {
    log = mock(Log.class);
    schemaFileService = new SchemaFileService();
    typeMappingService = new TypeMappingService();
    executor = new CodegenExecutor(log, schemaFileService, typeMappingService);
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
    Set<File> result = schemaFileService.expandSchemaPaths(schemaPaths);

    // Should find all 3 schema files
    assertEquals(3, result.size());
    assertTrue(result.contains(schemaFile));
    assertTrue(result.contains(schemaFile2));
    assertTrue(result.contains(schemaFile3));

    // Test without recursive search
    result = Arrays.stream(schemaPaths).collect(Collectors.toSet());
    assertEquals(2, result.size());
    assertTrue(result.contains(schemaFile));
    assertTrue(result.contains(schemaDir));
  }

  @Test
  void testFilterChangedSchemaFiles() {
    Set<File> allFiles =
        new HashSet<>(Arrays.asList(new File("a.graphqls"), new File("b.graphqls")));
    SchemaManifestService manifest = mock(SchemaManifestService.class);
    when(manifest.getChangedFiles()).thenReturn(Collections.singleton(new File("b.graphqls")));
    Set<File> result = schemaFileService.filterChangedSchemaFiles(allFiles, manifest);
    assertEquals(1, result.size());
    assertTrue(result.contains(new File("b.graphqls")));
    verify(manifest).setFiles(new HashSet<>(allFiles));
  }

  @Test
  void testLoadPropertiesFile_validJarAndProperties(@TempDir Path tempDir) throws Exception {
    // Call loadPropertiesFile using the shared test JAR
    String[] props = {propsFileName};
    Map<String, String> result = typeMappingService.loadPropertiesFile(testJarFile, props);
    assertEquals("bar", result.get("foo"));
    assertEquals("world", result.get("hello"));
  }

  @Test
  void testVerifySchemaFilesThrowsOnEmpty() {
    assertThrows(
        IllegalArgumentException.class,
        () -> schemaFileService.verifySchemaFiles(Collections.emptySet(), new String[0]));
  }

  @Test
  void testVerifySchemaFilesDoesNotThrow() {
    Set<File> files = new HashSet<>();
    files.add(new File("dummy.graphqls"));
    assertDoesNotThrow(() -> schemaFileService.verifySchemaFiles(files, new String[0]));
  }

  @Test
  void testMergeTypeMapping_userPrecedence(@TempDir Path tempDir) throws Exception {
    // User map overlaps with JAR and adds a new key
    Map<String, String> userMap = new java.util.HashMap<>();
    userMap.put("foo", "userBar"); // overrides JAR
    userMap.put("userOnly", "value");
    String[] props = {propsFileName};
    Set<org.apache.maven.artifact.Artifact> artifacts = new java.util.HashSet<>();
    org.apache.maven.artifact.Artifact mockArtifact =
        mock(org.apache.maven.artifact.Artifact.class);
    when(mockArtifact.getFile()).thenReturn(testJarFile);
    artifacts.add(mockArtifact);
    Map<String, String> result = typeMappingService.mergeTypeMapping(userMap, props, artifacts);
    // User value should win
    assertEquals("userBar", result.get("foo"));
    // JAR value should be present if not overridden
    assertEquals("world", result.get("hello"));
    // User-only value should be present
    assertEquals("value", result.get("userOnly"));
  }

  @Test
  void testToSet_nullAndEmptyAndNormal() {
    assertEquals(Collections.emptySet(), CodegenExecutor.toSet(null));
    assertEquals(Collections.emptySet(), CodegenExecutor.toSet(new String[0]));
    assertEquals(Set.of("a", "b"), CodegenExecutor.toSet(new String[] {"a", "b"}));
  }

  @Test
  void testToMap_nullAndEmptyAndNormal() {
    assertEquals(Collections.emptyMap(), CodegenExecutor.toMap(null));
    assertEquals(Collections.emptyMap(), CodegenExecutor.toMap(Collections.emptyMap()));
    ParameterMap paramMap = new ParameterMap();
    Map<String, String> props = new HashMap<>();
    props.put("foo", "bar");
    paramMap.setProperties(props);
    Map<String, ParameterMap> input = new HashMap<>();
    input.put("key", paramMap);
    Map<String, Map<String, String>> result = CodegenExecutor.toMap(input);
    assertEquals(1, result.size());
    assertEquals(Map.of("foo", "bar"), result.get("key"));
  }

  @Test
  void testDownloadCodeGenConfig_fetchesRemoteSchema() throws Exception {
    String content = schemaFileService.fetchSchema(TestUtils.TEST_SCHEMA_URL);
    assertNotNull(content);
    assertTrue(
        content.contains("type") || content.contains("schema"), "Should contain GraphQL SDL");
  }

  @Test
  void testSaveUrlToFile_createsFileWithContent(@TempDir java.nio.file.Path tempDir)
      throws Exception {
    // Arrange
    File outFile = schemaFileService.saveUrlToFile(TestUtils.TEST_SCHEMA_URL, tempDir.toFile());
    assertTrue(outFile.exists());
    String content = java.nio.file.Files.readString(outFile.toPath());
    assertTrue(
        content.contains("type") || content.contains("schema"), "Should contain GraphQL SDL");
  }
}
