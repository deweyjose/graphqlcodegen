package io.github.deweyjose.graphqlcodegen.services;

import io.github.deweyjose.graphqlcodegen.parameters.IntrospectionRequest;
import io.github.deweyjose.graphqlcodegen.services.RemoteSchemaService.IntrospectionOperation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
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
  private final RemoteSchemaService remoteSchemaService;

  private Set<File> schemaPaths;
  private List<File> schemaJarFilesFromDependencies;

  /**
   * Constructs a new SchemaFileService with the given output directory and manifest.
   *
   * @param outputDir the output directory to save the schema files
   * @param manifest the manifest service to use
   */
  public SchemaFileService(File outputDir, SchemaManifestService manifest) {
    this(outputDir, manifest, new RemoteSchemaService());
  }

  /**
   * Constructs a new SchemaFileService with the given output directory, manifest, and remote schema
   * service.
   *
   * @param outputDir the output directory to save the schema files
   * @param manifest the manifest service to use
   * @param remoteSchemaService the remote schema service to use
   */
  public SchemaFileService(
      File outputDir, SchemaManifestService manifest, RemoteSchemaService remoteSchemaService) {
    this.schemaPaths = new HashSet<>();
    this.outputDir = outputDir;
    this.manifest = manifest;
    this.remoteSchemaService = remoteSchemaService;
  }

  /**
   * Loads the schema paths, expanding directories to include all GraphQL schema files within them.
   *
   * @param schemaPaths the collection of files or directories to load as schema paths
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

  /**
   * Loads the schema jar files from dependencies into the internal list.
   *
   * @param artifacts the set of Maven artifacts (dependencies)
   * @param schemaJarFilesFromDependencies the set of dependency coordinates to extract schema jars
   *     from
   */
  public void loadSchemaJarFilesFromDependencies(
      Set<Artifact> artifacts, Set<String> schemaJarFilesFromDependencies) {
    this.schemaJarFilesFromDependencies =
        extractSchemaFilesFromDependencies(artifacts, schemaJarFilesFromDependencies);
  }

  /**
   * Loads remote schema URLs and saves them as files in the output directory, adding them to
   * schemaPaths.
   *
   * @param schemaUrls the list of schema URLs to load
   */
  @SneakyThrows
  public void loadSchemaUrls(List<String> schemaUrls) {
    for (String url : schemaUrls) {
      String content = remoteSchemaService.getRemoteSchemaFile(url);
      schemaPaths.add(saveUrlToFile(url, content));
    }
  }

  /**
   * Loads introspected schemas from the given collection of IntrospectionRequest objects.
   *
   * @param schemaUrls the collection of IntrospectionRequest objects to load
   */
  @SneakyThrows
  public void loadIntrospectedSchemas(Collection<IntrospectionRequest> schemaUrls) {
    for (IntrospectionRequest request : schemaUrls) {
      String query = Optional.ofNullable(request.getQuery()).orElse(Constants.DEFAULT_QUERY);
      String operationName =
          Optional.ofNullable(request.getOperationName()).orElse(Constants.DEFAULT_OPERATION_NAME);
      IntrospectionOperation operation =
          IntrospectionOperation.builder().query(query).operationName(operationName).build();
      String content =
          remoteSchemaService.getIntrospectedSchemaFile(
              request.getUrl(), operation, request.getHeaders());
      schemaPaths.add(saveUrlToFile(request.getUrl(), content));
    }
  }

  /**
   * Checks if there are any schema files or schema jars to generate. Throws if none are found.
   *
   * @throws IllegalArgumentException if no schema files or jars are found
   */
  public void checkHasSchemaFiles() {
    if (getSchemaPaths().isEmpty()
        && Optional.ofNullable(getSchemaJarFilesFromDependencies())
            .orElse(Collections.emptyList())
            .isEmpty()) {
      throw new IllegalArgumentException("No schema files found. Please check your configuration.");
    }
  }

  /**
   * Returns true if there are no schema files or schema jars to process.
   *
   * @return true if there is no work to do, false otherwise
   */
  public boolean noWorkToDo() {
    return getSchemaPaths().isEmpty() && getSchemaJarFilesFromDependencies().isEmpty();
  }

  /** Syncs the manifest by writing the current state to disk. Prints stack trace on error. */
  public void syncManifest() {
    try {
      manifest.syncManifest();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Filters schemaPaths to only include files that have changed according to the manifest. */
  public void filterChangedSchemaFiles() {
    manifest.setFiles(new HashSet<>(schemaPaths));
    Set<File> changed = new HashSet<>(schemaPaths);
    changed.retainAll(manifest.getChangedFiles());
    setSchemaPaths(changed);
  }

  /**
   * Fetches the contents of a remote schema from the given URL as a string.
   *
   * @param url the URL to fetch the schema from
   * @return the schema SDL as a string
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the thread is interrupted
   */
  public String fetchSchema(String url) throws IOException, InterruptedException {
    return remoteSchemaService.getRemoteSchemaFile(url);
  }

  /**
   * Downloads a remote schema from the given URL and saves it as a file in the output directory.
   *
   * @param url the URL to download the schema from
   * @param outputDir the output directory to save the file in
   * @return the File object representing the saved schema
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the thread is interrupted
   */
  private File saveUrlToFile(String url, String content) throws IOException, InterruptedException {
    String fileName =
        "remote-schemas/" + Base64.getEncoder().encodeToString(url.getBytes()) + ".graphqls";
    File outFile = new File(outputDir, fileName);
    Files.createDirectories(outFile.getParentFile().toPath());
    Files.writeString(outFile.toPath(), content);
    return outFile;
  }

  /**
   * Recursively finds all GraphQL schema files in a directory and its subdirectories.
   *
   * @param directory the directory to search
   * @return a set of GraphQL schema files found
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
   * @return true if the file is a GraphQL schema file, false otherwise
   */
  public static boolean isGraphqlFile(File file) {
    return file.getName().endsWith(".graphqls")
        || file.getName().endsWith(".graphql")
        || file.getName().endsWith(".gqls");
  }

  /**
   * Extracts schema files from the given set of dependency artifacts, matching the provided
   * dependency coordinates.
   *
   * @param dependencyArtifacts the set of Maven dependency artifacts
   * @param schemaJarFilesFromDependencies the collection of dependency coordinates to match
   * @return a list of schema files (jars) found in the dependencies
   */
  public static List<File> extractSchemaFilesFromDependencies(
      Set<Artifact> dependencyArtifacts, Collection<String> schemaJarFilesFromDependencies) {
    return schemaJarFilesFromDependencies.stream()
        .map(String::trim)
        .filter(jarDep -> !jarDep.isEmpty())
        .map(jarDep -> findArtifactFromDependencies(dependencyArtifacts, jarDep))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Artifact::getFile)
        .toList();
  }

  /**
   * Finds a Maven artifact in the given set of dependencies matching the provided coordinate
   * string.
   *
   * @param dependencyArtifacts the set of Maven dependency artifacts
   * @param artifactRef the Maven coordinate string (groupId:artifactId:version)
   * @return an Optional containing the matching Artifact, or empty if not found
   */
  private static Optional<Artifact> findArtifactFromDependencies(
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
    return Optional.empty();
  }
}
