package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class SchemaFileManifestTest {

  @TempDir Path tempFolder;

  @SneakyThrows
  @Test
  void testManifestNoChange() {

    File bar = TestUtils.getFile("schema/bar.graphqls");
    File foo = TestUtils.getFile("schema/foo.graphqls");

    Properties properties = new Properties();
    properties.put(
        tempFolder.relativize(bar.toPath()).toString(), "7cada13b5b8770e46f7a69e8856abdb9");
    properties.put(
        tempFolder.relativize(foo.toPath()).toString(), "61bbd2d58c22dfb3c664829ad116f7e9");

    File manifest = tempFolder.resolve("manifest.props").toFile();
    try (FileOutputStream fis = new FileOutputStream(manifest)) {
      properties.store(fis, "Schema Manifest");
    }

    SchemaManifestService sfm =
        new SchemaManifestService(
            new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());

    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());

    sfm.syncManifest();

    sfm =
        new SchemaManifestService(
            new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());

    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());
  }

  @SneakyThrows
  @Test
  void testManifestRequiresChange() {

    File bar = TestUtils.getFile("schema/bar.graphqls");
    File foo = TestUtils.getFile("schema/foo.graphqls");
    File manifest = tempFolder.resolve("manifest.props").toFile();

    SchemaManifestService sfm =
        new SchemaManifestService(
            new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());

    Assertions.assertTrue(sfm.getChangedFiles().contains(foo));

    sfm.syncManifest();
    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());

    sfm =
        new SchemaManifestService(
            new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());
    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());
    sfm.syncManifest();

    sfm =
        new SchemaManifestService(
            new HashSet<>(Arrays.asList(foo, bar)), manifest, tempFolder.toFile());
    Assertions.assertTrue(sfm.getChangedFiles().isEmpty());
  }

  private static Stream<Arguments> checksumProvider() {
    return Stream.of(
        Arguments.of(TestUtils.getFile("schema/bar.graphqls"), "7cada13b5b8770e46f7a69e8856abdb9"),
        Arguments.of(TestUtils.getFile("schema/foo.graphqls"), "61bbd2d58c22dfb3c664829ad116f7e9"));
  }

  @ParameterizedTest
  @MethodSource("checksumProvider")
  void testChecksum(File file, String checksum) {
    assertEquals(checksum, SchemaManifestService.generateChecksum(file));
  }
}
