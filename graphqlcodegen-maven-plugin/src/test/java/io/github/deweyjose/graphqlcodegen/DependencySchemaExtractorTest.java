package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.Test;

class DependencySchemaExtractorTest {

  @Test
  void extract_returnsMatchingArtifactFile() {
    Artifact artifact = mock(Artifact.class);
    when(artifact.getGroupId()).thenReturn("com.example");
    when(artifact.getArtifactId()).thenReturn("foo");
    when(artifact.getVersion()).thenReturn("1.0.0");
    File file = new File("foo-1.0.0.jar");
    when(artifact.getFile()).thenReturn(file);

    Set<Artifact> artifacts = new HashSet<>();
    artifacts.add(artifact);

    String[] deps = {"com.example:foo:1.0.0"};
    List<File> result = DependencySchemaExtractor.extract(artifacts, deps);

    assertEquals(1, result.size());
    assertEquals(file, result.get(0));
  }

  @Test
  void extract_skipsEmptyEntries() {
    Set<Artifact> artifacts = Collections.emptySet();

    String[] deps = {"   ", ""};
    List<File> result = DependencySchemaExtractor.extract(artifacts, deps);

    assertTrue(result.isEmpty());
  }

  @Test
  void extract_ignoresNonMatchingDependencies() {
    Artifact artifact = mock(Artifact.class);
    when(artifact.getGroupId()).thenReturn("com.example");
    when(artifact.getArtifactId()).thenReturn("foo");
    when(artifact.getVersion()).thenReturn("1.0.0");
    when(artifact.getFile()).thenReturn(new File("foo-1.0.0.jar"));

    Set<Artifact> artifacts = new HashSet<>();
    artifacts.add(artifact);

    String[] deps = {"com.other:bar:2.0.0"};
    List<File> result = DependencySchemaExtractor.extract(artifacts, deps);

    assertTrue(result.isEmpty());
  }

  @Test
  void extract_returnsEmptyListIfNoDependencies() {
    Set<Artifact> artifacts = Collections.emptySet();

    String[] deps = {"com.example:foo:1.0.0"};
    List<File> result = DependencySchemaExtractor.extract(artifacts, deps);

    assertTrue(result.isEmpty());
  }
}
 