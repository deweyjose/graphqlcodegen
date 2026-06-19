package io.github.deweyjose.graphqlcodegen.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TypeMappingServiceTest {
  private File tempJar;

  @TempDir static Path classTempDir;

  @BeforeEach
  void setUp() throws IOException {
    // Create a temporary jar file with a properties file inside
    tempJar = File.createTempFile("test", ".jar");
    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempJar))) {
      JarEntry entry = new JarEntry("type-mapping.properties");
      jos.putNextEntry(entry);
      String content = "foo=bar\nhello=world\n";
      jos.write(content.getBytes());
      jos.closeEntry();
    }
  }

  @AfterEach
  void tearDown() {
    if (tempJar != null && tempJar.exists()) {
      tempJar.delete();
    }
  }

  @Test
  void loadPropertiesFile_readsPropertiesFromJar() {
    TypeMappingService service = new TypeMappingService();
    Map<String, String> result =
        service.loadPropertiesFile(tempJar, List.of("type-mapping.properties"));
    assertEquals(2, result.size());
    assertEquals("bar", result.get("foo"));
    assertEquals("world", result.get("hello"));
  }

  @Test
  void loadPropertiesFile_returnsEmptyMapIfNoFile() {
    TypeMappingService service = new TypeMappingService();
    Map<String, String> result =
        service.loadPropertiesFile(tempJar, List.of("does-not-exist.properties"));
    assertTrue(result.isEmpty());
  }

  @Test
  void mergeTypeMapping_returnsEmptyIfNoInput() {
    TypeMappingService service = new TypeMappingService();
    Map<String, String> result =
        service.mergeTypeMapping(null, null, null, Collections.emptySet(), new File("."));
    assertTrue(result.isEmpty());
  }

  @Test
  void loadLocalPropertiesFiles_readsPropertiesFromLocalFile() throws IOException {
    TypeMappingService service = new TypeMappingService();
    File tempDir = Files.createTempDirectory("test").toFile();
    File propertiesFile = new File(tempDir, "local-type-mapping.properties");
    try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
      fos.write("foo=bar\nhello=world\n".getBytes());
    }

    Map<String, String> result =
        service.loadLocalPropertiesFiles(tempDir, List.of("local-type-mapping.properties"));
    assertEquals(2, result.size());
    assertEquals("bar", result.get("foo"));
    assertEquals("world", result.get("hello"));

    // Cleanup
    propertiesFile.delete();
    tempDir.delete();
  }

  @Test
  void mergeTypeMapping_includesLocalTypeMappings() throws IOException {
    TypeMappingService service = new TypeMappingService();
    File tempDir = Files.createTempDirectory("test").toFile();
    File propertiesFile = new File(tempDir, "local-type-mapping.properties");
    try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
      fos.write("foo=bar\nhello=world\n".getBytes());
    }

    Map<String, String> userMap = new HashMap<>();
    userMap.put("user", "value");

    Map<String, String> result =
        service.mergeTypeMapping(
            userMap,
            Collections.emptyList(),
            List.of("local-type-mapping.properties"),
            Collections.emptySet(),
            tempDir);

    assertEquals(3, result.size());
    assertEquals("bar", result.get("foo"));
    assertEquals("world", result.get("hello"));
    assertEquals("value", result.get("user"));

    // Cleanup
    propertiesFile.delete();
    tempDir.delete();
  }

  @Test
  void mergeTypeMapping_mergesJarAndUserTypeMapping() {
    TypeMappingService service = new TypeMappingService();
    Artifact artifact = mock(Artifact.class);
    when(artifact.getFile()).thenReturn(tempJar);
    Set<Artifact> artifacts = new HashSet<>();
    artifacts.add(artifact);
    Map<String, String> userMap = new HashMap<>();
    userMap.put("foo", "override");
    userMap.put("user", "value");
    Map<String, String> result =
        service.mergeTypeMapping(
            userMap,
            List.of("type-mapping.properties"),
            Collections.emptyList(),
            artifacts,
            new File("."));
    assertEquals(3, result.size());
    assertEquals("override", result.get("foo")); // user map overrides jar
    assertEquals("world", result.get("hello"));
    assertEquals("value", result.get("user"));
  }

  @Test
  void mergeTypeMapping_returnsUserMapIfNoArtifacts() {
    TypeMappingService service = new TypeMappingService();
    Map<String, String> userMap = new HashMap<>();
    userMap.put("foo", "bar");
    Map<String, String> result =
        service.mergeTypeMapping(
            userMap,
            List.of("type-mapping.properties"),
            Collections.emptyList(),
            Collections.emptySet(),
            new File("."));
    assertEquals(1, result.size());
    assertEquals("bar", result.get("foo"));
  }
}
