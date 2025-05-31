package io.github.deweyjose.graphqlcodegen;

import static junit.framework.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SchemaFileServiceTest {

  private final SchemaFileService service = new SchemaFileService();

  @Test
  void testFindGraphqlFiles() {
    File directory = getFile("schema");
    Set<File> files = service.expandSchemaPaths(new File[] {directory});
    assertEquals(2, files.size());
  }

  @Test
  void testIsGraphqlFile() {
    assertTrue(SchemaManifestService.isGraphqlFile(new File("abc/foo.graphql")));
    assertTrue(SchemaManifestService.isGraphqlFile(new File("abc/foo.graphqls")));
    assertFalse(SchemaManifestService.isGraphqlFile(new File("abc/foo.graph")));
    assertFalse(SchemaManifestService.isGraphqlFile(new File("abc")));
  }

  private static File getFile(String path) {
    return new File(SchemaFileServiceTest.class.getClassLoader().getResource(path).getFile());
  }
}
