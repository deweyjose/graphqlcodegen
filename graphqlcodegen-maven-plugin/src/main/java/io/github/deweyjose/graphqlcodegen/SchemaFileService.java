package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.maven.artifact.Artifact;

/** Service for managing schema files. */
@Getter
@Setter
public class SchemaFileService {
  private final File outputDir;
  private final SchemaManifestService manifest;
  private Set<File> schemaPaths;
  private List<File> schemaJarFilesFromDependencies;

  public SchemaFileService(File outputDir, SchemaManifestService manifest) {
    this.schemaPaths = new HashSet<>();
    this.outputDir = outputDir;
    this.manifest = manifest;
  }

  /**
   * Loads the schema paths into the schema paths.
   *
   * @param schemaPaths the schema paths to load
   */
  public void loadExpandedSchemaPaths(Collection<File> schemaPaths) {
    setSchemaPaths(
        schemaPaths.stream()
            .map(
                path -> {
                  if (path.isFile()) {
                    return Stream.of(path);
                  } else {
                    return findGraphQLSFiles(path).stream();
                  }
                })
            .flatMap(stream -> stream)
            .collect(Collectors.toSet()));
  }

  public void loadSchemaJarFilesFromDependencies(
      Set<Artifact> artifacts, Collection<String> schemaJarFilesFromDependencies) {
    this.schemaJarFilesFromDependencies =
        extractSchemaFilesFromDependencies(artifacts, schemaJarFilesFromDependencies);
  }

  /**
   * Loads the schema URLs into the schema paths.
   *
   * @param schemaUrls the schema URLs to load
   */
  @SneakyThrows
  public void loadSchemaUrls(String[] schemaUrls) {
    for (String url : schemaUrls) {
      schemaPaths.add(saveUrlToFile(url, outputDir));
    }
  }

  public void checkHasSchemaFiles() {
    if (getSchemaPaths().isEmpty()
        && Optional.ofNullable(getSchemaJarFilesFromDependencies())
            .orElse(Collections.emptyList())
            .isEmpty()) {
      throw new IllegalArgumentException("No schema files found. Please check your configuration.");
    }
  }

  public boolean noWorkToDo() {
    return getSchemaPaths().isEmpty() && getSchemaJarFilesFromDependencies().isEmpty();
  }

  public void syncManifest() {
    try {
      manifest.syncManifest();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void filterChangedSchemaFiles() {
    manifest.setFiles(new HashSet<>(schemaPaths));
    Set<File> changed = new HashSet<>(schemaPaths);
    changed.retainAll(manifest.getChangedFiles());
    setSchemaPaths(changed);
  }

  public String fetchSchema(String url) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }

  public File saveUrlToFile(String url, File outputDir) throws IOException, InterruptedException {
    String content = fetchSchema(url);
    String fileName = "remote-schemas/" + md5Hex(url) + ".graphqls";
    File outFile = new File(outputDir, fileName);
    Files.createDirectories(outFile.getParentFile().toPath());
    Files.writeString(outFile.toPath(), content);
    return outFile;
  }

  private String md5Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to compute MD5 hash", e);
    }
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
   * Extracts the schema files from the dependencies.
   *
   * @param dependencyArtifacts the set of dependency artifacts
   * @param schemaJarFilesFromDependencies the schema jar files from dependencies
   * @return the schema files
   */
  public static List<File> extractSchemaFilesFromDependencies(
      Set<Artifact> dependencyArtifacts, Collection<String> schemaJarFilesFromDependencies) {
    List<File> files = new java.util.ArrayList<>();

    for (final String jarDep : schemaJarFilesFromDependencies) {
      final String jarDepClean = jarDep.trim();
      if (jarDepClean.isEmpty()) {
        continue;
      }

      final java.util.Optional<Artifact> artifactOpt =
          findArtifactFromDependencies(dependencyArtifacts, jarDepClean);
      if (artifactOpt.isPresent()) {
        final Artifact artifact = artifactOpt.get();
        final File file = artifact.getFile();
        files.add(file);
      }
    }

    return files;
  }

  /**
   * Finds the artifact from the dependencies.
   *
   * @param dependencyArtifacts the set of dependency artifacts
   * @param artifactRef the artifact reference
   * @return the artifact
   */
  private static java.util.Optional<Artifact> findArtifactFromDependencies(
      Set<Artifact> dependencyArtifacts, final String artifactRef) {
    final String cleanRef = artifactRef.trim();

    for (final Artifact artifact : dependencyArtifacts) {
      final String ref =
          String.format(
              "%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());

      if (ref.equals(cleanRef)) {
        return java.util.Optional.of(artifact);
      }
    }
    return java.util.Optional.empty();
  }
}
