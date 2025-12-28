package io.github.deweyjose.bootstrapcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.kotlin.psi.KtParameter;
import org.junit.jupiter.api.Test;

class BuilderCodegenTest {

  @Test
  void testDownloadCodeGenConfig() throws Exception {
    // Use a versioned URL (v8.2.1) instead of master branch
    String url =
        "https://raw.githubusercontent.com/Netflix/dgs-codegen/v8.2.1/graphql-dgs-codegen-core/src/main/kotlin/com/netflix/graphql/dgs/codegen/CodeGen.kt";
    String codeGenConfig = BuilderCodegen.downloadCodeGenConfig(url);
    assertNotNull(codeGenConfig, "CodeGenConfig should not be null");
    assertTrue(
        codeGenConfig.contains("class CodeGenConfig("),
        "CodeGenConfig should contain class CodeGenConfig");
  }

  @Test
  void testParseCodeGenConfigParameters() throws Exception {
    String codeGenConfig = "class CodeGenConfig(val schemaPaths: Set<String> = emptySet())";
    KtParameter[] params = BuilderCodegen.parseCodeGenConfigParameters(codeGenConfig);
    assertNotNull(params, "Params should not be null");
    assertTrue(params.length > 0, "Params should contain at least one parameter");
    assertEquals("schemaPaths", params[0].getName(), "First param should be schemaPaths");
    assertEquals(
        "Set<String>", params[0].getTypeReference().getText(), "First param should be Set<String>");
    assertEquals(
        "emptySet()", params[0].getDefaultValue().getText(), "First param should be emptySet()");
  }

  @Test
  void testGenerateBuilderClass() throws Exception {
    // Read CodeGenConfig.kt from test resources
    java.net.URL resource = getClass().getClassLoader().getResource("CodeGenConfig.kt");
    assertNotNull(resource, "Test resource CodeGenConfig.kt should exist");
    java.nio.file.Path path = java.nio.file.Paths.get(resource.toURI());
    String codeGenConfig = java.nio.file.Files.readString(path);
    KtParameter[] params = BuilderCodegen.parseCodeGenConfigParameters(codeGenConfig);
    BuilderCodegen.generateBuilderClass(params, "target/generated-sources/buildercodegen");
  }
}
