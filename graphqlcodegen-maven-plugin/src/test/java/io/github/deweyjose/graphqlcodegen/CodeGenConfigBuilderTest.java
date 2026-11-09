package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CodeGenConfigBuilderTest {

  /**
   * Populate every non-null constructor argument of {@link CodeGenConfig} so {@code build()}
   * succeeds. The executor always sets these; this mirrors the minimum it provides.
   */
  private static CodeGenConfigBuilder baseBuilder() {
    return new CodeGenConfigBuilder()
        .setSchemas(Collections.emptySet())
        .setSchemaFiles(Collections.emptySet())
        .setSchemaJarFilesFromDependencies(Collections.emptyList())
        .setOutputDir(Paths.get("generated"))
        .setExamplesOutputDir(Paths.get("generated-examples"))
        .setPackageName("com.example.generated")
        .setSubPackageNameClient("client")
        .setSubPackageNameDatafetchers("datafetchers")
        .setSubPackageNameTypes("types")
        .setSubPackageNameDocs("docs")
        .setLanguage(Language.JAVA)
        .setTypeMapping(Collections.emptyMap())
        .setIncludeQueries(Collections.emptySet())
        .setIncludeMutations(Collections.emptySet())
        .setIncludeSubscriptions(Collections.emptySet())
        .setGeneratedDocsFolder(Paths.get("generated-docs"))
        .setIncludeImports(Collections.emptyMap())
        .setIncludeEnumImports(Collections.emptyMap())
        .setIncludeClassImports(Collections.emptyMap())
        .setJacksonVersions(Collections.emptySet());
  }

  /**
   * {@code fileWriteParallelism} is not a constructor argument in {@link CodeGenConfig}; the
   * builder must apply it via the setter after construction. Assert the value actually lands on the
   * built config rather than silently staying at the codegen default.
   */
  @Test
  void setFileWriteParallelismIsAppliedToBuiltConfig() {
    CodeGenConfig config = baseBuilder().setFileWriteParallelism(1).build();
    assertEquals(1, config.getFileWriteParallelism());
  }

  /** When left unset (null), the codegen default is preserved (a positive parallelism). */
  @Test
  void unsetFileWriteParallelismKeepsCodegenDefault() {
    CodeGenConfig config = baseBuilder().build();
    assertTrue(
        config.getFileWriteParallelism() >= 1,
        "Unset fileWriteParallelism should keep the codegen default (>= 1)");
  }
}
