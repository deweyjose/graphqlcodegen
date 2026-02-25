package io.github.deweyjose.graphqlcodegen.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.deweyjose.graphqlcodegen.TestUtils;
import io.github.deweyjose.graphqlcodegen.parameters.IntrospectionRequest;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SchemaFileServiceTest {

  private SchemaManifestService schemaManifestService;
  private SchemaFileService schemaFileService;
  private RemoteSchemaService remoteSchemaService;
  private SchemaTransformationService schemaTransformationService;

  @BeforeEach
  void setUp() {
    File manifestDir = new File("target/test-classes/schema");
    schemaManifestService = mock(SchemaManifestService.class);
    remoteSchemaService = mock(RemoteSchemaService.class);
    schemaTransformationService = mock(SchemaTransformationService.class);
    schemaFileService =
        new SchemaFileService(
            manifestDir, schemaManifestService, remoteSchemaService, schemaTransformationService);
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
    assertTrue(files.size() >= 6);
    assertTrue(files.stream().anyMatch(file -> file.getName().equals("test-schema.graphqls")));
    assertTrue(files.stream().anyMatch(file -> file.getName().equals("test-schema-with-user.graphqls")));
    assertTrue(
        files.stream()
            .anyMatch(file -> file.getName().equals("test-schema-with-nullable-user-fields.graphqls")));
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
    String url = TestUtils.TEST_SCHEMA_URL;
    String expectedContent = "type Query { hello: String }";
    when(remoteSchemaService.getRemoteSchemaFile(url)).thenReturn(expectedContent);
    String content = schemaFileService.fetchSchema(url);
    assertNotNull(content);
    assertEquals(expectedContent, content);
    verify(remoteSchemaService, times(1)).getRemoteSchemaFile(url);
  }

  @Test
  @SneakyThrows
  void testLoadSchemaUrls_createsFileWithContent(@TempDir Path tempDir) {
    String url = TestUtils.TEST_SCHEMA_URL;
    String expectedContent = "type Query { hello: String }";
    when(remoteSchemaService.getRemoteSchemaFile(url)).thenReturn(expectedContent);
    schemaFileService.loadSchemaUrls(java.util.List.of(url));
    File outFile = schemaFileService.getSchemaPaths().iterator().next();
    assertTrue(outFile.exists());
    String content = java.nio.file.Files.readString(outFile.toPath());
    assertEquals(expectedContent, content);
    verify(remoteSchemaService, times(1)).getRemoteSchemaFile(url);
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
    Set<Artifact> artifacts = java.util.Collections.emptySet();

    java.util.Collection<String> deps = java.util.List.of("com.example:foo:1.0.0");
    java.util.List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(artifacts, deps);

    assertTrue(result.isEmpty());
  }

  @Test
  @SneakyThrows
  void testLoadIntrospectedSchemaUrls_createsFileWithContent(@TempDir Path tempDir) {
    String url = "http://example.com/graphql";
    String query = "query { __schema { types { name } } }";
    String operationName = "IntrospectionQuery";
    Map<String, String> headers = java.util.Map.of("Authorization", "Bearer token");
    String expectedSDL = "type Query { hello: String }";
    when(remoteSchemaService.getIntrospectedSchemaFile(
            eq(url),
            argThat(
                op -> op.getQuery().equals(query) && op.getOperationName().equals(operationName)),
            eq(headers)))
        .thenReturn(expectedSDL);

    when(schemaTransformationService.transformSchema(expectedSDL)).thenReturn(expectedSDL);

    IntrospectionRequest request = new IntrospectionRequest();
    request.setUrl(url);
    request.setQuery(query);
    request.setOperationName(operationName);
    request.setHeaders(headers);

    SchemaFileService service =
        new SchemaFileService(
            tempDir.toFile(),
            schemaManifestService,
            remoteSchemaService,
            schemaTransformationService);
    service.loadIntrospectedSchemas(List.of(request));
    Set<File> schemaPaths = service.getSchemaPaths();
    assertEquals(1, schemaPaths.size());
    File outFile = schemaPaths.iterator().next();
    assertTrue(outFile.exists());
    String content = java.nio.file.Files.readString(outFile.toPath());
    assertEquals(expectedSDL, content);
    verify(remoteSchemaService, times(1))
        .getIntrospectedSchemaFile(
            eq(url),
            argThat(
                op -> op.getQuery().equals(query) && op.getOperationName().equals(operationName)),
            eq(headers));
  }
}
