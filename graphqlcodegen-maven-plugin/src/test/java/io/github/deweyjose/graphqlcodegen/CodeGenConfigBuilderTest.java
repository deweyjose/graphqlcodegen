package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CodeGenConfigBuilderTest {

  @Test
  void unsetFileWriteParallelismKeepsCodegenDefault() {
    CodeGenConfig config = minimalBuilder().build();

    assertEquals(CodeGenConfig.DEFAULT_FILE_WRITE_PARALLELISM, config.getFileWriteParallelism());
  }

  @Test
  void fileWriteParallelismIsAppliedAfterConstruction() {
    CodeGenConfig config = minimalBuilder().setFileWriteParallelism(1).build();

    assertEquals(1, config.getFileWriteParallelism());
  }

  private static CodeGenConfigBuilder minimalBuilder() {
    return new CodeGenConfigBuilder()
        .setSchemas(Collections.emptySet())
        .setSchemaFiles(Collections.emptySet())
        .setSchemaJarFilesFromDependencies(Collections.emptyList())
        .setOutputDir(Path.of("target/generated"))
        .setExamplesOutputDir(Path.of("target/generated-examples"))
        .setPackageName("com.example")
        .setSubPackageNameClient("client")
        .setSubPackageNameDatafetchers("datafetchers")
        .setSubPackageNameTypes("types")
        .setSubPackageNameDocs("docs")
        .setLanguage(Language.JAVA)
        .setTypeMapping(Collections.emptyMap())
        .setIncludeQueries(Collections.emptySet())
        .setIncludeMutations(Collections.emptySet())
        .setIncludeSubscriptions(Collections.emptySet())
        .setGeneratedDocsFolder(Path.of("target/generated-docs"))
        .setIncludeImports(Collections.emptyMap())
        .setIncludeEnumImports(Collections.emptyMap())
        .setIncludeClassImports(Collections.emptyMap());
  }
}
