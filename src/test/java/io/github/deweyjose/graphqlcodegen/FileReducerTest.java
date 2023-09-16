package io.github.deweyjose.graphqlcodegen;

import junit.framework.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class FileReducerTest {

  @ParameterizedTest
  @MethodSource("happyPathFileListProvider")
  void testHappyPaths(List<File> paths) {
    Assertions.assertTrue(FileReducer.verifySchemaPaths(paths).containsAll(paths));
  }

  @ParameterizedTest
  @MethodSource("overlappingFileListProvider")
  void testDirectoryOverlap(List<File> paths) {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> FileReducer.verifySchemaPaths(paths),
      "should not succeed"
    );
  }

  /**
   * wrapper for resource File objects
   * @param path
   * @return File
   */
  private static File getFile(String path) {
    return new File(FileReducerTest.class.getClassLoader().getResource(path).getFile());
  }

  /**
   * A set of scenarios with no overlapping ancestry.
   * @return
   */
  private static Stream<Arguments> happyPathFileListProvider() {
    return Stream.of(
      Arguments.of(Arrays.asList(
        getFile("schema")
      )),
      Arguments.of(Arrays.asList(
        getFile("schema/bar"),
        getFile("schema/foo")
      )),
      Arguments.of(Arrays.asList(
        getFile("schema/bar/sink/kitchen.graphqls"),
        getFile("schema/foo")
      )),
      Arguments.of(Arrays.asList(
        getFile("schema/bar/sink/kitchen.graphqls"),
        getFile("schema/foo/it.graphqls")
      ))
    );
  }

  /**
   * A set of overlapping file scenarios.
   * Each should fail in accordance with DGS
   * inclusion behavior for parent directories.
   * @return
   */
  private static Stream<Arguments> overlappingFileListProvider() {
    return Stream.of(
      Arguments.of(Arrays.asList(
        getFile("schema"),
        getFile("schema/bar")
      )),
      Arguments.of(Arrays.asList(
        getFile("schema"),
        getFile("schema/bar/sink/kitchen.graphqls")
      )),
      Arguments.of(Arrays.asList(
        getFile("schema/bar/sink/kitchen.graphqls"),
        getFile("schema/bar")
      ))
    );
  }
}
