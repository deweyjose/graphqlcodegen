package io.github.deweyjose.graphqlcodegen.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.deweyjose.graphqlcodegen.TestUtils;
import io.github.deweyjose.graphqlcodegen.parameters.IntrospectionRequest;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaFileServiceTest {

  @Mock private SchemaManifestService schemaManifestService;

  @Mock private RemoteSchemaService remoteSchemaService;

  @Mock private SchemaTransformationService schemaTransformationService;

  private SchemaFileService underTest;

  @BeforeEach
  void setUp() {
    File manifestDir = new File("target/test-classes/schema");
    underTest =
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
    underTest.loadExpandedSchemaPaths(schemaPaths);
    Set<File> result = underTest.getSchemaPaths();

    // Should find all 3 schema files
    assertEquals(3, result.size());
    assertTrue(result.contains(schemaFile));
    assertTrue(result.contains(schemaFile2));
    assertTrue(result.contains(schemaFile3));
  }

  @Test
  void testSetSchemaPaths() {
    Set<File> schemaPaths = Set.of(new File("a.graphqls"), new File("b.graphqls"));
    underTest.setSchemaPaths(schemaPaths);
    assertEquals(schemaPaths, underTest.getSchemaPaths());
  }

  @Test
  void testFilterChangedSchemaFiles() {
    Set<File> allFiles = Set.of(new File("a.graphqls"), new File("b.graphqls"));
    when(schemaManifestService.getChangedFiles())
        .thenReturn(Collections.singleton(new File("b.graphqls")));
    underTest.setSchemaPaths(allFiles);

    underTest.filterChangedSchemaFiles();
    Set<File> result = underTest.getSchemaPaths();
    assertEquals(1, result.size());
    assertTrue(result.contains(new File("b.graphqls")));
  }

  @Test
  void testVerifySchemaFilesThrowsOnEmpty() {
    assertThrows(IllegalArgumentException.class, () -> underTest.checkHasSchemaFiles());
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
    assertTrue(
        files.stream().anyMatch(file -> file.getName().equals("test-schema-with-user.graphqls")));
    assertTrue(
        files.stream()
            .anyMatch(
                file -> file.getName().equals("test-schema-with-nullable-user-fields.graphqls")));
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
    String content = underTest.fetchSchema(url);
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
    underTest.loadSchemaUrls(java.util.List.of(url));
    File outFile = underTest.getSchemaPaths().iterator().next();
    assertTrue(outFile.exists());
    String content = java.nio.file.Files.readString(outFile.toPath());
    assertEquals(expectedContent, content);
    verify(remoteSchemaService, times(1)).getRemoteSchemaFile(url);
  }

  @Test
  void extractSchemaFilesFromDependencies_returnsMatchingArtifactFile() {

    final File artifactFileExpected = new File("foo-1.0.0.jar");
    final File anotherArtifactFileExpected = new File("fqq-2.0.0-20260101.120000-3-schema.jar");

    // Given
    final Set<Artifact> dependencyArtifacts =
        Set.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("foo")
                .version("1.0.0")
                .type("jar")
                .file(artifactFileExpected)
                .build(),
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("fpp")
                .version("1.0.0-20260101.120000-3")
                .type("jar")
                .file(new File("fpp-1.0.0-20260101.120000-3.jar"))
                .build(),
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("fqq")
                .classifier("schema")
                .version("2.0.0-20260101.120000-3")
                .type("jar")
                .file(anotherArtifactFileExpected)
                .build(),
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("frr")
                .classifier("schema")
                .version("1.0.0")
                .type("jar")
                .file(new File("frr-1.0.0.jar"))
                .build());
    final Collection<String> schemaJarFilesFromDependencies =
        List.of(
            " com.example:foo:1.0.0 ",
            " com.example:fqq:schema:2.0.0-SNAPSHOT ",
            // blank coordinate is skipped
            "   ",
            // unmatched coordinate
            " com.example:unmatchedArtifact:1.0.0");

    // When
    final List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(
            dependencyArtifacts, schemaJarFilesFromDependencies);

    // Then
    assertEquals(2, result.size());
    assertTrue(result.contains(artifactFileExpected));
    assertTrue(result.contains(anotherArtifactFileExpected));
  }

  @Test
  void
      extractSchemaFilesFromDependencies_givenSchemaJarFilesFromDependencies_whenIsEmpty_returnEmptyList() {

    // Given
    final Set<Artifact> dependencyArtifacts =
        Set.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("foo")
                .version("1.0.0")
                .type("jar")
                .file(new File("foo-1.0.0.jar"))
                .build());
    final Collection<String> schemaJarFilesFromDependencies = List.of();

    // When
    final List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(
            dependencyArtifacts, schemaJarFilesFromDependencies);

    // Then
    assertEquals(0, result.size());
  }

  @Test
  void extractSchemaFilesFromDependencies_givenDependencyArtifacts_whenIsEmpty_returnEmptyList() {

    // Given
    final Set<Artifact> dependencyArtifacts = Set.of();

    final Collection<String> schemaJarFilesFromDependencies = List.of(" com.example:foo:1.0.0 ");

    // When
    final List<File> result =
        SchemaFileService.extractSchemaFilesFromDependencies(
            dependencyArtifacts, schemaJarFilesFromDependencies);

    // Then
    assertEquals(0, result.size());
  }

  @Test
  void findArtifactFromDependencies_thenReturnFirstMatchingArtifact() {

    final Artifact artifactExpected =
        ArtifactImpl.builder()
            .groupId("com.example.expected")
            .artifactId("foo")
            .classifier("schema")
            .version("1.0.0")
            .type("jar")
            .build();

    // Given
    final Set<Artifact> dependencyArtifacts =
        Set.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("faa")
                .version("1.0.0-20260101.120000-3")
                .type("jar")
                .build(),
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("fbb")
                .version("1.0.0")
                .type("jar")
                .build(),
            artifactExpected);
    final String artifactRef = " com.example.expected:foo:schema:1.0.0  ";

    // When
    final Optional<Artifact> result =
        SchemaFileService.findArtifactFromDependencies(dependencyArtifacts, artifactRef);

    // Then
    assertTrue(result.isPresent());
    assertEquals(artifactExpected, result.get());
  }

  @Test
  void findArtifactFromDependencies_whenNoMatchingArtifactFound_thenReturnOptionalEmpty() {

    // Given
    final Set<Artifact> dependencyArtifacts =
        Set.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("faa")
                .version("1.0.0-20260101.120000-3")
                .type("jar")
                .build(),
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("fbb")
                .version("1.0.0")
                .type("jar")
                .build());
    final String artifactRef = " com.example.expected:foo:schema:1.0.0  ";

    // When
    final Optional<Artifact> result =
        SchemaFileService.findArtifactFromDependencies(dependencyArtifacts, artifactRef);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void findArtifactFromDependencies_givenDependencyArtifacts_whenIsEmpty_thenReturnOptionalEmpty() {

    // Given
    final Set<Artifact> dependencyArtifacts = Set.of();
    final String artifactRef = " com.example.expected:foo:schema:1.0.0  ";

    // When
    final Optional<Artifact> result =
        SchemaFileService.findArtifactFromDependencies(dependencyArtifacts, artifactRef);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void findArtifactFromDependencies_givenArtifactRef_whenIsBlank_thenReturnOptionalEmpty() {

    // Given
    final Set<Artifact> dependencyArtifacts =
        Set.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("faa")
                .version("1.0.0-20260101.120000-3")
                .type("jar")
                .build(),
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("fbb")
                .version("1.0.0")
                .type("jar")
                .build());
    final String artifactRef = "  ";

    // When
    final Optional<Artifact> result =
        SchemaFileService.findArtifactFromDependencies(dependencyArtifacts, artifactRef);

    // Then
    assertTrue(result.isEmpty());
  }

  static Stream<Arguments>
      formatAsCoordinate_givenArtifact_thenReturnArtifactCoordinateAsExpected() {
    return Stream.of(
        Arguments.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("foo")
                .classifier("schema")
                .version("1.0.0-20260101.120000-3")
                .type("jar")
                .build(),
            "com.example:foo:schema:1.0.0-SNAPSHOT"),
        Arguments.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("foo")
                .classifier("schema")
                .version("1.0.0")
                .type("jar")
                .build(),
            "com.example:foo:schema:1.0.0"),
        Arguments.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("foo")
                .version("1.0.0-20260101.120000-3")
                .type("jar")
                .build(),
            "com.example:foo:1.0.0-SNAPSHOT"),
        Arguments.of(
            ArtifactImpl.builder()
                .groupId("com.example")
                .artifactId("foo")
                .version("1.0.0")
                .type("jar")
                .build(),
            "com.example:foo:1.0.0"));
  }

  @MethodSource
  @ParameterizedTest
  void formatAsCoordinate_givenArtifact_thenReturnArtifactCoordinateAsExpected(
      Artifact artifact, String coordinateExpected) {

    // When
    final String result = SchemaFileService.formatAsCoordinate(artifact);

    // Then
    assertEquals(coordinateExpected, result);
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

  private static class ArtifactImpl extends DefaultArtifact {

    @Builder
    public ArtifactImpl(
        String groupId,
        String artifactId,
        String version,
        String scope,
        String type,
        String classifier,
        File file) {
      super(
          groupId, artifactId, version, scope, type, classifier, new DefaultArtifactHandler(type));
      setFile(file);
    }
  }
}
