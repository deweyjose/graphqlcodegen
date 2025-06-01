package io.github.deweyjose.graphqlcodegen;

import static junit.framework.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SchemaFileServiceTest {

  private SchemaManifestService schemaManifestService;
  private SchemaFileService schemaFileService;

  @BeforeEach
  void setUp() {
    File manifestDir = new File("target/test-classes/schema");
    schemaManifestService = mock(SchemaManifestService.class);
    schemaFileService = new SchemaFileService(manifestDir, schemaManifestService);
  }

  @SneakyThrows
  @Test
  void testLoadExpandedSchemaPaths(@TempDir Path tempDir) {
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
    Set<File> schemaPaths = Set.of(schemaFile, schemaDir);
    schemaFileService.loadExpandedSchemaPaths(schemaPaths);
    Set<File> result = schemaFileService.getSchemaPaths();

    // Should find all 3 schema files
    assertEquals(3, result.size());
    assertTrue(result.contains(schemaFile));
    assertTrue(result.contains(schemaFile2));
    assertTrue(result.contains(schemaFile3));
  }

  @Test
  void testSetSchemaPaths() {
    Set<File> schemaPaths = Set.of(new File("a.graphqls"), new File("b.graphqls"));
    schemaFileService.setSchemaPaths(schemaPaths);
    assertEquals(schemaPaths, schemaFileService.getSchemaPaths());
  }

  @Test
  void testFilterChangedSchemaFiles() {
    Set<File> allFiles = Set.of(new File("a.graphqls"), new File("b.graphqls"));
    when(schemaManifestService.getChangedFiles())
        .thenReturn(Collections.singleton(new File("b.graphqls")));
    schemaFileService.setSchemaPaths(allFiles);

    schemaFileService.filterChangedSchemaFiles();
    Set<File> result = schemaFileService.getSchemaPaths();
    assertEquals(1, result.size());
    assertTrue(result.contains(new File("b.graphqls")));
  }

  @Test
  void testVerifySchemaFilesThrowsOnEmpty() {
    assertThrows(IllegalArgumentException.class, () -> schemaFileService.checkHasSchemaFiles());
  }

  @Test
  void testIsGraphqlFile() {
    assertTrue(SchemaFileService.isGraphqlFile(new File("abc/foo.graphql")));
    assertTrue(SchemaFileService.isGraphqlFile(new File("abc/foo.graphqls")));
    assertTrue(SchemaFileService.isGraphqlFile(new File("abc/foo.gqls")));
    assertFalse(SchemaFileService.isGraphqlFile(new File("abc/foo.graph")));
    assertFalse(SchemaFileService.isGraphqlFile(new File("abc")));
  }

  @Test
  void testFindGraphqlFiles() {
    File directory = TestUtils.getFile("schema");
    Set<File> files = SchemaFileService.findGraphQLSFiles(directory);
    assertEquals(2, files.size());
  }

  @Test
  @SneakyThrows
  void testFindGraphqlFilesWithNestedDirectories(@TempDir Path tempDir) {
    // Create root directory with a schema file
    File rootSchema = tempDir.resolve("root.graphqls").toFile();
    rootSchema.createNewFile();

    // Create a nested directory with another schema file
    File nestedDir = tempDir.resolve("nested").toFile();
    nestedDir.mkdir();
    File nestedSchema = new File(nestedDir, "nested.graphqls");
    nestedSchema.createNewFile();

    // Create a deeper nested directory with another schema file
    File deepDir = new File(nestedDir, "deep");
    deepDir.mkdir();
    File deepSchema = new File(deepDir, "deep.graphqls");
    deepSchema.createNewFile();

    Set<File> files = SchemaFileService.findGraphQLSFiles(tempDir.toFile());
    assertEquals(3, files.size());
    assertTrue(files.contains(rootSchema));
    assertTrue(files.contains(nestedSchema));
    assertTrue(files.contains(deepSchema));
  }

  @Test
  @SneakyThrows
  void testDownloadCodeGenConfig_fetchesRemoteSchema() {
    String content = schemaFileService.fetchSchema(TestUtils.TEST_SCHEMA_URL);
    assertNotNull(content);
    assertTrue(
        content.contains("type") || content.contains("schema"), "Should contain GraphQL SDL");
  }

  @Test
  @SneakyThrows
  void testSaveUrlToFile_createsFileWithContent(@TempDir Path tempDir) {
    File outFile = schemaFileService.saveUrlToFile(TestUtils.TEST_SCHEMA_URL, tempDir.toFile());
    assertTrue(outFile.exists());
    String content = java.nio.file.Files.readString(outFile.toPath());
    assertTrue(
        content.contains("type") || content.contains("schema"), "Should contain GraphQL SDL");
  }

  @Test
  void extractSchemaFilesFromDependencies_returnsMatchingArtifactFile() {
    org.apache.maven.artifact.Artifact artifact = mock(org.apache.maven.artifact.Artifact.class);
    when(artifact.getGroupId()).thenReturn("com.example");
    when(artifact.getArtifactId()).thenReturn("foo");
    when(artifact.getVersion()).thenReturn("1.0.0");
    File file = new File("foo-1.0.0.jar");
    when(artifact.getFile()).thenReturn(file);

    Set<org.apache.maven.artifact.Artifact> artifacts = new java.util.HashSet<>();
    artifacts.add(artifact);

    java.util.Collection<String> deps = java.util.List.of("com.example:foo:1.0.0");
    java.util.List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(artifacts, deps);

    assertEquals(1, result.size());
    assertEquals(file, result.get(0));
  }

  @Test
  void extractSchemaFilesFromDependencies_skipsEmptyEntries() {
    Set<org.apache.maven.artifact.Artifact> artifacts = java.util.Collections.emptySet();

    java.util.Collection<String> deps = java.util.List.of("   ", "");
    java.util.List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(artifacts, deps);

    assertTrue(result.isEmpty());
  }

  @Test
  void extractSchemaFilesFromDependencies_ignoresNonMatchingDependencies() {
    org.apache.maven.artifact.Artifact artifact = mock(org.apache.maven.artifact.Artifact.class);
    when(artifact.getGroupId()).thenReturn("com.example");
    when(artifact.getArtifactId()).thenReturn("foo");
    when(artifact.getVersion()).thenReturn("1.0.0");
    when(artifact.getFile()).thenReturn(new File("foo-1.0.0.jar"));

    Set<org.apache.maven.artifact.Artifact> artifacts = new java.util.HashSet<>();
    artifacts.add(artifact);

    java.util.Collection<String> deps = java.util.List.of("com.other:bar:2.0.0");
    java.util.List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(artifacts, deps);

    assertTrue(result.isEmpty());
  }

  @Test
  void extractSchemaFilesFromDependencies_returnsEmptyListIfNoDependencies() {
    Set<org.apache.maven.artifact.Artifact> artifacts = java.util.Collections.emptySet();

    java.util.Collection<String> deps = java.util.List.of("com.example:foo:1.0.0");
    java.util.List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(artifacts, deps);

    assertTrue(result.isEmpty());
  }
}
