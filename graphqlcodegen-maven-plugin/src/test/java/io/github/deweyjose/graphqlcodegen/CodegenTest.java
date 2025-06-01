package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CodegenTest {
  private void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Disabled("Skipping this test for now")
  void testExecute_skipsWhenSkipIsTrue() {
    Codegen codegen = new Codegen();
    MavenProject project = mock(MavenProject.class);
    setField(codegen, "project", project);
    setField(codegen, "skip", true);
    // Should not throw, should log skip
    assertDoesNotThrow(codegen::execute);
  }

  @Test
  @Disabled("Skipping this test for now")
  void testExecute_runsCodegenWhenNotSkipped() {
    Codegen codegen = new Codegen();
    MavenProject project = mock(MavenProject.class);
    File basedir = new File(".");
    when(project.getBasedir()).thenReturn(basedir);
    when(project.getArtifacts()).thenReturn(Collections.emptySet());
    doNothing().when(project).addCompileSourceRoot(anyString());
    setField(codegen, "project", project);
    setField(codegen, "skip", false);
    setField(codegen, "outputDir", new File("target/test-generated"));
    setField(codegen, "schemaManifestOutputDir", new File("target/test-manifest"));
    // Should not throw, should attempt codegen
    assertDoesNotThrow(codegen::execute);
  }
}
