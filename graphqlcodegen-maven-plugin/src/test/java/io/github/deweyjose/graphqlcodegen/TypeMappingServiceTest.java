package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        service.loadPropertiesFile(tempJar, new String[] {"type-mapping.properties"});
    assertEquals(2, result.size());
    assertEquals("bar", result.get("foo"));
    assertEquals("world", result.get("hello"));
  }

  @Test
  void loadPropertiesFile_returnsEmptyMapIfNoFile() {
    TypeMappingService service = new TypeMappingService();
    Map<String, String> result =
        service.loadPropertiesFile(tempJar, new String[] {"does-not-exist.properties"});
    assertTrue(result.isEmpty());
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
        service.mergeTypeMapping(userMap, new String[] {"type-mapping.properties"}, artifacts);
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
            userMap, new String[] {"type-mapping.properties"}, Collections.emptySet());
    assertEquals(1, result.size());
    assertEquals("bar", result.get("foo"));
  }

  @Test
  void mergeTypeMapping_returnsEmptyIfNoInput() {
    TypeMappingService service = new TypeMappingService();
    Map<String, String> result = service.mergeTypeMapping(null, null, Collections.emptySet());
    assertTrue(result.isEmpty());
  }
}
