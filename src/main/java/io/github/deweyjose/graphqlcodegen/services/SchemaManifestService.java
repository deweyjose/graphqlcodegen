package io.github.deweyjose.graphqlcodegen.services;

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
   * @param files the set of schema files to track
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
   * @param manifestDir the directory where the manifest file will be created
   * @param projectPath the project base directory
   */
  public SchemaManifestService(File manifestDir, File projectPath) {
    this.manifestPath = new File(manifestDir, "schema-manifest.props");
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
   * Sets the files to be tracked by the manifest.
   *
   * @param files the set of files to track
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

  /** Syncs the manifest with the files. */
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

  /**
   * Loads the manifest from the manifest path, or returns an empty manifest if it does not exist.
   *
   * @return the loaded manifest properties
   * @throws java.io.IOException if an I/O error occurs reading the manifest
   */
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

  /**
   * Relativizes a file path to the project path.
   *
   * @param file the file to relativize
   * @return the relativized file path as a string
   */
  private String relativizeToProject(File file) {
    return projectPath.toPath().relativize(file.toPath()).toString();
  }
}
