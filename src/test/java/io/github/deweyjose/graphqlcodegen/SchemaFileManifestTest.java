package io.github.deweyjose.graphqlcodegen;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.deweyjose.graphqlcodegen.SchemaFileManifest.generateChecksum;
import static junit.framework.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class SchemaFileManifestTest {

  @TempDir
  Path tempFolder;

  @Test
  void testFindGraphqlFiles() {
    File directory = getFile("schema");
    Set<File> files = SchemaFileManifest.findGraphQLSFiles(directory);
    assertEquals(4, files.size());
  }

  @Test
  void testIsGraphqlFile() {
    assertTrue(SchemaFileManifest.isGraphqlFile(new File("abc/foo.graphql")));
    assertTrue(SchemaFileManifest.isGraphqlFile(new File("abc/foo.graphqls")));
    assertFalse(SchemaFileManifest.isGraphqlFile(new File("abc/foo.graph")));
    assertFalse(SchemaFileManifest.isGraphqlFile(new File("abc")));
  }

  @SneakyThrows
  @Test
  void testManifestNoChange() {

    File bar = getFile("schema/bar.graphqls");
    File foo = getFile("schema/foo.graphqls");

    Properties properties = new Properties();
    properties.put(tempFolder.relativize(bar.toPath()).toString(), "7cada13b5b8770e46f7a69e8856abdb9");
    properties.put(tempFolder.relativize(foo.toPath()).toString(), "61bbd2d58c22dfb3c664829ad116f7e9");

    File manifest = tempFolder.resolve("manifest.props").toFile();
    try (FileOutputStream fis = new FileOutputStream(manifest)) {
      properties.store(fis, "Schema Manifest");
    }

    SchemaFileManifest sfm = new SchemaFileManifest(new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());

    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());

    sfm.syncManifest();

    sfm = new SchemaFileManifest(new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());

    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());
  }

  @SneakyThrows
  @Test
  void testManifestRequiresChange() {

    File bar = getFile("schema/bar.graphqls");
    File foo = getFile("schema/foo.graphqls");
    File manifest = tempFolder.resolve("manifest.props").toFile();

    SchemaFileManifest sfm = new SchemaFileManifest(new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());

    Assertions.assertTrue(sfm.getChangedFiles().contains(foo));

    sfm.syncManifest();
    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());

    sfm = new SchemaFileManifest(new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());
    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());
    sfm.syncManifest();

    sfm = new SchemaFileManifest(new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());
    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("checksumProvider")
  void testChecksum(File file, String checksum) {
    assertEquals(checksum, generateChecksum(file));
  }

  /**
   * wrapper for resource File objects
   * @param path
   * @return File
   */
  private static File getFile(String path) {
    return new File(ValidationsTest.class.getClassLoader().getResource(path).getFile());
  }

  private static Stream<Arguments> checksumProvider() {
    return Stream.of(
      Arguments.of(getFile("schema/bar.graphqls"), "7cada13b5b8770e46f7a69e8856abdb9"),
      Arguments.of(getFile("schema/foo.graphqls"), "61bbd2d58c22dfb3c664829ad116f7e9")
    );
  }
}
