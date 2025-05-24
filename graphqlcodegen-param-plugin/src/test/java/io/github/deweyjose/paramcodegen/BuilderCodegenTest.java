package io.github.deweyjose.paramcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.io.File;
import java.util.Map;
import org.jetbrains.kotlin.psi.KtParameter;
import org.junit.jupiter.api.Test;

class BuilderCodegenTest {

  @Test
  void testDownloadCodeGenConfig() throws Exception {
    String url = BuilderCodegen.CODEGENCONFIG_URL;
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

  @Test
  void testMapKotlinTypeToJavaType_String() {
    assertEquals(ClassName.get(String.class), BuilderCodegen.mapKotlinTypeToJavaType("String"));
  }

  @Test
  void testMapKotlinTypeToJavaType_Int() {
    assertEquals(TypeName.INT, BuilderCodegen.mapKotlinTypeToJavaType("Int"));
  }

  @Test
  void testMapKotlinTypeToJavaType_Boolean() {
    assertEquals(TypeName.BOOLEAN, BuilderCodegen.mapKotlinTypeToJavaType("Boolean"));
  }

  @Test
  void testMapKotlinTypeToJavaType_Double() {
    assertEquals(TypeName.DOUBLE, BuilderCodegen.mapKotlinTypeToJavaType("Double"));
  }

  @Test
  void testMapKotlinTypeToJavaType_Float() {
    assertEquals(TypeName.FLOAT, BuilderCodegen.mapKotlinTypeToJavaType("Float"));
  }

  @Test
  void testMapKotlinTypeToJavaType_Long() {
    assertEquals(TypeName.LONG, BuilderCodegen.mapKotlinTypeToJavaType("Long"));
  }

  @Test
  void testMapKotlinTypeToJavaType_SetString() {
    assertEquals(
        ArrayTypeName.of(String.class), BuilderCodegen.mapKotlinTypeToJavaType("Set<String>"));
  }

  @Test
  void testMapKotlinTypeToJavaType_ListString() {
    assertEquals(
        ArrayTypeName.of(String.class), BuilderCodegen.mapKotlinTypeToJavaType("List<String>"));
  }

  @Test
  void testMapKotlinTypeToJavaType_SetFile() {
    assertEquals(
        ArrayTypeName.of(File.class), BuilderCodegen.mapKotlinTypeToJavaType("Set<java.io.File>"));
  }

  @Test
  void testMapKotlinTypeToJavaType_ListFile() {
    assertEquals(
        ArrayTypeName.of(File.class), BuilderCodegen.mapKotlinTypeToJavaType("List<java.io.File>"));
  }

  @Test
  void testMapKotlinTypeToJavaType_Path() {
    assertEquals(
        ClassName.get("java.nio.file", "Path"),
        BuilderCodegen.mapKotlinTypeToJavaType("java.nio.file.Path"));
  }

  @Test
  void testMapKotlinTypeToJavaType_MapStringString() {
    TypeName expected =
        ParameterizedTypeName.get(
            ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(String.class));
    assertEquals(expected, BuilderCodegen.mapKotlinTypeToJavaType("Map<String, String>"));
  }

  @Test
  void testMapKotlinTypeToJavaType_UnknownType() {
    assertEquals(
        ClassName.get(Object.class), BuilderCodegen.mapKotlinTypeToJavaType("UnknownType"));
  }
}
