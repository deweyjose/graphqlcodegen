package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.deweyjose.graphqlcodegen.parameters.IntrospectionRequest;
import io.github.deweyjose.graphqlcodegen.parameters.ParameterMap;
import io.github.deweyjose.graphqlcodegen.services.RemoteSchemaService;
import io.github.deweyjose.graphqlcodegen.services.SchemaFileService;
import io.github.deweyjose.graphqlcodegen.services.SchemaManifestService;
import io.github.deweyjose.graphqlcodegen.services.SchemaTransformationService;
import io.github.deweyjose.graphqlcodegen.services.TypeMappingService;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CodegenExecutorTest {

  private RemoteSchemaService remoteSchemaService;
  private SchemaTransformationService schemaTransformationService;
  private SchemaFileService schemaFileService;
  private TypeMappingService typeMappingService;
  private CodegenExecutor executor;
  private File outputDir;

  @BeforeEach
  void setUp() {
    remoteSchemaService = mock(RemoteSchemaService.class);
    schemaTransformationService = new SchemaTransformationService();
    typeMappingService = new TypeMappingService();

    // Setup single output directory
    outputDir = new File("target/generated-test-codegen-executor");
    if (outputDir.exists()) {
      deleteDirectory(outputDir);
    }
    outputDir.mkdirs();
  }

  @SneakyThrows
  @Test
  void testGenerateCodeFromSchema() {
    File schemaFile = TestUtils.getFile("schema/test-schema.graphqls");

    SchemaManifestService manifestService = new SchemaManifestService(outputDir, outputDir);
    schemaFileService =
        new SchemaFileService(
            outputDir, manifestService, remoteSchemaService, schemaTransformationService);
    executor = new CodegenExecutor(schemaFileService, typeMappingService);

    TestCodegenProvider config = new TestCodegenProvider();
    config.setSchemaPaths(Set.of(schemaFile));
    config.setOutputDir(outputDir);
    config.setSchemaManifestOutputDir(outputDir);

    executor.execute(config, new HashSet<>(), new File("."));

    assertTrue(outputDir.exists());
    assertTrue(outputDir.isDirectory());
    assertTrue(
        new File(outputDir, "com/example/datafetchers/HelloDatafetcher.java").exists(),
        "Should generate datafetcher file");
    assertTrue(
        new File(outputDir, "com/example/DgsConstants.java").exists(),
        "Should generate constants file");
  }

  @SneakyThrows
  @Test
  void testGenerateCodeFromSchemaWithRemoteSchema() {
    String testSchema = TestUtils.getFileContent("schema/test-schema-with-user.graphqls");
    when(remoteSchemaService.getRemoteSchemaFile(TestUtils.TEST_SCHEMA_URL)).thenReturn(testSchema);

    SchemaManifestService manifestService = new SchemaManifestService(outputDir, outputDir);
    schemaFileService =
        new SchemaFileService(
            outputDir, manifestService, remoteSchemaService, schemaTransformationService);
    executor = new CodegenExecutor(schemaFileService, typeMappingService);

    TestCodegenProvider config = new TestCodegenProvider();
    config.setOutputDir(outputDir);
    config.setSchemaManifestOutputDir(outputDir);
    config.setSchemaUrls(java.util.List.of(TestUtils.TEST_SCHEMA_URL));

    executor.execute(config, new HashSet<>(), new File("."));

    assertTrue(outputDir.exists());
    assertTrue(outputDir.isDirectory());
    assertTrue(
        new File(outputDir, "com/example/datafetchers/HelloDatafetcher.java").exists(),
        "Should generate datafetcher file");
    assertTrue(
        new File(outputDir, "com/example/datafetchers/UserDatafetcher.java").exists(),
        "Should generate datafetcher file");
    assertTrue(
        new File(outputDir, "com/example/types/User.java").exists(),
        "Should generate User Type file");
    assertTrue(
        new File(outputDir, "com/example/DgsConstants.java").exists(),
        "Should generate constants file");
  }

  @Test
  void testToMapNullAndEmptyAndNormal() {
    assertEquals(Collections.emptyMap(), CodegenExecutor.toMap(null));
    assertEquals(Collections.emptyMap(), CodegenExecutor.toMap(Collections.emptyMap()));

    ParameterMap paramMap = new ParameterMap();
    paramMap.setProperties(Map.of("nested", "value"));
    Map<String, ParameterMap> input = new HashMap<>();
    input.put("key", paramMap);
    assertEquals(Map.of("key", Map.of("nested", "value")), CodegenExecutor.toMap(input));
  }

  @Test
  void testGenerateCodeFromIntrospection() throws Exception {
    String testSchema = TestUtils.getFileContent("schema/test-schema-with-user.graphqls");

    when(remoteSchemaService.getIntrospectedSchemaFile(
            eq("https://example.com/graphql"), any(), any()))
        .thenReturn(testSchema);

    TestCodegenProvider config = new TestCodegenProvider();
    config.setOutputDir(outputDir);
    config.setSchemaManifestOutputDir(outputDir);

    IntrospectionRequest introspectionRequest = new IntrospectionRequest();
    introspectionRequest.setUrl("https://example.com/graphql");
    config.setIntrospectionRequests(List.of(introspectionRequest));

    SchemaManifestService manifestService = new SchemaManifestService(outputDir, outputDir);
    schemaFileService =
        new SchemaFileService(
            outputDir, manifestService, remoteSchemaService, schemaTransformationService);
    executor = new CodegenExecutor(schemaFileService, typeMappingService);

    executor.execute(config, new HashSet<>(), new File("."));

    // Assert that code generation produced output files
    assertTrue(outputDir.exists());
    assertTrue(outputDir.isDirectory());
    assertTrue(
        new File(outputDir, "com/example/datafetchers/HelloDatafetcher.java").exists(),
        "Should generate Hello datafetcher file");
    assertTrue(
        new File(outputDir, "com/example/datafetchers/UserDatafetcher.java").exists(),
        "Should generate User datafetcher file");
    assertTrue(
        new File(outputDir, "com/example/types/User.java").exists(),
        "Should generate User type file");
    assertTrue(
        new File(outputDir, "com/example/DgsConstants.java").exists(),
        "Should generate constants file");
  }

  @Test
  void testGenerateCodeFromIntrospectionCustomTypes() throws IOException {
    String testSchema = TestUtils.getFileContent("schema/test-schema-custom-roots.graphqls");

    when(remoteSchemaService.getIntrospectedSchemaFile(
            eq("https://example.com/graphql"), any(), any()))
        .thenReturn(testSchema);

    TestCodegenProvider config = new TestCodegenProvider();
    config.setOutputDir(outputDir);
    config.setSchemaManifestOutputDir(outputDir);
    config.setGenerateDataTypes(false);

    IntrospectionRequest introspectionRequest = new IntrospectionRequest();
    introspectionRequest.setUrl("https://example.com/graphql");
    config.setIntrospectionRequests(List.of(introspectionRequest));

    SchemaManifestService manifestService = new SchemaManifestService(outputDir, outputDir);
    schemaFileService =
        new SchemaFileService(
            outputDir, manifestService, remoteSchemaService, schemaTransformationService);
    executor = new CodegenExecutor(schemaFileService, typeMappingService);

    executor.execute(config, new HashSet<>(), new File("."));

    // Assert that code generation produced output files
    assertTrue(outputDir.exists());
    assertTrue(outputDir.isDirectory());

    // Assert constants file
    assertTrue(
        new File(outputDir, "com/example/DgsConstants.java").exists(),
        "Should generate constants file");

    // Assert client files
    assertTrue(
        new File(outputDir, "com/example/client/ActorsGraphQLQuery.java").exists(),
        "Should generate ActorsGraphQLQuery file");
    assertTrue(
        new File(outputDir, "com/example/client/ActorsProjectionRoot.java").exists(),
        "Should generate ActorsProjectionRoot file");
    assertTrue(
        new File(outputDir, "com/example/client/HelloEventsGraphQLQuery.java").exists(),
        "Should generate HelloEventsGraphQLQuery file");
    assertTrue(
        new File(outputDir, "com/example/client/UpdateHelloGraphQLQuery.java").exists(),
        "Should generate UpdateHelloGraphQLQuery file");
    assertTrue(
        new File(outputDir, "com/example/client/HelloGraphQLQuery.java").exists(),
        "Should generate HelloGraphQLQuery file");

    // Assert datafetcher files
    assertTrue(
        new File(outputDir, "com/example/datafetchers/HelloDatafetcher.java").exists(),
        "Should generate HelloDatafetcher file");
    assertTrue(
        new File(outputDir, "com/example/datafetchers/ActorsDatafetcher.java").exists(),
        "Should generate ActorsDatafetcher file");
  }

  private void deleteDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          file.delete();
        }
      }
    }
    directory.delete();
  }
}
