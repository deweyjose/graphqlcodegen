package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nu.studer.java.util.OrderedProperties;
import nu.studer.java.util.OrderedProperties.OrderedPropertiesBuilder;

/** Manages a manifest of GraphQL schema files and their checksums for change detection. */
@Slf4j
public class SchemaManifestService {
  private Set<File> files;
  private final File manifestPath;
  private final File projectPath;

  /**
   * Constructs a SchemaFileManifest with a set of files, manifest path, and project path.
   *
   * @param files the set of schema files
   * @param manifestPath the manifest file path
   * @param projectPath the project base directory
   */
  public SchemaManifestService(Set<File> files, File manifestPath, File projectPath) {
    this.files = files;
    this.manifestPath = manifestPath;
    this.projectPath = projectPath;
  }

  /**
   * Constructs a SchemaFileManifest with a manifest path and project path.
   *
   * @param manifestPath the manifest file path
   * @param projectPath the project base directory
   */
  public SchemaManifestService(File manifestPath, File projectPath) {
    this.manifestPath = manifestPath;
    this.projectPath = projectPath;
  }

  /**
   * Generates an MD5 checksum for the given file.
   *
   * @param path the file to checksum
   * @return the checksum as a hex string
   */
  @SneakyThrows
  public static String generateChecksum(File path) {
    byte[] data = Files.readAllBytes(Paths.get(path.toURI()));
    byte[] hash = MessageDigest.getInstance("MD5").digest(data);
    String checksum = new BigInteger(1, hash).toString(16);
    return checksum;
  }

  /**
   * Returns true if the file is a GraphQL schema file (.graphql, .graphqls, .gqls).
   *
   * @param file the file to check
   * @return true if the file is a GraphQL schema file
   */
  public static boolean isGraphqlFile(File file) {
    return file.getName().endsWith(".graphqls")
        || file.getName().endsWith(".graphql")
        || file.getName().endsWith(".gqls");
  }

  /**
   * Recursively finds all GraphQL schema files in a directory.
   *
   * @param directory the directory to search
   * @return a set of GraphQL schema files
   */
  public static Set<File> findGraphQLSFiles(File directory) {
    Set<File> result = new HashSet<>();

    File[] contents = directory.listFiles();
    if (contents != null) {
      for (File content : contents) {
        if (content.isFile() && isGraphqlFile(content)) {
          result.add(content);
        } else if (content.isDirectory()) {
          Set<File> subdirectoryGraphQLSFiles = findGraphQLSFiles(content);
          result.addAll(subdirectoryGraphQLSFiles);
        }
      }
    }

    return result;
  }

  /**
   * Sets the files to be tracked by the manifest.
   *
   * @param files the set of files
   */
  public void setFiles(Set<File> files) {
    this.files = files;
  }

  /**
   * Computes the set of files that have changed or are new and need to trigger code generation.
   *
   * @return a set of changed or new files
   */
  public Set<File> getChangedFiles() {
    Set<File> changed = new HashSet<>();
    OrderedProperties manifest = loadManifest();
    for (File file : files) {
      String oldChecksum = manifest.getProperty(relativizeToProject(file));
      if (oldChecksum == null) {
        log.info("{} is new, will generate code", file.getName());
      } else if (!oldChecksum.equals(generateChecksum(file))) {
        log.info("{} has changed, will generate code", file.getName());
      } else {
        log.info("{} has not changed, will not generate code", file.getName());
        continue;
      }
      changed.add(file);
    }
    return changed;
  }

  /**
   * Clears the old manifest, computes new checksums for each file, and saves the properties file.
   */
  @SneakyThrows
  public void syncManifest() {
    OrderedProperties manifest =
        new OrderedPropertiesBuilder().withSuppressDateInComment(true).build();
    for (File file : files) {
      manifest.setProperty(relativizeToProject(file), generateChecksum(file));
    }

    if (!manifestPath.exists()) {
      manifestPath.getParentFile().mkdirs();
    }

    try (FileOutputStream fos = new FileOutputStream(manifestPath)) {
      manifest.store(fos, "Schema Manifest");
      fos.flush();
    }
  }

  @SneakyThrows
  private OrderedProperties loadManifest() {
    OrderedProperties properties =
        new OrderedPropertiesBuilder().withSuppressDateInComment(true).build();
    if (manifestPath.exists()) {
      try (FileInputStream fis = new FileInputStream(manifestPath)) {
        properties.load(fis);
      }
    }
    return properties;
  }

  private String relativizeToProject(File file) {
    return projectPath.toPath().relativize(file.toPath()).toString();
  }
}
